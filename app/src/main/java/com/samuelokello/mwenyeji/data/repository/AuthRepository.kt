package com.samuelokello.mwenyeji.data.repository

import com.samuelokello.mwenyeji.data.helpers.DeviceIdProvider
import com.samuelokello.mwenyeji.datasources.firebase.FirebaseService

interface AuthRepository {
    val currentUserId: String?
    val isAnonymous: Boolean

    suspend fun signInAnonymously(): String?

    suspend fun ensureSignedIn(): String? // ← add this

    suspend fun login(
        email: String,
        password: String,
    ): String?

    suspend fun signup(
        email: String,
        password: String,
    ): String?

    suspend fun signInWithGoogle(googleToken: String): String?

    fun logout()
}

class AuthRepositoryImpl(
    private val firebaseService: FirebaseService,
    private val deviceIdProvider: DeviceIdProvider, // ← add this
) : AuthRepository {
    override val currentUserId: String?
        get() = firebaseService.currentUserId()

    override val isAnonymous: Boolean
        get() = firebaseService.isAnonymous()

    override suspend fun signInAnonymously(): String? = firebaseService.signInAnonymously()

    // ← add this — stable identity across reinstalls
    override suspend fun ensureSignedIn(): String? {
        val deviceId = deviceIdProvider.getDeviceId()
        return firebaseService.getStableUserId(deviceId)
    }

    override suspend fun login(
        email: String,
        password: String,
    ): String? = firebaseService.loginWithEmailAndPassword(email, password)

    override suspend fun signup(
        email: String,
        password: String,
    ): String? = firebaseService.createUserWithEmailAndPassword(email, password)

    override suspend fun signInWithGoogle(googleToken: String): String? =
        firebaseService.createAccountWithGoogle(googleToken)

    override fun logout() {
        com.google.firebase.auth.FirebaseAuth
            .getInstance()
            .signOut()
    }
}
