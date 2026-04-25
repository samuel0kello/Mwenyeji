// feature/feed/FeedSorting.kt
package com.samuelokello.mwenyeji.data.helpers

import com.samuelokello.mwenyeji.data.models.Route
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Sort by haversine distance from (userLat, userLng) to each route's origin.
 * Routes without coordinates are sorted to the end, preserving original relative order.
 */
internal fun List<Route>.sortedByProximity(userLat: Double?, userLng: Double?): List<Route> {
    if (userLat == null || userLng == null) return this
    return sortedBy { route ->
        val lat = route.fromLat
        val lng = route.fromLng
        if (lat == null || lng == null) {
            Double.POSITIVE_INFINITY
        } else {
            haversineKm(userLat, userLng, lat, lng)
        }
    }
}

internal fun List<Route>.filterBy(query: String): List<Route> {
    if (query.isBlank()) return this
    val trimmed = query.trim()
    return filter {
        it.from.contains(trimmed, ignoreCase = true) ||
            it.to.contains(trimmed, ignoreCase = true) ||
            it.via.contains(trimmed, ignoreCase = true)
    }
}

private const val EARTH_RADIUS_KM = 6371.0

private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a =
        sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLng / 2).pow(2)
    return EARTH_RADIUS_KM * 2 * asin(sqrt(a))
}
