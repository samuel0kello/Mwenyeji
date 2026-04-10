package com.samuelokello.mwenyeji.datasources.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.datasources.firebase.dto.RouteDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseServiceImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : FirebaseService {

    private val routesCollection = firestore.collection("routes")

    //auth

    override suspend fun loginWithEmailAndPassword(email: String, password: String): String? =
        runCatching {
            auth.signInWithEmailAndPassword(email, password).await()
                .user?.getIdToken(false)?.await()?.token
        }.getOrNull()

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): String? =
        runCatching {
            auth.createUserWithEmailAndPassword(email, password).await()
                .user?.getIdToken(false)?.await()?.token
        }.getOrNull()

    override suspend fun createAccountWithGoogle(
        googleToken: String,
        accessToken: String?
    ): String? =
        runCatching {
            val credential = GoogleAuthProvider.getCredential(googleToken, accessToken)
            auth.signInWithCredential(credential).await()
                .user?.getIdToken(false)?.await()?.token
        }.getOrNull()

    override suspend fun sendPasswordResetEmail(email: String) {
        runCatching { auth.sendPasswordResetEmail(email).await() }
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String) {
        runCatching { auth.confirmPasswordReset(code, newPassword).await() }
    }

    override suspend fun getIdToken(): String? = runCatching {
        auth.currentUser?.getIdToken(false)?.await()?.token
    }.getOrNull()

    override suspend fun signInAnonymously(): String? = runCatching {
        auth.currentUser?.uid ?: auth.signInAnonymously().await().user?.uid
    }.getOrNull()

    override fun isAnonymous(): Boolean = auth.currentUser?.isAnonymous ?: true

    override fun currentUserId(): String? = auth.currentUser?.uid

    //routes

    override fun getRoutes(timeOfDay: TimeOfDay): Flow<List<RouteDto>> = callbackFlow {
        val query = if (timeOfDay == TimeOfDay.ANYTIME) {
            routesCollection
                .orderBy("confirmedCount", Query.Direction.DESCENDING)
                .limit(20)
        } else {
            routesCollection
                .whereEqualTo("bestTimeOfDay", timeOfDay.name)
                .orderBy("confirmedCount", Query.Direction.DESCENDING)
                .limit(20)
        }
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error); return@addSnapshotListener
            }
            val dtos = snapshot?.documents?.mapNotNull {
                it.toObject(RouteDto::class.java)
            } ?: emptyList()
            trySend(dtos)
        }
        awaitClose { listener.remove() }
    }

    override fun getRouteById(id: String): Flow<RouteDto?> = callbackFlow {
        val listener = routesCollection.document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                trySend(snapshot?.toObject(RouteDto::class.java))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun submitRoute(dto: RouteDto): String {
        val ref = routesCollection.document()
        ref.set(dto.copy(id = ref.id)).await()
        return ref.id
    }

    override suspend fun confirmRoute(routeId: String, userId: String, verdict: String) {
        routesCollection
            .document(routeId)
            .collection("confirmations")
            .document(userId)
            .set(
                mapOf(
                    "verdict" to verdict,
                    "userId" to userId,
                    "confirmedAt" to Timestamp.now(),
                )
            ).await()
    }
}