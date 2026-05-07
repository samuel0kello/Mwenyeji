package com.samuelokello.mwenyeji.data.repository

import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.toDataResult
import com.samuelokello.mwenyeji.data.helpers.toDataResultFlow
import com.samuelokello.mwenyeji.data.mappers.toDomain
import com.samuelokello.mwenyeji.data.mappers.toDto
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.Verdict
import com.samuelokello.mwenyeji.datasources.sources.confirmation.ConfirmationsRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.routes.RoutesRemoteDataSource
import kotlinx.coroutines.flow.Flow

interface RoutesRepository {
    /** Real-time stream of all routes — filtering and sorting done client-side. */
    fun observeRoutes(): Flow<DataResult<List<Route>>>

    fun observeRouteById(id: String): Flow<DataResult<Route?>>

    suspend fun submitRoute(route: Route): DataResult<String>

    suspend fun submitVerdict(routeId: String, userId: String, verdict: Verdict): DataResult<Unit>

    suspend fun getUserVerdict(routeId: String, userId: String): DataResult<Verdict?>

    /**
     * Returns the ordered stop list for a GTFS-sourced route.
     * Community routes return an empty list — they use [Route.steps] instead.
     * Only called when route detail screen opens, never during feed loading.
     */
    suspend fun getRouteStops(routeId: String): DataResult<List<RouteStop>>
}

internal class RoutesRepositoryImpl(
    private val routesDataSource: RoutesRemoteDataSource,
    private val confirmationsDataSource: ConfirmationsRemoteDataSource,
) : RoutesRepository {
    override fun observeRoutes(): Flow<DataResult<List<Route>>> =
        routesDataSource.observeRoutes().toDataResultFlow { dtos ->
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

    override suspend fun getRouteStops(routeId: String): DataResult<List<RouteStop>> =
        routesDataSource.getRouteStops(routeId).toDataResult { dto ->
            dto?.toOutboundStops() ?: emptyList()
        }
}
