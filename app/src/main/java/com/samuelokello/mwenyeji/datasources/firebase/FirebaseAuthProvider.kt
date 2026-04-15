package com.samuelokello.mwenyeji.datasources.firebase

interface FirebaseAuthProvider {
    suspend fun createAccountWithGoogle(token: String, accessToken: String?): String?

    suspend fun createUserWithEmailAndPassword(email: String, password: String): String?

    suspend fun loginWithEmailAndPassword(email: String, password: String): String?

    suspend fun sendPasswordResetEmail(email: String)

    suspend fun confirmPasswordReset(code: String, newPassword: String)

    suspend fun getIdToken(): String?

    //  anonymous auth methods
    suspend fun signInAnonymously(): String?

    fun isAnonymous(): Boolean

    fun currentUserId(): String?
}

object FirebaseAuthHelper {
    var provider: FirebaseAuthProvider? = null
}
