package com.samuelokello.mwenyeji.datasources.sources.routes.dto

import androidx.annotation.Keep
import com.samuelokello.mwenyeji.data.models.RouteStop

/**
 * DTO for /route_stops/{routeId} documents.
 * Loaded on demand when route detail screen opens — never during feed loading.
 */
@Keep
data class RouteStopsDto(
    val routeId: String = "",
    val outbound: List<Map<String, Any>> = emptyList(),
    val inbound: List<Map<String, Any>> = emptyList(),
) {
    fun toOutboundStops(): List<RouteStop> = outbound.mapNotNull { it.toRouteStop() }

    fun toInboundStops(): List<RouteStop> = inbound.mapNotNull { it.toRouteStop() }
}

private fun Map<String, Any>.toRouteStop(): RouteStop? {
    val stopId = this["stopId"] as? String ?: return null
    val name = this["name"] as? String ?: return null
    val lat = (this["lat"] as? Double) ?: return null
    val lng = (this["lng"] as? Double) ?: return null
    val sequence =
        (this["sequence"] as? Long)?.toInt()
            ?: (this["sequence"] as? Int)
            ?: return null
    return RouteStop(stopId = stopId, name = name, lat = lat, lng = lng, sequence = sequence)
}
