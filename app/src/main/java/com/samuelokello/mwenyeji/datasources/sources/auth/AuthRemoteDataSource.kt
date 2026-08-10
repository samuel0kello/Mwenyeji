package com.samuelokello.mwenyeji.datasources.sources.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.samuelokello.mwenyeji.datasources.core.firebase.safeFirebaseCall
import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.sources.auth.dto.AuthStateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface AuthRemoteDataSource {
    val currentUserId: String?
    val isAnonymous: Boolean

    fun observeAuthState(): Flow<AuthStateData>

    suspend fun signInAnonymously(): NetworkResult<String>

    suspend fun signInWithEmail(
        email: String,
        password: String,
    ): NetworkResult<String>

    suspend fun createAccountWithEmail(
        email: String,
        password: String,
    ): NetworkResult<String>

    suspend fun signInWithGoogle(idToken: String): NetworkResult<String>

    suspend fun sendPasswordResetEmail(email: String): NetworkResult<Unit>

    suspend fun confirmPasswordReset(
        code: String,
        newPassword: String,
    ): NetworkResult<Unit>

    suspend fun getIdToken(forceRefresh: Boolean = false): NetworkResult<String>

    suspend fun signOut(): NetworkResult<Unit>
}

internal class FirebaseAuthRemoteDataSource(
    private val auth: FirebaseAuth,
) : AuthRemoteDataSource {
    override val currentUserId: String? get() = auth.currentUser?.uid
    override val isAnonymous: Boolean get() = auth.currentUser?.isAnonymous == true

    override fun observeAuthState(): Flow<AuthStateData> =
        callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { fb ->
                    val user = fb.currentUser
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

    override suspend fun signInAnonymously(): NetworkResult<String> =
        safeFirebaseCall {
            auth.currentUser?.uid
                ?: auth
                    .signInAnonymously()
                    .await()
                    .user
                    ?.uid
                ?: error("Anonymous sign-in returned null user")
        }

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): NetworkResult<String> =
        safeFirebaseCall {
            auth
                .signInWithEmailAndPassword(email, password)
                .await()
                .user
                ?.getIdToken(false)
                ?.await()
                ?.token
                ?: error("Sign-in returned null token")
        }

    override suspend fun createAccountWithEmail(
        email: String,
        password: String,
    ): NetworkResult<String> =
        safeFirebaseCall {
            auth
                .createUserWithEmailAndPassword(email, password)
                .await()
                .user
                ?.getIdToken(false)
                ?.await()
                ?.token
                ?: error("Account creation returned null token")
        }

    override suspend fun signInWithGoogle(idToken: String): NetworkResult<String> =
        safeFirebaseCall {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val current = auth.currentUser
            val uid =
                if (current != null && current.isAnonymous) {
                    try {
                        current
                            .linkWithCredential(credential)
                            .await()
                            .user
                            ?.uid
                    } catch (_: FirebaseAuthUserCollisionException) {
                        // Google account exists — fall back to signing in
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
            uid ?: error("Google sign-in returned null user")
        }

    override suspend fun sendPasswordResetEmail(email: String): NetworkResult<Unit> =
        safeFirebaseCall { auth.sendPasswordResetEmail(email).await() }

    override suspend fun confirmPasswordReset(
        code: String,
        newPassword: String,
    ): NetworkResult<Unit> =
        safeFirebaseCall {
            auth.confirmPasswordReset(code, newPassword).await()
        }

    override suspend fun getIdToken(forceRefresh: Boolean): NetworkResult<String> =
        safeFirebaseCall {
            auth.currentUser
                ?.getIdToken(forceRefresh)
                ?.await()
                ?.token
                ?: error("No authenticated user")
        }

    override suspend fun signOut(): NetworkResult<Unit> =
        safeFirebaseCall {
            auth.signOut()
            // Caller is responsible for clearing Credential Manager state (needs Context)
        }
}
