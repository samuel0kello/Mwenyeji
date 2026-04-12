package com.samuelokello.mwenyeji.datasources.firebase

import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.datasources.firebase.dto.RouteDto
import kotlinx.coroutines.flow.Flow

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