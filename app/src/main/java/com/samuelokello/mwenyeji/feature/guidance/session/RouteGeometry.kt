package com.samuelokello.mwenyeji.feature.guidance.session

import com.samuelokello.mwenyeji.data.helpers.ProximityEngine
import com.samuelokello.mwenyeji.data.models.GeoPoint
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripEvent
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

internal fun distanceMetres(
    fix: TripEvent.LocationFix,
    stop: RouteStop,
): Double = ProximityEngine.haversineKm(fix.lat, fix.lng, stop.lat, stop.lng) * 1000

/** Compass bearing from the fix to [stop], in degrees clockwise from north. */
internal fun bearingDegrees(
    fix: TripEvent.LocationFix,
    stop: RouteStop,
): Double {
    val lat1 = Math.toRadians(fix.lat)
    val lat2 = Math.toRadians(stop.lat)
    val dLng = Math.toRadians(stop.lng - fix.lng)
    val y = sin(dLng) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
    return Math.toDegrees(atan2(y, x))
}

/** Distance from the fix to the nearest of [paths], in metres. Infinite when there are none. */
internal fun distanceToPathsMetres(
    fix: TripEvent.LocationFix,
    paths: List<List<GeoPoint>>,
): Double = paths.minOfOrNull { distanceToPathMetres(fix, it) } ?: Double.POSITIVE_INFINITY

/**
 * Distance from the fix to the polyline through [points], in metres. A route without a road shape is
 * the straight lines between its stops.
 */
internal fun distanceToPathMetres(
    fix: TripEvent.LocationFix,
    points: List<GeoPoint>,
): Double {
    if (points.isEmpty()) return Double.POSITIVE_INFINITY

    // Flat projection centred on the fix. Accurate enough at the few-km scale of a route.
    val metresPerDegree = 111_320.0
    val lngScale = cos(Math.toRadians(fix.lat))
    fun x(point: GeoPoint) = (point.lng - fix.lng) * lngScale * metresPerDegree
    fun y(point: GeoPoint) = (point.lat - fix.lat) * metresPerDegree

    if (points.size == 1) return hypot(x(points[0]), y(points[0]))

    return points.zipWithNext().minOf { (a, b) ->
        val ax = x(a)
        val ay = y(a)
        val dx = x(b) - ax
        val dy = y(b) - ay
        val lengthSquared = dx * dx + dy * dy
        val t = if (lengthSquared == 0.0) 0.0 else (-(ax * dx + ay * dy) / lengthSquared).coerceIn(0.0, 1.0)
        hypot(ax + t * dx, ay + t * dy)
    }
}
