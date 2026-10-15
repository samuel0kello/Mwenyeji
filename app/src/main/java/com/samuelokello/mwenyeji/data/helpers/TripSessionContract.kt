package com.samuelokello.mwenyeji.data.helpers

import com.samuelokello.mwenyeji.data.helpers.TripSessionContract.TripEffect
import com.samuelokello.mwenyeji.data.helpers.TripSessionContract.TripEvent
import com.samuelokello.mwenyeji.data.helpers.TripSessionContract.TripPlan
import com.samuelokello.mwenyeji.data.helpers.TripSessionContract.TripState
import com.samuelokello.mwenyeji.data.models.RouteStop
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

interface TripSessionContract {
    /**
     * What the user chose before tapping Start. Does not change during the trip.
     *
     * @param stops         Route stops ordered in the direction of travel.
     * @param boardingIndex Index in [stops] of the stage the user walks to.
     * @param alightIndex   Index in [stops] of the stop the user gets off at.
     */
    data class TripPlan(
        val stops: List<RouteStop>,
        val boardingIndex: Int,
        val alightIndex: Int,
        val vehicleName: String,
        val fare: Int? = null,
        val walkingDistanceMetres: Int,
    ) {
        val stage: RouteStop get() = stops[boardingIndex]
        val alightStop: RouteStop get() = stops[alightIndex]
        val stopsToRide: Int get() = alightIndex - boardingIndex
    }

    data class TripState(
        val currentPhase: TripPhase = TripPhase.PLANNED,
        // How many fixes in a row each guard has matched. Cleared on every phase change.
        val guardStreaks: Map<TripGuard, Int> = emptyMap(),
        // When the current timed guard (walking away, off the bus) started matching.
        val currentStreakTime: Instant? = null,
        val previousDistanceToStage: Double? = null,
        val wrongBusWarningGiven: Boolean = false,
        val gpsLostWarningGiven: Boolean = false,
    )

    sealed interface TripEvent {
        data class UserTapsStart(
            val locationPermissionGranted: Boolean,
        ) : TripEvent
        data object UserTapsIOnBoard : TripEvent
        data object UserTapsIHaveAlighted : TripEvent

        /** @param speed metres per second, as reported by Android's Location. */
        data class LocationFix(
            val lat: Double,
            val lng: Double,
            val speed: Double,
            val accuracy: Double,
            val timeStamp: Instant,
        ) : TripEvent
        data object UserTapsCancel : TripEvent

        /** Sent by the location source after [TripThresholds.GPS_LOST_TIMEOUT] without a fix. */
        data object GpsLost : TripEvent
    }

    sealed interface TripEffect {
        data class Speak(
            val message: Message,
        ) : TripEffect
        data class Vibrate(
            val pattern: VibrationPattern,
        ) : TripEffect
        data object RequestLocationPermission : TripEffect
        data object EndTrip : TripEffect
    }
}

/** Starting guesses. Tune them on real trips. */
object TripThresholds {
    const val STAGE_RADIUS_M = 30.0
    const val BOARDED_DISTANCE_M = 100.0
    const val GET_READY_DISTANCE_M = 800.0
    const val ALIGHT_DISTANCE_M = 300.0
    const val ARRIVAL_RADIUS_M = 50.0
    const val MISSED_STOP_DISTANCE_M = 150.0
    const val WRONG_BUS_DISTANCE_M = 300.0
    const val VEHICLE_SPEED_MPS = 15 / 3.6
    const val WALKING_SPEED_MPS = 7 / 3.6
    val OFF_ROUTE_TIME = 30.seconds
    val OFF_BUS_TIME = 60.seconds
    val GPS_LOST_TIMEOUT = 60.seconds
}

/**
 * Pure transition function for a guided trip. See StateMachine.md for the table this implements.
 * If no guard matches, the state is returned unchanged with no effects.
 */
internal fun tripSessionMachine(
    plan: TripPlan,
    currentState: TripState,
    event: TripEvent,
): Pair<TripState, List<TripEffect>> {
    val phase = currentState.currentPhase
    if (phase.isFinal) return currentState.unchanged()

    return when (event) {
        is TripEvent.UserTapsStart -> when {
            phase != TripPhase.PLANNED -> currentState.unchanged()

            event.locationPermissionGranted ->
                currentState.enter(TripPhase.WALKING_TO_STAGE) to say(
                    "Walk ${plan.walkingDistanceMetres}m to ${plan.stage.name} stage",
                    VibrationPattern.TICK,
                )

            else -> currentState to listOf(TripEffect.RequestLocationPermission)
        }

        TripEvent.UserTapsIOnBoard ->
            if (phase == TripPhase.AT_STAGE) {
                currentState.enter(TripPhase.ON_BOARD) to say(
                    "Sawa, ${plan.stopsToRide} stops to ${plan.alightStop.name}. I'll tell you when to get off",
                    VibrationPattern.TICK,
                )
            } else {
                currentState.unchanged()
            }

        TripEvent.UserTapsIHaveAlighted ->
            if (phase.isRiding || phase == TripPhase.MISSED_STOP) {
                currentState.enter(TripPhase.ARRIVED) to
                    say("Trip ended", VibrationPattern.MILESTONE) + TripEffect.EndTrip
            } else {
                currentState.unchanged()
            }

        TripEvent.UserTapsCancel ->
            currentState.enter(TripPhase.CANCELLED) to
                say("Guidance stopped", VibrationPattern.TICK) + TripEffect.EndTrip

        TripEvent.GpsLost ->
            // Nothing is tracking the user before Start, so there is nothing to warn about.
            if (phase == TripPhase.PLANNED || currentState.gpsLostWarningGiven) {
                currentState.unchanged()
            } else {
                currentState.copy(gpsLostWarningGiven = true) to
                    say("I've lost your location, watch for your stop", VibrationPattern.WARNING)
            }

        is TripEvent.LocationFix -> {
            val state = currentState.copy(gpsLostWarningGiven = false)
            when (phase) {
                TripPhase.WALKING_TO_STAGE -> walkingToStage(plan, state, event)

                TripPhase.OFF_ROUTE -> offRoute(plan, state, event)

                TripPhase.AT_STAGE -> atStage(plan, state, event)

                TripPhase.ON_BOARD,
                TripPhase.APPROACHING_STOP,
                TripPhase.ALIGHT_NOW,
                -> riding(plan, state, event)

                TripPhase.MISSED_STOP -> missedStop(plan, state, event)

                TripPhase.PLANNED,
                TripPhase.ARRIVED,
                TripPhase.CANCELLED,
                -> state.unchanged()
            }
        }
    }
}

private fun walkingToStage(
    plan: TripPlan,
    state: TripState,
    fix: TripEvent.LocationFix,
): Pair<TripState, List<TripEffect>> {
    val distance = distanceMetres(fix, plan.stage)

    val atStage = state.streak(TripGuard.REACHED_STAGE, distance <= TripThresholds.STAGE_RADIUS_M)
    if (atStage.count(TripGuard.REACHED_STAGE) >= 3) {
        return atStage.enter(TripPhase.AT_STAGE) to say(
            "You're at the stage. Look for ${plan.vehicleName} to ${plan.alightStop.name}" +
                (plan.fare?.let { ", fare around $it" } ?: ""),
            VibrationPattern.MILESTONE,
        )
    }

    val previous = state.previousDistanceToStage
    val walkingAwaySince =
        if (previous != null && distance > previous) state.currentStreakTime ?: fix.timeStamp else null
    if (walkingAwaySince != null && fix.timeStamp - walkingAwaySince >= TripThresholds.OFF_ROUTE_TIME) {
        return atStage.enter(TripPhase.OFF_ROUTE).copy(previousDistanceToStage = distance) to
            say("You're walking away from the stage", VibrationPattern.WARNING)
    }

    return atStage.copy(
        previousDistanceToStage = distance,
        currentStreakTime = walkingAwaySince,
    ) to emptyList()
}

private fun offRoute(
    plan: TripPlan,
    state: TripState,
    fix: TripEvent.LocationFix,
): Pair<TripState, List<TripEffect>> {
    val distance = distanceMetres(fix, plan.stage)
    val previous = state.previousDistanceToStage

    val next = state
        .streak(TripGuard.BACK_ON_TRACK, previous != null && distance < previous)
        .copy(previousDistanceToStage = distance)
    if (next.count(TripGuard.BACK_ON_TRACK) >= 3) {
        return next.enter(TripPhase.WALKING_TO_STAGE).copy(previousDistanceToStage = distance) to
            say("Back on track", VibrationPattern.TICK)
    }
    return next to emptyList()
}

private fun atStage(
    plan: TripPlan,
    state: TripState,
    fix: TripEvent.LocationFix,
): Pair<TripState, List<TripEffect>> {
    val moving = fix.speed > TripThresholds.VEHICLE_SPEED_MPS &&
        distanceMetres(fix, plan.stage) > TripThresholds.BOARDED_DISTANCE_M

    val next = state.streak(TripGuard.BOARDED, moving)
    if (next.count(TripGuard.BOARDED) >= 3) {
        return next.enter(TripPhase.ON_BOARD) to
            say("Looks like you're on the move. I'll tell you when to get off", VibrationPattern.TICK)
    }
    return next to emptyList()
}

/** ON_BOARD, APPROACHING_STOP and ALIGHT_NOW. The missed-stop check wins over everything else. */
private fun riding(
    plan: TripPlan,
    state: TripState,
    fix: TripEvent.LocationFix,
): Pair<TripState, List<TripEffect>> {
    val stopName = plan.alightStop.name
    val toStop = distanceMetres(fix, plan.alightStop)
    val nearestIndex = plan.stops.indices.minBy { distanceMetres(fix, plan.stops[it]) }
    // "Past the stop": the nearest stop on the route comes after the alight stop.
    val pastStop = nearestIndex > plan.alightIndex

    var next = state.streak(
        TripGuard.MISSED_STOP,
        pastStop && toStop > TripThresholds.MISSED_STOP_DISTANCE_M && fix.speed > TripThresholds.VEHICLE_SPEED_MPS,
    )
    if (next.count(TripGuard.MISSED_STOP) >= 3) {
        return next.enter(TripPhase.MISSED_STOP) to say(
            "You've passed your stop. Ask to be dropped now if it's safe, otherwise at the next stage",
            VibrationPattern.ALIGHT,
        )
    }

    when (state.currentPhase) {
        TripPhase.ON_BOARD -> {
            next = next.streak(
                TripGuard.APPROACHING,
                toStop <= TripThresholds.GET_READY_DISTANCE_M || nearestIndex >= plan.alightIndex - 1,
            )
            if (next.count(TripGuard.APPROACHING) >= 3) {
                return next.enter(TripPhase.APPROACHING_STOP) to
                    say("Get ready, $stopName is coming up", VibrationPattern.GET_READY)
            }

            next = next.streak(
                TripGuard.WRONG_BUS,
                distanceToRouteMetres(fix, plan.stops) > TripThresholds.WRONG_BUS_DISTANCE_M,
            )
            if (next.count(TripGuard.WRONG_BUS) >= 5 && !next.wrongBusWarningGiven) {
                return next.copy(wrongBusWarningGiven = true) to say(
                    "This bus may not be going to $stopName, check with the conductor",
                    VibrationPattern.WARNING,
                )
            }
        }

        TripPhase.APPROACHING_STOP -> {
            next = next.streak(TripGuard.ALIGHT, toStop <= TripThresholds.ALIGHT_DISTANCE_M)
            if (next.count(TripGuard.ALIGHT) >= 2) {
                return next.enter(TripPhase.ALIGHT_NOW) to
                    say("Alight at $stopName in ${toStop.roundToTens()} metres", VibrationPattern.ALIGHT)
            }
        }

        TripPhase.ALIGHT_NOW -> {
            next = next.streak(
                TripGuard.ARRIVED,
                toStop <= TripThresholds.ARRIVAL_RADIUS_M && fix.speed < TripThresholds.WALKING_SPEED_MPS,
            )
            if (next.count(TripGuard.ARRIVED) >= 3) {
                return next.enter(TripPhase.ARRIVED) to
                    say("You've arrived at $stopName", VibrationPattern.MILESTONE) + TripEffect.EndTrip
            }
        }

        else -> Unit
    }
    return next to emptyList()
}

private fun missedStop(
    plan: TripPlan,
    state: TripState,
    fix: TripEvent.LocationFix,
): Pair<TripState, List<TripEffect>> {
    val slowSince =
        if (fix.speed < TripThresholds.WALKING_SPEED_MPS) state.currentStreakTime ?: fix.timeStamp else null
    if (slowSince != null && fix.timeStamp - slowSince >= TripThresholds.OFF_BUS_TIME) {
        val behind = distanceMetres(fix, plan.alightStop).roundToTens()
        return state.enter(TripPhase.ARRIVED) to say(
            "You're off the bus. ${plan.alightStop.name} stage is ${behind}m behind you",
            VibrationPattern.MILESTONE,
        ) + TripEffect.EndTrip
    }
    return state.copy(currentStreakTime = slowSince) to emptyList()
}

private fun TripState.unchanged(): Pair<TripState, List<TripEffect>> = this to emptyList()

private fun TripState.enter(phase: TripPhase): TripState =
    copy(
        currentPhase = phase,
        guardStreaks = emptyMap(),
        currentStreakTime = null,
        previousDistanceToStage = null,
    )

private fun TripState.streak(
    guard: TripGuard,
    matched: Boolean,
): TripState = copy(guardStreaks = if (matched) guardStreaks + (guard to count(guard) + 1) else guardStreaks - guard)

private fun TripState.count(guard: TripGuard): Int = guardStreaks[guard] ?: 0

private fun say(
    text: String,
    pattern: VibrationPattern,
): List<TripEffect> = listOf(TripEffect.Speak(Message.Text(text)), TripEffect.Vibrate(pattern))

private fun Double.roundToTens(): Int = (this / 10).roundToInt() * 10

private fun distanceMetres(
    fix: TripEvent.LocationFix,
    stop: RouteStop,
): Double = ProximityEngine.haversineKm(fix.lat, fix.lng, stop.lat, stop.lng) * 1000

/**
 * Distance from the fix to the line through the stops, in metres.
 * There are no route shapes, so straight lines between consecutive stops stand in for the road.
 */
private fun distanceToRouteMetres(
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

sealed class Message {
    data class Text(
        val text: String,
    ) : Message()
    data class Resource(
        val resId: Int,
    ) : Message()
}

enum class TripPhase {
    PLANNED,
    WALKING_TO_STAGE,
    OFF_ROUTE,
    AT_STAGE,
    ON_BOARD,
    APPROACHING_STOP,
    ALIGHT_NOW,
    MISSED_STOP,
    ARRIVED,
    CANCELLED,
    ;

    val isFinal: Boolean get() = this == ARRIVED || this == CANCELLED
    val isRiding: Boolean get() = this == ON_BOARD || this == APPROACHING_STOP || this == ALIGHT_NOW
}

/** Guards that need several fixes in a row before they fire. */
enum class TripGuard {
    REACHED_STAGE,
    BACK_ON_TRACK,
    BOARDED,
    APPROACHING,
    WRONG_BUS,
    ALIGHT,
    ARRIVED,
    MISSED_STOP,
}

enum class VibrationPattern {
    TICK,
    MILESTONE,
    WARNING,
    GET_READY,
    ALIGHT,
}
