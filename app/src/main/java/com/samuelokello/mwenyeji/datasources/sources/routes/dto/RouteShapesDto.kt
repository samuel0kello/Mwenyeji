package com.samuelokello.mwenyeji.datasources.sources.routes.dto

import androidx.annotation.Keep
import com.samuelokello.mwenyeji.data.models.GeoPoint

/**
 * DTO for /route_shapes/{routeId} documents: the road each direction follows, from the GTFS shapes.
 * Points are stored flat (lat, lng, lat, lng, …) to keep the documents small.
 */
@Keep
data class RouteShapesDto(
    val routeId: String = "",
    val outbound: List<Double> = emptyList(),
    val inbound: List<Double> = emptyList(),
) {
    fun toOutboundRoad(): List<GeoPoint> = outbound.toPoints()

    fun toInboundRoad(): List<GeoPoint> = inbound.toPoints()
}

private fun List<Double>.toPoints(): List<GeoPoint> = chunked(2).filter { it.size == 2 }.map { (lat, lng) -> GeoPoint(lat, lng) }
