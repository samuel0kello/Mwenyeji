package com.samuelokello.mwenyeji.data.repository

import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.toDataResult
import com.samuelokello.mwenyeji.data.helpers.toDataResultFlow
import com.samuelokello.mwenyeji.data.mappers.toDomain
import com.samuelokello.mwenyeji.data.mappers.toDto
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.data.models.Verdict
import com.samuelokello.mwenyeji.datasources.sources.confirmation.ConfirmationsRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.routes.RoutesRemoteDataSource
import kotlinx.coroutines.flow.Flow

interface RoutesRepository {
    fun observeRoutes(timeOfDay: TimeOfDay): Flow<DataResult<List<Route>>>

    fun observeRouteById(id: String): Flow<DataResult<Route?>>

    suspend fun submitRoute(route: Route): DataResult<String>

    suspend fun submitVerdict(routeId: String, userId: String, verdict: Verdict): DataResult<Unit>

    suspend fun getUserVerdict(routeId: String, userId: String): DataResult<Verdict?>
}

internal class RoutesRepositoryImpl(
    private val routesDataSource: RoutesRemoteDataSource,
    private val confirmationsDataSource: ConfirmationsRemoteDataSource,
) : RoutesRepository {
    override fun observeRoutes(timeOfDay: TimeOfDay): Flow<DataResult<List<Route>>> =
        routesDataSource.observeRoutes(timeOfDay).toDataResultFlow { dtos ->
            dtos.map { it.toDomain() }
        }

    override fun observeRouteById(id: String): Flow<DataResult<Route?>> =
        routesDataSource.observeRouteById(id).toDataResultFlow { dto ->
            dto?.toDomain()
        }

    override suspend fun submitRoute(route: Route): DataResult<String> = routesDataSource.submitRoute(route.toDto()).toDataResult()

    override suspend fun submitVerdict(routeId: String, userId: String, verdict: Verdict): DataResult<Unit> =
        confirmationsDataSource.submitVerdict(routeId, userId, verdict).toDataResult()

    override suspend fun getUserVerdict(routeId: String, userId: String): DataResult<Verdict?> =
        confirmationsDataSource.getUserVerdict(routeId, userId).toDataResult()
}
