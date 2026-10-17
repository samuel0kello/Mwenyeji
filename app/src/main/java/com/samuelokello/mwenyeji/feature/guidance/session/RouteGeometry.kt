package com.samuelokello.mwenyeji.feature.guidance.session

import com.samuelokello.mwenyeji.data.helpers.ProximityEngine
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripEvent
import kotlin.math.cos
import kotlin.math.hypot

internal fun distanceMetres(
    fix: TripEvent.LocationFix,
    stop: RouteStop,
): Double = ProximityEngine.haversineKm(fix.lat, fix.lng, stop.lat, stop.lng) * 1000

/**
 * Distance from the fix to the line through the stops, in metres.
 * There are no route shapes, so straight lines between consecutive stops stand in for the road.
 */
internal fun distanceToRouteMetres(
    fix: TripEvent.LocationFix,
    stops: List<RouteStop>,
): Double {
    if (stops.size < 2) return stops.minOfOrNull { distanceMetres(fix, it) } ?: 0.0

    // Flat projection centred on the fix. Accurate enough at the few-km scale of a route.
    val metresPerDegree = 111_320.0
    val lngScale = cos(Math.toRadians(fix.lat))
    fun x(stop: RouteStop) = (stop.lng - fix.lng) * lngScale * metresPerDegree
    fun y(stop: RouteStop) = (stop.lat - fix.lat) * metresPerDegree

    return stops.zipWithNext().minOf { (a, b) ->
        val ax = x(a)
        val ay = y(a)
        val dx = x(b) - ax
        val dy = y(b) - ay
        val lengthSquared = dx * dx + dy * dy
        val t = if (lengthSquared == 0.0) 0.0 else (-(ax * dx + ay * dy) / lengthSquared).coerceIn(0.0, 1.0)
        hypot(ax + t * dx, ay + t * dy)
    }
}
