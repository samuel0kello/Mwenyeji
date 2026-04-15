package com.samuelokello.mwenyeji.datasources.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.datasources.firebase.dto.RouteDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface FirebaseService {
    suspend fun loginWithEmailAndPassword(email: String, password: String): String?

    suspend fun createUserWithEmailAndPassword(email: String, password: String): String?

    suspend fun createAccountWithGoogle(googleToken: String, accessToken: String? = null): String?

    suspend fun sendPasswordResetEmail(email: String)

    suspend fun confirmPasswordReset(code: String, newPassword: String)

    suspend fun getIdToken(): String?

    suspend fun signInAnonymously(): String?

    fun isAnonymous(): Boolean

    fun currentUserId(): String?

    fun getRoutes(timeOfDay: TimeOfDay): Flow<List<RouteDto>>

    fun getRouteById(id: String): Flow<RouteDto?>

    suspend fun submitRoute(dto: RouteDto): String

    suspend fun confirmRoute(routeId: String, userId: String, verdict: String)

    suspend fun getUserVerdict(routeId: String, userId: String): String?

    suspend fun migrateOrphanedConfirmations()

    suspend fun getStableUserId(deviceId: String): String
}

class FirebaseServiceImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : FirebaseService {
    private val routesCollection = firestore.collection("routes")

    override suspend fun loginWithEmailAndPassword(email: String, password: String): String? =
        runCatching {
            auth
                .signInWithEmailAndPassword(email, password)
                .await()
                .user
                ?.getIdToken(false)
                ?.await()
                ?.token
        }.getOrNull()

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): String? =
        runCatching {
            auth
                .createUserWithEmailAndPassword(email, password)
                .await()
                .user
                ?.getIdToken(false)
                ?.await()
                ?.token
        }.getOrNull()

    override suspend fun createAccountWithGoogle(googleToken: String, accessToken: String?): String? =
        runCatching {
            val credential = GoogleAuthProvider.getCredential(googleToken, accessToken)
            auth
                .signInWithCredential(credential)
                .await()
                .user
                ?.getIdToken(false)
                ?.await()
                ?.token
        }.getOrNull()

    override suspend fun sendPasswordResetEmail(email: String) {
        runCatching { auth.sendPasswordResetEmail(email).await() }
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String) {
        runCatching { auth.confirmPasswordReset(code, newPassword).await() }
    }

    override suspend fun getIdToken(): String? =
        runCatching {
            auth.currentUser
                ?.getIdToken(false)
                ?.await()
                ?.token
        }.getOrNull()

    override suspend fun signInAnonymously(): String? =
        runCatching {
            // If already signed in, return existing UID
            auth.currentUser?.let { return@runCatching it.uid }

            // Sign in anonymously and store device ID in the user Profile
            val result = auth.signInAnonymously().await()
            result.user?.uid
        }.getOrNull()

    override suspend fun getStableUserId(deviceId: String): String {
        // Check if we already have a UID stored for this device
        val stored =
            firestore
                .collection("device_identities")
                .document(deviceId)
                .get()
                .await()

        if (stored.exists()) {
            val uid = stored.getString("uid") ?: return signInAnonymouslyAndStore(deviceId)
            return uid
        }

        return signInAnonymouslyAndStore(deviceId)
    }

    private suspend fun signInAnonymouslyAndStore(deviceId: String): String {
        val result = auth.signInAnonymously().await()
        val uid = result.user?.uid ?: throw Exception("Failed to sign in anonymously")

        // Store the mapping device → uid
        firestore
            .collection("device_identities")
            .document(deviceId)
            .set(
                mapOf(
                    "uid" to uid,
                    "deviceId" to deviceId,
                    "createdAt" to Timestamp.now(),
                ),
            ).await()

        return uid
    }

    override fun isAnonymous(): Boolean = auth.currentUser?.isAnonymous ?: true

    override fun currentUserId(): String? = auth.currentUser?.uid

    override fun getRoutes(timeOfDay: TimeOfDay): Flow<List<RouteDto>> =
        callbackFlow {
            val query =
                if (timeOfDay == TimeOfDay.ANYTIME) {
                    routesCollection
                        .orderBy("confirmedCount", Query.Direction.DESCENDING)
                        .limit(20)
                } else {
                    routesCollection
                        .whereEqualTo("bestTimeOfDay", timeOfDay.name)
                        .orderBy("confirmedCount", Query.Direction.DESCENDING)
                        .limit(20)
                }
            val listener =
                query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val dtos =
                        snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(RouteDto::class.java)?.copy(id = doc.id)
                        } ?: emptyList()
                    trySend(dtos)
                }
            awaitClose { listener.remove() }
        }

    override fun getRouteById(id: String): Flow<RouteDto?> =
        callbackFlow {
            val listener =
                routesCollection
                    .document(id)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        val dto = snapshot?.toObject(RouteDto::class.java)?.copy(id = snapshot.id)
                        trySend(dto)
                    }
            awaitClose { listener.remove() }
        }

    override suspend fun submitRoute(dto: RouteDto): String {
        val ref = routesCollection.document()
        ref.set(dto.copy(id = ref.id)).await()
        return ref.id
    }

    override suspend fun confirmRoute(routeId: String, userId: String, verdict: String) {
        val routeRef = routesCollection.document(routeId)
        val confirmationRef = routeRef.collection("confirmations").document(userId)

        // Read existing vote BEFORE transaction (reads inside transactions are limited)
        val existingSnapshot =
            runCatching {
                confirmationRef.get().await()
            }.getOrNull()

        val previousVerdict =
            existingSnapshot
                ?.takeIf { it.exists() }
                ?.getString("verdict")

        // User clicking the same button they already chose — toggle it off
        if (previousVerdict == verdict) {
            firestore
                .runTransaction { transaction ->
                    transaction.delete(confirmationRef)
                    val decrementField = verdictToField(verdict)
                    if (decrementField != null) {
                        transaction.update(routeRef, decrementField, FieldValue.increment(-1))
                    }
                }.await()
            return
        }

        // Normal case — new vote or switching vote
        firestore
            .runTransaction { transaction ->
                transaction.set(
                    confirmationRef,
                    mapOf(
                        "verdict" to verdict,
                        "userId" to userId,
                        "confirmedAt" to Timestamp.now(),
                    ),
                )

                // Undo previous verdict if switching
                previousVerdict?.let {
                    val decrementField = verdictToField(it)
                    if (decrementField != null) {
                        transaction.update(routeRef, decrementField, FieldValue.increment(-1))
                    }
                }

                // Apply new verdict
                val incrementField = verdictToField(verdict)
                if (incrementField != null) {
                    transaction.update(routeRef, incrementField, FieldValue.increment(1))
                }

                if (verdict == "CONFIRMED") {
                    transaction.update(routeRef, "lastConfirmedAt", Timestamp.now())
                }
            }.await()
    }

    // Returns the current user's verdict for a route, or null if they haven't voted
    override suspend fun getUserVerdict(routeId: String, userId: String): String? =
        runCatching {
            routesCollection
                .document(routeId)
                .collection("confirmations")
                .document(userId)
                .get()
                .await()
                .getString("verdict")
        }.getOrNull()

    // One-time migration — fixes orphaned confirmations from before the count fix
    // Call this once from a ViewModel or admin screen, then remove it
    override suspend fun migrateOrphanedConfirmations() {
        val routes = routesCollection.get().await()

        routes.documents.forEach { routeDoc ->
            val confirmations =
                routeDoc.reference
                    .collection("confirmations")
                    .get()
                    .await()

            var confirmed = 0
            var didntWork = 0
            var outdated = 0

            // Count each unique user's verdict
            // One document per userId so no duplicates possible
            confirmations.documents.forEach { confirmation ->
                when (confirmation.getString("verdict")) {
                    "CONFIRMED" -> confirmed++
                    "DIDNT_WORK" -> didntWork++
                    "OUTDATED" -> outdated++
                }
            }

            // Overwrite the counts on the route document with real values
            routeDoc.reference
                .update(
                    mapOf(
                        "confirmedCount" to confirmed,
                        "didntWorkCount" to didntWork,
                        "outdatedCount" to outdated,
                    ),
                ).await()
        }
    }

    private fun verdictToField(verdict: String): String? =
        when (verdict) {
            "CONFIRMED" -> "confirmedCount"
            "DIDNT_WORK" -> "didntWorkCount"
            "OUTDATED" -> "outdatedCount"
            else -> null
        }
}
