package com.samuelokello.mwenyeji.feature.planner.domain

import com.samuelokello.mwenyeji.data.helpers.ProximityEngine
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.TravelDirection
import com.samuelokello.mwenyeji.feature.guidance.service.TripRequest
import kotlin.math.roundToInt

/** A route with its stops in stored order. */
data class RouteLine(
    val route: Route,
    val stops: List<RouteStop>,
)

/**
 * One way to make the trip on a single matatu: walk to a stage, ride, get off, walk on.
 *
 * @param stops The route's stops in the direction of travel.
 */
data class TripOption(
    val route: Route,
    val direction: TravelDirection,
    val stops: List<RouteStop>,
    val boardingIndex: Int,
    val alightIndex: Int,
    val walkToStageMetres: Int,
    val walkFromStopMetres: Int,
    // From the route's best guide, when there is one.
    val sacco: String? = null,
    val fare: Int? = null,
) {
    val boardingStop: RouteStop get() = stops[boardingIndex]
    val alightStop: RouteStop get() = stops[alightIndex]
    val stopsToRide: Int get() = alightIndex - boardingIndex

    /** What Start guide hands the guidance service. [defaultVehicle] is said when no sacco is known. */
    fun toTripRequest(defaultVehicle: String): TripRequest =
        TripRequest(
            routeId = route.id,
            direction = direction,
            stops = stops,
            alightStopId = alightStop.stopId,
            boardingStopId = boardingStop.stopId,
            vehicleName = sacco?.takeIf { it.isNotBlank() } ?: defaultVehicle,
            fare = fare,
        )
}

/**
 * Direct trips between two places: pure Kotlin, no network, so it works offline on the routes and stops
 * already on the phone. Transfers come later.
 */
object TripPlanner {
    /** Further than this to a stage, on either end, and the route is not offered. */
    const val MAX_WALK_M = 1_200.0

    /** Closer than this, walking is the answer. */
    const val WALKABLE_M = 800.0

    // Each stop ridden weighs like this much walking, so of two equal walks the shorter ride wins.
    private const val STOP_WEIGHT_M = 40.0

    fun plan(
        from: Place,
        to: Place,
        lines: List<RouteLine>,
        limit: Int = 5,
    ): List<TripOption> =
        lines
            .mapNotNull { line ->
                listOfNotNull(
                    best(line, TravelDirection.OUTBOUND, line.stops, from, to),
                    best(line, TravelDirection.INBOUND, line.stops.reversed(), from, to),
                ).minByOrNull { it.second }
            }.sortedBy { it.second }
            .take(limit)
            .map { it.first }

    /** The best boarding and alighting pair on [stops] (in travel order), with its cost. */
    private fun best(
        line: RouteLine,
        direction: TravelDirection,
        stops: List<RouteStop>,
        from: Place,
        to: Place,
    ): Pair<TripOption, Double>? {
        if (stops.size < 2) return null
        val walkTo = stops.map { metres(from, it) }
        val walkFrom = stops.map { metres(to, it) }

        // For each alight stop j, the best stage i before it: cost = walkTo[i] + walkFrom[j] + (j - i) * weight.
        var bestStage = -1
        var bestStageScore = Double.MAX_VALUE
        var best: Triple<Int, Int, Double>? = null
        for (j in stops.indices) {
            if (j > 0) {
                val i = j - 1
                val score = walkTo[i] - i * STOP_WEIGHT_M
                if (walkTo[i] <= MAX_WALK_M && score < bestStageScore) {
                    bestStage = i
                    bestStageScore = score
                }
            }
            if (bestStage >= 0 && walkFrom[j] <= MAX_WALK_M) {
                val cost = walkTo[bestStage] + walkFrom[j] + (j - bestStage) * STOP_WEIGHT_M
                if (best == null || cost < best.third) best = Triple(bestStage, j, cost)
            }
        }
        val (i, j, cost) = best ?: return null
        return TripOption(
            route = line.route,
            direction = direction,
            stops = stops,
            boardingIndex = i,
            alightIndex = j,
            walkToStageMetres = walkTo[i].roundToTens(),
            walkFromStopMetres = walkFrom[j].roundToTens(),
        ) to cost
    }

    fun metres(
        place: Place,
        stop: RouteStop,
    ): Double = ProximityEngine.haversineKm(place.lat, place.lng, stop.lat, stop.lng) * 1000

    fun metres(
        a: Place,
        b: Place,
    ): Double = ProximityEngine.haversineKm(a.lat, a.lng, b.lat, b.lng) * 1000

    private fun Double.roundToTens(): Int = (this / 10).roundToInt() * 10
}
