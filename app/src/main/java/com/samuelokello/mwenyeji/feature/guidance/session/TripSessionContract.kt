package com.samuelokello.mwenyeji.feature.guidance.session

import com.samuelokello.mwenyeji.data.models.GeoPoint
import com.samuelokello.mwenyeji.data.models.RouteStop

interface TripSessionContract {
    /**
     * What the user chose before tapping Start. Does not change during the trip.
     *
     * @param stops         Route stops ordered in the direction of travel.
     * @param boardingIndex Index in [stops] of the stage the user walks to.
     * @param alightIndex   Index in [stops] of the stop the user gets off at.
     * @param knownPaths    Roads matatus on this route are known to take besides the main line,
     *                      learned from riders' traces. A bus on one of them is on route.
     * @param road          The road the route follows in this direction, from the route data. Empty
     *                      when none is stored: the main line is then the straight lines between stops.
     * @param stageChoices  Indexes in [stops] of the stages within walking distance when the trip
     *                      started, nearest first. Stops are not always where the stage is, so the
     *                      rider may pick another one.
     */
    data class TripPlan(
        val stops: List<RouteStop>,
        val boardingIndex: Int,
        val alightIndex: Int,
        val vehicleName: String,
        val fare: Int? = null,
        // Null when the phone had no location yet when the trip started.
        val walkingDistanceMetres: Int?,
        val knownPaths: List<List<GeoPoint>> = emptyList(),
        val road: List<GeoPoint> = emptyList(),
        val stageChoices: List<Int> = emptyList(),
    ) {
        val stage: RouteStop get() = stops[boardingIndex]
        val alightStop: RouteStop get() = stops[alightIndex]
        val stopsToRide: Int get() = alightIndex - boardingIndex

        /** The line the route runs along: its road, or the straight lines between its stops. */
        val mainLine: List<GeoPoint> get() = road.ifEmpty { stops.map { GeoPoint(it.lat, it.lng) } }
    }

    data class TripState(
        val currentPhase: TripPhase = TripPhase.PLANNED,
        // How many fixes in a row each guard has matched. Cleared on every phase change.
        val guardStreaks: Map<TripGuard, Int> = emptyMap(),
        // When the current timed guard (walking away, off the bus) started matching.
        val currentStreakTime: Long? = null,
        val previousDistanceToStage: Double? = null,
        // Distance to the stage at the last walk update (or at the first fix of the walk).
        val lastWalkUpdateMetres: Double? = null,
        // Distance to the alight stop when the current detour began. Null while on route.
        val detourStartToStopMetres: Double? = null,
        val shortcutNoticeGiven: Boolean = false,
        val wrongBusWarningGiven: Boolean = false,
        val gpsLostWarningGiven: Boolean = false,
        // Where the last fix put the rider, so a tap can tell which stop they are at.
        val lastPosition: GeoPoint? = null,
        // Distance to the alight stop when the rider began moving at vehicle speed along the route
        // before boarding was noticed. Null while they are not.
        val movingStartToStopMetres: Double? = null,
        // The stop nearest the rider when that began.
        val movingStartStage: Int? = null,
    )

    sealed interface TripEvent {
        data class UserTapsStart(
            val locationPermissionGranted: Boolean,
        ) : TripEvent

        /** Allowed before the stage is reached too: the rider may board at another stage. */
        data object UserTapsIOnBoard : TripEvent

        /** The stop on the map is not where the stage is, and the rider is at the stage. */
        data object UserTapsAtStage : TripEvent

        /** The rider would rather board at the stop at [stopIndex] in the plan's stops. */
        data class UserPicksStage(
            val stopIndex: Int,
        ) : TripEvent

        data object UserTapsIHaveAlighted : TripEvent

        /** @param speed metres per second, as reported by Android's Location. */
        data class LocationFix(
            val lat: Double,
            val lng: Double,
            val speed: Double,
            val accuracy: Double,
            val elapsedMillis: Long,
        ) : TripEvent

        data object UserTapsCancel : TripEvent

        /** Sent by the location source after [TripThresholds.GPS_LOST_TIMEOUT_MS] without a fix. */
        data object GpsLost : TripEvent

        /** Sent [TripThresholds.MAX_TRIP_MS] after Start. No trip takes this long; something went wrong. */
        data object TripExpired : TripEvent
    }

    sealed interface TripEffect {
        data class Speak(
            val message: Message,
        ) : TripEffect

        data class Vibrate(
            val pattern: VibrationPattern,
        ) : TripEffect

        /** The rider boards at the stop at [boardingIndex] instead: the plan changes to match. */
        data class MoveStage(
            val boardingIndex: Int,
        ) : TripEffect

        data object RequestLocationPermission : TripEffect
        data object EndTrip : TripEffect
    }
}
