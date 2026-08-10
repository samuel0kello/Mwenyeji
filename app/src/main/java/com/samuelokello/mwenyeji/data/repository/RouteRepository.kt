package com.samuelokello.mwenyeji.data.repository

import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.toDataResult
import com.samuelokello.mwenyeji.data.helpers.toDataResultFlow
import com.samuelokello.mwenyeji.data.mappers.toDomain
import com.samuelokello.mwenyeji.data.models.Guide
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.Verdict
import com.samuelokello.mwenyeji.datasources.sources.confirmation.ConfirmationsRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.routes.RoutesRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.routes.dto.toDomain
import com.samuelokello.mwenyeji.datasources.sources.routes.dto.toDto
import kotlinx.coroutines.flow.Flow

interface RoutesRepository {
    fun observeRoutes(): Flow<DataResult<List<Route>>>

    fun observeRouteById(id: String): Flow<DataResult<Route?>>

    /**
     * Ordered stop list for a route.
     * Only called when route detail opens — never during feed loading.
     */
    suspend fun getRouteStops(routeId: String): DataResult<List<RouteStop>>

    /**
     * Real-time stream of all guides for a route, sorted by confirmedCount.
     */
    fun observeGuides(routeId: String): Flow<DataResult<List<Guide>>>

    /**
     * Submits a new guide under /routes/{routeId}/guides.
     * Returns the new guide document ID.
     */
    suspend fun submitGuide(
        routeId: String,
        guide: Guide,
    ): DataResult<String>

    suspend fun submitVerdict(
        routeId: String,
        userId: String,
        verdict: Verdict,
    ): DataResult<Unit>

    suspend fun getUserVerdict(
        routeId: String,
        userId: String,
    ): DataResult<Verdict?>
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

    override suspend fun getRouteStops(routeId: String): DataResult<List<RouteStop>> =
        routesDataSource.getRouteStops(routeId).toDataResult { dto ->
            dto?.toOutboundStops() ?: emptyList()
        }

    override fun observeGuides(routeId: String): Flow<DataResult<List<Guide>>> =
        routesDataSource.observeGuides(routeId).toDataResultFlow { dtos ->
            dtos.map { it.toDomain() }
        }

    override suspend fun submitGuide(
        routeId: String,
        guide: Guide,
    ): DataResult<String> = routesDataSource.submitGuide(routeId, guide.toDto()).toDataResult()

    override suspend fun submitVerdict(
        routeId: String,
        userId: String,
        verdict: Verdict,
    ): DataResult<Unit> = confirmationsDataSource.submitVerdict(routeId, userId, verdict).toDataResult()

    override suspend fun getUserVerdict(
        routeId: String,
        userId: String,
    ): DataResult<Verdict?> = confirmationsDataSource.getUserVerdict(routeId, userId).toDataResult()
}
