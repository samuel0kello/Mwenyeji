package com.samuelokello.mwenyeji.data.repository

import com.samuelokello.mwenyeji.datasources.firebase.FirebaseService

interface AuthRepository {
    /**
     * Returns the current user's UID or null if not signed in.
     */
    val currentUserId: String?

    /**
     * Returns true if the current user is anonymous.
     */
    val isAnonymous: Boolean

    /**
     * Signs in anonymously — called on app launch.
     * Returns UID on success, null on failure.
     */
    suspend fun signInAnonymously(): String?

    /**
     * Signs in with email and password.
     * Returns ID token on success, null on failure.
     */
    suspend fun login(email: String, password: String): String?

    /**
     * Creates account with email and password.
     * Returns ID token on success, null on failure.
     */
    suspend fun signup(email: String, password: String): String?

    /**
     * Signs in with Google token.
     * Returns ID token on success, null on failure.
     */
    suspend fun signInWithGoogle(googleToken: String): String?

    /**
     * Signs out the current user.
     */
    fun logout()
}

class AuthRepositoryImpl(
    private val firebaseService: FirebaseService
) : AuthRepository {

    override val currentUserId: String?
        get() = firebaseService.currentUserId()

    override val isAnonymous: Boolean
        get() = firebaseService.isAnonymous()

    override suspend fun signInAnonymously(): String? =
        firebaseService.signInAnonymously()

    override suspend fun login(email: String, password: String): String? =
        firebaseService.loginWithEmailAndPassword(email, password)

    override suspend fun signup(email: String, password: String): String? =
        firebaseService.createUserWithEmailAndPassword(email, password)

    override suspend fun signInWithGoogle(googleToken: String): String? =
        firebaseService.createAccountWithGoogle(googleToken)

    override fun logout() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
    }
}