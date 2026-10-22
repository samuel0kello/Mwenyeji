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
    // The route's road (or the line through its stops), then the roads riders' traces showed matatus also take.
    private val routePaths: List<List<GeoPoint>> = listOf(plan.mainLine) + plan.knownPaths

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

            TripEvent.UserTapsIOnBoard -> when (phase) {
                TripPhase.AT_STAGE -> board(currentState, plan.boardingIndex)

                // Boarded before reaching the planned stage: at whichever stop the rider is nearest.
                TripPhase.WALKING_TO_STAGE,
                TripPhase.OFF_ROUTE,
                -> board(currentState, currentState.lastPosition?.let { nearestStage(it.lat, it.lng) } ?: plan.boardingIndex)

                else -> currentState.unchanged()
            }

            TripEvent.UserTapsAtStage -> if (phase == TripPhase.WALKING_TO_STAGE || phase == TripPhase.OFF_ROUTE) {
                currentState.enter(TripPhase.AT_STAGE) to say(
                    Message.AtStage(plan.vehicleName, plan.alightStop.name, plan.fare),
                    VibrationPattern.MILESTONE,
                )
            } else {
                currentState.unchanged()
            }

            is TripEvent.UserPicksStage -> {
                val index = event.stopIndex
                if (phase.isBeforeBoarding && index in 0 until plan.alightIndex && index != plan.boardingIndex) {
                    val stage = plan.stops[index]
                    val metres = currentState.lastPosition?.let { metresBetween(it.lat, it.lng, stage).roundToTens() }
                    currentState.enter(TripPhase.WALKING_TO_STAGE) to
                        listOf(TripEffect.MoveStage(index)) + say(Message.WalkToStage(metres, stage.name), VibrationPattern.TICK)
                } else {
                    currentState.unchanged()
                }
            }

            TripEvent.UserTapsIHaveAlighted -> if (phase.isRiding || phase == TripPhase.MISSED_STOP) {
                currentState.enter(TripPhase.ARRIVED) to buzz(VibrationPattern.MILESTONE) + TripEffect.EndTrip
            } else {
                currentState.unchanged()
            }

            TripEvent.UserTapsCancel -> currentState.enter(TripPhase.CANCELLED) to buzz(VibrationPattern.TICK) + TripEffect.EndTrip

            TripEvent.TripExpired ->
                currentState.enter(TripPhase.CANCELLED) to say(Message.GuidanceEnded, VibrationPattern.WARNING) + TripEffect.EndTrip

            TripEvent.GpsLost ->
                // Nothing is tracking the user before Start, so there is nothing to warn about.
                if (phase == TripPhase.PLANNED || currentState.gpsLostWarningGiven) {
                    currentState.unchanged()
                } else {
                    currentState.copy(gpsLostWarningGiven = true) to say(Message.LostLocation, VibrationPattern.WARNING)
                }

            is TripEvent.LocationFix -> {
                val state = currentState.copy(gpsLostWarningGiven = false, lastPosition = GeoPoint(event.lat, event.lng))
                when (phase) {
                    TripPhase.WALKING_TO_STAGE -> beforeBoarding(state, event, ::walkingToStage)

                    TripPhase.OFF_ROUTE -> beforeBoarding(state, event, ::offRoute)

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

    /**
     * Checks that apply on the way to the stage, before the [phaseRules] of the current phase. The
     * rider may get on at another stage than planned (the stop on the map is not always where
     * matatus stop), or get all the way to their stop without boarding being noticed.
     */
    private fun beforeBoarding(
        state: TripState,
        fix: TripEvent.LocationFix,
        phaseRules: (TripState, TripEvent.LocationFix) -> Pair<TripState, List<TripEffect>>,
    ): Pair<TripState, List<TripEffect>> {
        val toStop = distanceMetres(fix, plan.alightStop)

        var next = state.streak(
            TripGuard.ARRIVED_WITHOUT_RIDE,
            toStop <= TripThresholds.ALIGHT_DISTANCE_M && distanceMetres(fix, plan.stage) > 2 * TripThresholds.ALIGHT_DISTANCE_M,
        )
        if (next.count(TripGuard.ARRIVED_WITHOUT_RIDE) >= 3) {
            return next.enter(TripPhase.ARRIVED) to
                say(Message.Arrived(plan.alightStop.name), VibrationPattern.MILESTONE) + TripEffect.EndTrip
        }

        // Moving at vehicle speed along the route and getting closer to the stop: on a matatu. Where
        // the rider was when it began says which stage they boarded at. Traffic slows the matatu
        // without resetting that; leaving the route does.
        val nearRoute = distanceToPathsMetres(fix, routePaths) <= TripThresholds.BOARDING_CORRIDOR_M
        next = next.streak(TripGuard.BOARDED_ELSEWHERE, nearRoute && fix.speed > TripThresholds.VEHICLE_SPEED_MPS)
        next =
            when {
                !nearRoute -> next.copy(movingStartToStopMetres = null, movingStartStage = null)

                next.movingStartToStopMetres == null && next.count(TripGuard.BOARDED_ELSEWHERE) > 0 ->
                    next.copy(movingStartToStopMetres = toStop, movingStartStage = nearestStage(fix.lat, fix.lng))

                else -> next
            }
        val start = next.movingStartToStopMetres
        if (start != null &&
            next.count(TripGuard.BOARDED_ELSEWHERE) >= 3 &&
            toStop <= start - TripThresholds.BOARDING_PROGRESS_M
        ) {
            return board(next, next.movingStartStage ?: plan.boardingIndex)
        }

        return phaseRules(next, fix)
    }

    /** On board, from the stop at [boardingIndex]; the plan moves there if that is not the planned stage. */
    private fun board(
        state: TripState,
        boardingIndex: Int,
    ): Pair<TripState, List<TripEffect>> {
        val moved = if (boardingIndex != plan.boardingIndex) listOf(TripEffect.MoveStage(boardingIndex)) else emptyList()
        return state.enter(TripPhase.ON_BOARD).copy(movingStartToStopMetres = null, movingStartStage = null) to
            moved + say(Message.OnBoardConfirmed(plan.alightIndex - boardingIndex, plan.alightStop.name), VibrationPattern.TICK)
    }

    /** The stop before the alight stop nearest to the point. */
    private fun nearestStage(
        lat: Double,
        lng: Double,
    ): Int = (0 until plan.alightIndex).minBy { metresBetween(lat, lng, plan.stops[it]) }

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

        // Every stretch closer, say how far and which way. The first fix of the walk only sets the mark.
        // Compared as spoken, to the nearest ten metres, so "400" follows "500" and not "390".
        val lastUpdate = state.lastWalkUpdateMetres
        val progress =
            lastUpdate != null &&
                distance.roundToTens() <= lastUpdate.roundToTens() - TripThresholds.WALK_UPDATE_STEP_M &&
                distance > TripThresholds.WALK_UPDATE_MIN_M &&
                // Riding past the stage in a matatu is not walking to it.
                fix.speed <= TripThresholds.VEHICLE_SPEED_MPS
        val next =
            atStage.copy(
                previousDistanceToStage = distance,
                currentStreakTime = walkingAwaySince,
                lastWalkUpdateMetres = if (lastUpdate == null || progress) distance else lastUpdate,
            )
        if (!progress) return next to emptyList()
        return next to say(
            Message.WalkProgress(
                plan.stage.name,
                distance.roundToTens(),
                CompassDirection.fromBearing(bearingDegrees(fix, plan.stage)),
            ),
            VibrationPattern.TICK,
        )
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

                // Matatus drop riders where they can, not always at the stop. Slow (off and walking)
                // this close to it for long enough is arrived too.
                val slowSince = if (fix.speed < TripThresholds.WALKING_SPEED_MPS) state.currentStreakTime ?: fix.elapsedMillis else null
                if (slowSince != null && fix.elapsedMillis - slowSince >= TripThresholds.ARRIVED_SLOW_TIME_MS) {
                    return next.enter(TripPhase.ARRIVED) to say(Message.Arrived(stopName), VibrationPattern.MILESTONE) + TripEffect.EndTrip
                }
                next = next.copy(currentStreakTime = slowSince)
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
        lastWalkUpdateMetres = null,
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
