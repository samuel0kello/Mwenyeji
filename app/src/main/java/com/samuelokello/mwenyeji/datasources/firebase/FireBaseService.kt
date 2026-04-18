package com.samuelokello.mwenyeji.datasources.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.datasources.firebase.dto.AuthStateData
import com.samuelokello.mwenyeji.datasources.firebase.dto.RouteDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface FirebaseService {
    fun authStateFlow(): Flow<AuthStateData>

    suspend fun loginWithEmailAndPassword(email: String, password: String): String?

    suspend fun createUserWithEmailAndPassword(email: String, password: String): String?

    suspend fun createAccountWithGoogle(googleToken: String, accessToken: String? = null): String?

    suspend fun sendPasswordResetEmail(email: String)

    suspend fun confirmPasswordReset(code: String, newPassword: String)

    suspend fun getIdToken(): String?

    suspend fun signInAnonymously(): String?

    fun isAnonymous(): Boolean

    fun currentUserId(): String?

    suspend fun signOut()

    suspend fun signInWithGoogle(idToken: String): String?

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

    override fun authStateFlow(): Flow<AuthStateData> =
        callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    trySend(
                        AuthStateData(
                            uid = user?.uid,
                            isAnonymous = user?.isAnonymous ?: true,
                            displayName = user?.displayName,
                            email = user?.email,
                            photoUrl = user?.photoUrl?.toString(),
                        ),
                    )
                }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    override suspend fun signInAnonymously(): String? =
        runCatching {
            auth.currentUser?.uid ?: auth
                .signInAnonymously()
                .await()
                .user
                ?.uid
        }.getOrNull()

    override suspend fun signInWithGoogle(idToken: String): String? =
        runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val currentUser = auth.currentUser

            if (currentUser != null && currentUser.isAnonymous) {
                // Link Google to the existing anonymous account so the same UID is kept.
                // If linking fails (account already exists) fall back to normal sign-in.
                runCatching {
                    currentUser
                        .linkWithCredential(credential)
                        .await()
                        .user
                        ?.uid
                }.getOrElse {
                    auth
                        .signInWithCredential(credential)
                        .await()
                        .user
                        ?.uid
                }
            } else {
                auth
                    .signInWithCredential(credential)
                    .await()
                    .user
                    ?.uid
            }
        }.getOrNull()

    override suspend fun signOut() {
        auth.signOut()
        // Caller is responsible for clearing Credential Manager state
        // (must be done from a Context — handled in AuthRepository)
    }

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

    override suspend fun createAccountWithGoogle(googleToken: String, accessToken: String?): String? = signInWithGoogle(googleToken)

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

    override fun isAnonymous(): Boolean = auth.currentUser?.isAnonymous ?: true

    override fun currentUserId(): String? = auth.currentUser?.uid

    override suspend fun getStableUserId(deviceId: String): String {
        val stored =
            firestore
                .collection("device_identities")
                .document(deviceId)
                .get()
                .await()
        if (stored.exists()) {
            return stored.getString("uid") ?: signInAnonymouslyAndStore(deviceId)
        }
        return signInAnonymouslyAndStore(deviceId)
    }

    private suspend fun signInAnonymouslyAndStore(deviceId: String): String {
        auth.currentUser?.let { return it.uid }
        val result = auth.signInAnonymously().await()
        val uid = result.user?.uid ?: throw Exception("Failed to sign in anonymously")
        firestore
            .collection("device_identities")
            .document(deviceId)
            .set(mapOf("uid" to uid, "deviceId" to deviceId, "createdAt" to Timestamp.now()))
            .await()
        return uid
    }

    //  Routes

    override fun getRoutes(timeOfDay: TimeOfDay): Flow<List<RouteDto>> =
        callbackFlow {
            val query =
                if (timeOfDay == TimeOfDay.ANYTIME) {
                    routesCollection.orderBy("confirmedCount", Query.Direction.DESCENDING).limit(20)
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
                    val dtos = snapshot?.documents?.mapNotNull { it.toObject(RouteDto::class.java)?.copy(id = it.id) } ?: emptyList()
                    trySend(dtos)
                }
            awaitClose { listener.remove() }
        }

    override fun getRouteById(id: String): Flow<RouteDto?> =
        callbackFlow {
            val listener =
                routesCollection.document(id).addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toObject(RouteDto::class.java)?.copy(id = snapshot.id))
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

        val existingSnapshot = runCatching { confirmationRef.get().await() }.getOrNull()
        val previousVerdict = existingSnapshot?.takeIf { it.exists() }?.getString("verdict")

        if (previousVerdict == verdict) {
            firestore
                .runTransaction { transaction ->
                    transaction.delete(confirmationRef)
                    verdictToField(verdict)?.let { transaction.update(routeRef, it, FieldValue.increment(-1)) }
                }.await()
            return
        }

        firestore
            .runTransaction { transaction ->
                transaction.set(confirmationRef, mapOf("verdict" to verdict, "userId" to userId, "confirmedAt" to Timestamp.now()))
                previousVerdict?.let { verdictToField(it)?.let { f -> transaction.update(routeRef, f, FieldValue.increment(-1)) } }
                verdictToField(verdict)?.let { transaction.update(routeRef, it, FieldValue.increment(1)) }
                if (verdict == "CONFIRMED") transaction.update(routeRef, "lastConfirmedAt", Timestamp.now())
            }.await()
    }

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
            confirmations.documents.forEach { c ->
                when (c.getString("verdict")) {
                    "CONFIRMED" -> confirmed++
                    "DIDNT_WORK" -> didntWork++
                    "OUTDATED" -> outdated++
                }
            }
            routeDoc.reference
                .update(
                    mapOf("confirmedCount" to confirmed, "didntWorkCount" to didntWork, "outdatedCount" to outdated),
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
