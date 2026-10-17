package com.samuelokello.mwenyeji.feature.guidance.session

import com.samuelokello.mwenyeji.data.models.GeoPoint
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripEffect
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripEvent
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripPlan
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripState
import kotlin.math.roundToInt

internal class TripSessionReducer(
    private val plan: TripPlan,
) {
    // The main line through the stops, then the roads riders' traces showed matatus also take.
    private val routePaths: List<List<GeoPoint>> = listOf(plan.stops.map { GeoPoint(it.lat, it.lng) }) + plan.knownPaths

    /**
     * Pure transition function for a guided trip. See StateMachine.md for the table this implements.
     * If no guard matches, the state is returned unchanged with no effects.
     */
    internal fun reduce(
        currentState: TripState,
        event: TripEvent,
    ): Pair<TripState, List<TripEffect>> {
        val phase = currentState.currentPhase
        if (phase.isFinal) return currentState.unchanged()

        return when (event) {
            is TripEvent.UserTapsStart -> when {
                phase != TripPhase.PLANNED -> currentState.unchanged()

                event.locationPermissionGranted -> currentState.enter(TripPhase.WALKING_TO_STAGE) to say(
                    Message.WalkToStage(plan.walkingDistanceMetres, plan.stage.name),
                    VibrationPattern.TICK,
                )

                else -> currentState to listOf(TripEffect.RequestLocationPermission)
            }

            TripEvent.UserTapsIOnBoard -> if (phase == TripPhase.AT_STAGE) {
                currentState.enter(TripPhase.ON_BOARD) to say(
                    Message.OnBoardConfirmed(plan.stopsToRide, plan.alightStop.name),
                    VibrationPattern.TICK,
                )
            } else {
                currentState.unchanged()
            }

            TripEvent.UserTapsIHaveAlighted -> if (phase.isRiding || phase == TripPhase.MISSED_STOP) {
                currentState.enter(TripPhase.ARRIVED) to buzz(VibrationPattern.MILESTONE) + TripEffect.EndTrip
            } else {
                currentState.unchanged()
            }

            TripEvent.UserTapsCancel -> currentState.enter(TripPhase.CANCELLED) to buzz(VibrationPattern.TICK) + TripEffect.EndTrip

            TripEvent.GpsLost ->
                // Nothing is tracking the user before Start, so there is nothing to warn about.
                if (phase == TripPhase.PLANNED || currentState.gpsLostWarningGiven) {
                    currentState.unchanged()
                } else {
                    currentState.copy(gpsLostWarningGiven = true) to say(Message.LostLocation, VibrationPattern.WARNING)
                }

            is TripEvent.LocationFix -> {
                val state = currentState.copy(gpsLostWarningGiven = false)
                when (phase) {
                    TripPhase.WALKING_TO_STAGE -> walkingToStage(state, event)

                    TripPhase.OFF_ROUTE -> offRoute(state, event)

                    TripPhase.AT_STAGE -> atStage(state, event)

                    TripPhase.ON_BOARD,
                    TripPhase.APPROACHING_STOP,
                    TripPhase.ALIGHT_NOW,
                    -> riding(state, event)

                    TripPhase.MISSED_STOP -> missedStop(state, event)

                    TripPhase.PLANNED,
                    TripPhase.ARRIVED,
                    TripPhase.CANCELLED,
                    -> state.unchanged()
                }
            }
        }
    }

    private fun walkingToStage(
        state: TripState,
        fix: TripEvent.LocationFix,
    ): Pair<TripState, List<TripEffect>> {
        val distance = distanceMetres(fix, plan.stage)

        val atStage = state.streak(TripGuard.REACHED_STAGE, distance <= TripThresholds.STAGE_RADIUS_M)
        if (atStage.count(TripGuard.REACHED_STAGE) >= 3) {
            return atStage.enter(TripPhase.AT_STAGE) to say(
                Message.AtStage(plan.vehicleName, plan.alightStop.name, plan.fare),
                VibrationPattern.MILESTONE,
            )
        }

        val previous = state.previousDistanceToStage
        val walkingAwaySince = if (previous != null && distance > previous) state.currentStreakTime ?: fix.elapsedMillis else null
        if (walkingAwaySince != null && fix.elapsedMillis - walkingAwaySince >= TripThresholds.OFF_ROUTE_TIME_MS) {
            return atStage.enter(TripPhase.OFF_ROUTE).copy(previousDistanceToStage = distance) to say(
                Message.WalkingAwayFromStage,
                VibrationPattern.WARNING,
            )
        }

        return atStage.copy(
            previousDistanceToStage = distance,
            currentStreakTime = walkingAwaySince,
        ) to emptyList()
    }

    private fun offRoute(
        state: TripState,
        fix: TripEvent.LocationFix,
    ): Pair<TripState, List<TripEffect>> {
        val distance = distanceMetres(fix, plan.stage)
        val previous = state.previousDistanceToStage

        val next = state.streak(TripGuard.BACK_ON_TRACK, previous != null && distance < previous).copy(previousDistanceToStage = distance)
        if (next.count(TripGuard.BACK_ON_TRACK) >= 3) {
            return next.enter(TripPhase.WALKING_TO_STAGE).copy(previousDistanceToStage = distance) to buzz(VibrationPattern.TICK)
        }
        return next to emptyList()
    }

    private fun atStage(
        state: TripState,
        fix: TripEvent.LocationFix,
    ): Pair<TripState, List<TripEffect>> {
        val moving = fix.speed > TripThresholds.VEHICLE_SPEED_MPS && distanceMetres(fix, plan.stage) > TripThresholds.BOARDED_DISTANCE_M

        val next = state.streak(TripGuard.BOARDED, moving)
        if (next.count(TripGuard.BOARDED) >= 3) {
            return next.enter(TripPhase.ON_BOARD) to buzz(VibrationPattern.TICK)
        }
        return next to emptyList()
    }

    /** ON_BOARD, APPROACHING_STOP and ALIGHT_NOW. The missed-stop check wins over everything else. */
    private fun riding(
        state: TripState,
        fix: TripEvent.LocationFix,
    ): Pair<TripState, List<TripEffect>> {
        val stopName = plan.alightStop.name
        val toStop = distanceMetres(fix, plan.alightStop)
        val nearestIndex = plan.stops.indices.minBy { distanceMetres(fix, plan.stops[it]) }
        val offRoute = distanceToPathsMetres(fix, routePaths) > TripThresholds.OFF_ROUTE_RIDE_DISTANCE_M
        // "Past the stop": back on known roads, and the nearest stop comes after the alight stop.
        // On a detour the nearest stop says little, because the bus skips stops.
        val pastStop = !offRoute && nearestIndex > plan.alightIndex

        var next = state.streak(
            TripGuard.MISSED_STOP,
            pastStop && toStop > TripThresholds.MISSED_STOP_DISTANCE_M && fix.speed > TripThresholds.VEHICLE_SPEED_MPS,
        )
        if (next.count(TripGuard.MISSED_STOP) >= 3) {
            return next.enter(TripPhase.MISSED_STOP) to say(
                Message.MissedStop,
                VibrationPattern.ALIGHT,
            )
        }

        when (state.currentPhase) {
            TripPhase.ON_BOARD -> {
                // Get ready by distance, or by stops passed while the bus is on its known roads.
                next = next.streak(
                    TripGuard.APPROACHING,
                    toStop <= TripThresholds.GET_READY_DISTANCE_M || (!offRoute && nearestIndex >= plan.alightIndex - 1),
                )
                if (next.count(TripGuard.APPROACHING) >= 3) {
                    return next.enter(TripPhase.APPROACHING_STOP) to say(Message.GetReady(stopName), VibrationPattern.GET_READY)
                }

                return detour(next.streak(TripGuard.DETOUR, offRoute), toStop)
            }

            TripPhase.APPROACHING_STOP -> {
                next = next.streak(TripGuard.ALIGHT, toStop <= TripThresholds.ALIGHT_DISTANCE_M)
                if (next.count(TripGuard.ALIGHT) >= 2) {
                    return next.enter(TripPhase.ALIGHT_NOW) to say(Message.Alight(stopName, toStop.roundToTens()), VibrationPattern.ALIGHT)
                }
            }

            TripPhase.ALIGHT_NOW -> {
                next = next.streak(
                    TripGuard.ARRIVED,
                    toStop <= TripThresholds.ARRIVAL_RADIUS_M && fix.speed < TripThresholds.WALKING_SPEED_MPS,
                )
                if (next.count(TripGuard.ARRIVED) >= 3) {
                    return next.enter(TripPhase.ARRIVED) to say(Message.Arrived(stopName), VibrationPattern.MILESTONE) + TripEffect.EndTrip
                }
            }

            else -> Unit
        }
        return next to emptyList()
    }

    /**
     * A bus off its known roads is judged by where it is heading, not where it is: still getting closer
     * to the stop is a shortcut around traffic, getting clearly further is probably the wrong bus.
     */
    private fun detour(
        state: TripState,
        toStop: Double,
    ): Pair<TripState, List<TripEffect>> {
        if (state.count(TripGuard.DETOUR) < 3) {
            return state.copy(detourStartToStopMetres = null) to emptyList()
        }

        val start = state.detourStartToStopMetres ?: toStop
        val next = state.copy(detourStartToStopMetres = start)
        return when {
            toStop >= start + TripThresholds.DETOUR_LOSS_M && !next.wrongBusWarningGiven ->
                next.copy(wrongBusWarningGiven = true) to say(Message.WrongBus(plan.alightStop.name), VibrationPattern.WARNING)

            toStop <= start - TripThresholds.DETOUR_PROGRESS_M && !next.shortcutNoticeGiven ->
                next.copy(shortcutNoticeGiven = true) to say(Message.Shortcut(plan.alightStop.name), VibrationPattern.TICK)

            else -> next to emptyList()
        }
    }

    private fun missedStop(
        state: TripState,
        fix: TripEvent.LocationFix,
    ): Pair<TripState, List<TripEffect>> {
        val slowSince = if (fix.speed < TripThresholds.WALKING_SPEED_MPS) state.currentStreakTime ?: fix.elapsedMillis else null
        if (slowSince != null && fix.elapsedMillis - slowSince >= TripThresholds.OFF_BUS_TIME_MS) {
            val behind = distanceMetres(fix, plan.alightStop).roundToTens()
            return state.enter(TripPhase.ARRIVED) to say(
                Message.OffBus(plan.alightStop.name, behind),
                VibrationPattern.MILESTONE,
            ) + TripEffect.EndTrip
        }
        return state.copy(currentStreakTime = slowSince) to emptyList()
    }
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

// The buzz comes first so attention arrives before the words.
private fun say(
    message: Message,
    pattern: VibrationPattern,
): List<TripEffect> = listOf(TripEffect.Vibrate(pattern), TripEffect.Speak(message))

/** For moments the user already knows about, or that need no words: a buzz is enough. */
private fun buzz(pattern: VibrationPattern): List<TripEffect> = listOf(TripEffect.Vibrate(pattern))

private fun Double.roundToTens(): Int = (this / 10).roundToInt() * 10
