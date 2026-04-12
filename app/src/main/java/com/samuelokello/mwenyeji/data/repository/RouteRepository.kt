package com.samuelokello.mwenyeji.data.repository

import com.samuelokello.mwenyeji.data.mappers.toDomain
import com.samuelokello.mwenyeji.data.mappers.toDto
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.datasources.firebase.FirebaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface RouteRepository {
    fun getRoutes(timeOfDay: TimeOfDay): Flow<List<Route>>

    fun getRouteById(id: String): Flow<Route?>

    suspend fun submitRoute(route: Route): Result<String>

    suspend fun confirmRoute(
        routeId: String,
        userId: String,
        verdict: String,
    ): Result<Unit>

    suspend fun getUserVerdict(
        routeId: String,
        userId: String,
    ): String?
}

class RouteRepositoryImpl(
    private val firebaseService: FirebaseService, // ← only dependency
) : RouteRepository {
    override fun getRoutes(timeOfDay: TimeOfDay): Flow<List<Route>> =
        firebaseService.getRoutes(timeOfDay).map { dtos ->
            dtos.map { it.toDomain() }
        }

    override fun getRouteById(id: String): Flow<Route?> =
        firebaseService.getRouteById(id).map { it?.toDomain() }

    override suspend fun submitRoute(route: Route): Result<String> =
        runCatching {
            firebaseService.submitRoute(route.toDto())
        }

    override suspend fun confirmRoute(
        routeId: String,
        userId: String,
        verdict: String,
    ): Result<Unit> =
        runCatching {
            firebaseService.confirmRoute(routeId, userId, verdict)
        }

    override suspend fun getUserVerdict(
        routeId: String,
        userId: String,
    ): String? = firebaseService.getUserVerdict(routeId, userId)
}
