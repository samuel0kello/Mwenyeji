package com.samuelokello.mwenyeji.data.repository

import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.toDataResult
import com.samuelokello.mwenyeji.data.models.GeoPoint
import com.samuelokello.mwenyeji.data.models.RideTrace
import com.samuelokello.mwenyeji.data.models.TravelDirection
import com.samuelokello.mwenyeji.datasources.sources.learning.RouteLearningRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.learning.dto.RideTraceDto

/**
 * Riders' traces go up; the roads learned from them (seeder/route_learning) come back down as extra
 * paths the trip session counts as on route.
 */
interface RouteLearningRepository {
    suspend fun uploadRideTrace(trace: RideTrace): DataResult<Unit>

    /** Each learned variant as a path in the direction of travel. */
    suspend fun getKnownPaths(
        routeId: String,
        direction: TravelDirection,
    ): DataResult<List<List<GeoPoint>>>
}

class RouteLearningRepositoryImpl(
    private val dataSource: RouteLearningRemoteDataSource,
) : RouteLearningRepository {
    override suspend fun uploadRideTrace(trace: RideTrace): DataResult<Unit> =
        dataSource
            .uploadRideTrace(
                RideTraceDto(
                    routeId = trace.routeId,
                    direction = trace.direction.key,
                    boardingStopId = trace.boardingStopId,
                    alightStopId = trace.alightStopId,
                    weekday = trace.weekday,
                    hour = trace.hour,
                    points = trace.points.map { mapOf("lat" to it.lat, "lng" to it.lng, "t" to it.secondsFromStart, "v" to it.speedMps) },
                ),
            ).toDataResult()

    override suspend fun getKnownPaths(
        routeId: String,
        direction: TravelDirection,
    ): DataResult<List<List<GeoPoint>>> =
        dataSource.getRouteVariants(routeId, direction.key).toDataResult { dtos ->
            dtos.map { dto -> dto.points.mapNotNull { it.toGeoPoint() } }.filter { it.size >= 2 }
        }
}

private fun Map<String, Any>.toGeoPoint(): GeoPoint? {
    val lat = (this["lat"] as? Number)?.toDouble() ?: return null
    val lng = (this["lng"] as? Number)?.toDouble() ?: return null
    return GeoPoint(lat, lng)
}
