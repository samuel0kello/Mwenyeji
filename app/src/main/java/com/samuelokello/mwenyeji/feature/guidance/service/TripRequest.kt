package com.samuelokello.mwenyeji.feature.guidance.service

import android.content.Intent
import com.samuelokello.mwenyeji.data.helpers.ProximityEngine
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.TravelDirection
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripPlan
import kotlin.math.roundToInt

/**
 * What a guide's Start button hands to the guidance service.
 *
 * @param routeId        The route the guide belongs to.
 * @param direction      Which way along the route the trip runs.
 * @param stops          Route stops in the direction of travel.
 * @param alightStopId   The stop the user is going to.
 * @param boardingStopId The stage to board at, or null to use the stop nearest the user when the trip starts.
 */
data class TripRequest(
    val routeId: String,
    val direction: TravelDirection,
    val stops: List<RouteStop>,
    val alightStopId: String,
    val boardingStopId: String?,
    val vehicleName: String,
    val fare: Int?,
) {
    /**
     * Builds the plan once the user's position is known ([hereLat], [hereLng] are null when it is not).
     * Without a named stage, the user boards at the stop nearest them, before the alight stop.
     */
    fun toPlan(
        hereLat: Double?,
        hereLng: Double?,
    ): TripPlan {
        val alight = stops.indexOfFirst { it.stopId == alightStopId }
        require(alight > 0) { "The alight stop must come after at least one stop" }
        val candidates = 0 until alight

        val named = stops.indexOfFirst { it.stopId == boardingStopId }.takeIf { it in candidates }
        val nearest =
            if (hereLat != null && hereLng != null) {
                candidates.minBy { metresBetween(hereLat, hereLng, stops[it]) }
            } else {
                null
            }
        val boarding = named ?: nearest ?: 0

        return TripPlan(
            stops = stops,
            boardingIndex = boarding,
            alightIndex = alight,
            vehicleName = vehicleName,
            fare = fare,
            walkingDistanceMetres =
                if (hereLat != null && hereLng != null) {
                    (metresBetween(hereLat, hereLng, stops[boarding]) / 10).roundToInt() * 10
                } else {
                    null
                },
        )
    }

    companion object {
        /**
         * Orders [routeStops] (as stored) for a trip from [from] to [to], the names the user searched or
         * tapped. A name matching a stop pins the stage or the stop.
         *
         * The direction comes from where those stops sit in the list, because the stored order and a
         * route's from/to label often disagree. [labelSaysReverse] is only used when the names match no
         * stops, so the order cannot tell.
         */
        fun forDirection(
            routeId: String,
            routeStops: List<RouteStop>,
            from: String?,
            to: String?,
            labelSaysReverse: Boolean,
            vehicleName: String,
            fare: Int?,
        ): TripRequest? {
            if (routeStops.size < 2) return null
            val reverse = runsReversed(routeStops, from, to) ?: labelSaysReverse
            val stops = if (reverse) routeStops.reversed() else routeStops
            val alight = stops.indexOfLast { it.matches(to) }.takeIf { it > 0 } ?: stops.lastIndex
            val boarding = stops.indexOfFirst { it.matches(from) }.takeIf { it in 0 until alight }
            return TripRequest(
                routeId = routeId,
                direction = if (reverse) TravelDirection.INBOUND else TravelDirection.OUTBOUND,
                stops = stops,
                alightStopId = stops[alight].stopId,
                boardingStopId = boarding?.let { stops[it].stopId },
                vehicleName = vehicleName,
                fare = fare,
            )
        }

        /**
         * True when a trip from [from] to [to] runs against the stored stop order, false when it runs
         * with it, null when the names do not both match stops.
         */
        fun runsReversed(
            routeStops: List<RouteStop>,
            from: String?,
            to: String?,
        ): Boolean? {
            val fromIndex = routeStops.indexOfFirst { it.matches(from) }
            val toIndex = routeStops.indexOfFirst { it.matches(to) }
            return if (fromIndex < 0 || toIndex < 0 || fromIndex == toIndex) null else fromIndex > toIndex
        }
    }
}

/**
 * Stop names are written many ways ("Marigu-ini", "Mariguini", "Kikuyu Terminus"), so compare only
 * letters and digits, ignoring case.
 */
internal fun RouteStop.matches(place: String?): Boolean {
    val wanted = place?.normalised()?.takeIf { it.isNotEmpty() } ?: return false
    return name.normalised() == wanted
}

private fun String.normalised(): String = lowercase().filter { it.isLetterOrDigit() }

private fun metresBetween(
    lat: Double,
    lng: Double,
    stop: RouteStop,
): Double = ProximityEngine.haversineKm(lat, lng, stop.lat, stop.lng) * 1000

// Stops travel as parallel arrays because RouteStop is not Parcelable.
private const val EXTRA_ROUTE_ID = "routeId"
private const val EXTRA_DIRECTION = "direction"
private const val EXTRA_STOP_IDS = "stopIds"
private const val EXTRA_STOP_NAMES = "stopNames"
private const val EXTRA_STOP_LATS = "stopLats"
private const val EXTRA_STOP_LNGS = "stopLngs"
private const val EXTRA_BOARDING_ID = "boardingStopId"
private const val EXTRA_ALIGHT_ID = "alightStopId"
private const val EXTRA_VEHICLE = "vehicleName"
private const val EXTRA_FARE = "fare"

internal fun Intent.putTripRequest(request: TripRequest): Intent =
    apply {
        putExtra(EXTRA_ROUTE_ID, request.routeId)
        putExtra(EXTRA_DIRECTION, request.direction.name)
        putExtra(EXTRA_STOP_IDS, request.stops.map { it.stopId }.toTypedArray())
        putExtra(EXTRA_STOP_NAMES, request.stops.map { it.name }.toTypedArray())
        putExtra(EXTRA_STOP_LATS, request.stops.map { it.lat }.toDoubleArray())
        putExtra(EXTRA_STOP_LNGS, request.stops.map { it.lng }.toDoubleArray())
        request.boardingStopId?.let { putExtra(EXTRA_BOARDING_ID, it) }
        putExtra(EXTRA_ALIGHT_ID, request.alightStopId)
        putExtra(EXTRA_VEHICLE, request.vehicleName)
        request.fare?.let { putExtra(EXTRA_FARE, it) }
    }

internal fun Intent.getTripRequest(): TripRequest? {
    val ids = getStringArrayExtra(EXTRA_STOP_IDS) ?: return null
    val names = getStringArrayExtra(EXTRA_STOP_NAMES) ?: return null
    val lats = getDoubleArrayExtra(EXTRA_STOP_LATS) ?: return null
    val lngs = getDoubleArrayExtra(EXTRA_STOP_LNGS) ?: return null
    return TripRequest(
        routeId = getStringExtra(EXTRA_ROUTE_ID) ?: return null,
        direction = getStringExtra(EXTRA_DIRECTION)?.let { name -> TravelDirection.entries.firstOrNull { it.name == name } } ?: return null,
        stops = ids.indices.map { RouteStop(ids[it], names[it], lats[it], lngs[it], it) },
        boardingStopId = getStringExtra(EXTRA_BOARDING_ID),
        alightStopId = getStringExtra(EXTRA_ALIGHT_ID) ?: return null,
        vehicleName = getStringExtra(EXTRA_VEHICLE) ?: return null,
        fare = if (hasExtra(EXTRA_FARE)) getIntExtra(EXTRA_FARE, 0) else null,
    )
}
