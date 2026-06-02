package com.samuelokello.mwenyeji.data.helpers

import com.samuelokello.mwenyeji.data.models.BoardableRoute
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.TripDirection
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Converts GTFS routes + their stop lists into BoardableRoute objects
 * relative to the user's current location.
 *
 * Core logic:
 *   1. For each stop on a route, check if it is within BOARDING_RADIUS_KM of user.
 *   2. If yes, that stop is a valid boarding point.
 *   3. The route is boardable in the direction that has stops REMAINING after
 *      the boarding stop — the direction going AWAY from the user.
 *   4. Routes where the user is AT the terminus (no stops ahead in that direction)
 *      are skipped — the matatu terminates there, it does not continue.
 *
 * A route produces two BoardableRoute entries (one per direction) when the user
 * is near a middle stop — both are valid boarding options shown in the feed.
 */
object ProximityEngine {
    /**
     * Radius within which a stop is considered reachable on foot.
     * 500m ≈ 6-minute walk — acceptable for a matatu search.
     */
    private const val BOARDING_RADIUS_KM = 0.5

    /**
     * Minimum stops remaining for a direction to be worth showing.
     * Filters out routes where the user is one stop from the terminus.
     */
    private const val MIN_STOPS_REMAINING = 1

    /**
     * Computes all BoardableRoute entries for a given user location.
     *
     * @param routes     All GTFS routes loaded from Firestore.
     * @param stopsCache routeId → ordered outbound stop list.
     *                   Inbound is derived by reversing the outbound list.
     * @param userLat    User's current latitude.
     * @param userLng    User's current longitude.
     * @return BoardableRoutes sorted by walking distance to boarding stop.
     */
    fun computeBoardable(
        routes: List<Route>,
        stopsCache: Map<String, List<RouteStop>>,
        userLat: Double,
        userLng: Double,
    ): List<BoardableRoute> {
        val results = mutableListOf<BoardableRoute>()

        for (route in routes) {
            val outboundStops = stopsCache[route.id] ?: continue
            if (outboundStops.isEmpty()) continue

            // Outbound direction: stops as stored in route_stops
            findBoardingPoint(outboundStops, userLat, userLng)?.let {
                results.add(
                    BoardableRoute(
                        route = route,
                        boardingStop = it.stop,
                        walkingDistanceKm = it.distanceKm,
                        onwardTerminus = it.terminus,
                        stopsRemaining = it.remaining,
                        tripDirection = TripDirection.OUTBOUND,
                    ),
                )
            }

            // Inbound direction: reverse the outbound stop list.
            // Matatus run both ways — a user near any stop can board going either direction.
            findBoardingPoint(outboundStops.reversed(), userLat, userLng)?.let {
                results.add(
                    BoardableRoute(
                        route = route,
                        boardingStop = it.stop,
                        walkingDistanceKm = it.distanceKm,
                        onwardTerminus = it.terminus,
                        stopsRemaining = it.remaining,
                        tripDirection = TripDirection.INBOUND,
                    ),
                )
            }
        }

        return results.sortedBy { it.walkingDistanceKm }
    }

    /**
     * Finds the nearest stop within [BOARDING_RADIUS_KM] that has at least
     * [MIN_STOPS_REMAINING] stops ahead of it in the given stop sequence.
     *
     * Returns null if no boardable stop exists within radius.
     */
    private fun findBoardingPoint(stops: List<RouteStop>, userLat: Double, userLng: Double): BoardingPoint? {
        var nearestStop: RouteStop? = null
        var nearestDist = Double.MAX_VALUE
        var nearestIndex = -1

        for ((index, stop) in stops.withIndex()) {
            val dist = haversineKm(userLat, userLng, stop.lat, stop.lng)
            if (dist < nearestDist && dist <= BOARDING_RADIUS_KM) {
                nearestDist = dist
                nearestStop = stop
                nearestIndex = index
            }
        }

        if (nearestStop == null || nearestIndex < 0) return null

        val stopsAhead = stops.size - nearestIndex - 1
        if (stopsAhead < MIN_STOPS_REMAINING) return null

        return BoardingPoint(
            stop = nearestStop,
            distanceKm = nearestDist,
            remaining = stopsAhead,
            terminus = stops.last().name,
        )
    }

    private data class BoardingPoint(
        val stop: RouteStop,
        val distanceKm: Double,
        val remaining: Int,
        val terminus: String,
    )

    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a =
            sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        return r * 2 * asin(sqrt(a))
    }
}
