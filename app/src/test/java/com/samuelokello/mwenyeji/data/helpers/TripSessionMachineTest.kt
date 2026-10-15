package com.samuelokello.mwenyeji.data.helpers

import com.samuelokello.mwenyeji.data.helpers.TripSessionContract.TripEffect
import com.samuelokello.mwenyeji.data.helpers.TripSessionContract.TripEvent
import com.samuelokello.mwenyeji.data.helpers.TripSessionContract.TripPlan
import com.samuelokello.mwenyeji.data.helpers.TripSessionContract.TripState
import com.samuelokello.mwenyeji.data.models.RouteStop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TripSessionMachineTest {
    // Five stops heading east along one latitude, roughly 1.1km apart.
    private val lat = -1.28
    private val stops = List(5) { RouteStop("s$it", "Stop $it", lat, 36.82 + it * 0.01, it) }
    private val plan = TripPlan(
        stops = stops,
        boardingIndex = 0,
        alightIndex = 3,
        vehicleName = "Super Metro",
        fare = 100,
        walkingDistanceMetres = 200,
    )
    private val stage = stops[0]
    private val alight = stops[3]

    private val metre = 1 / 111_320.0
    private val walking = 1.2
    private val driving = 10.0

    private var clock = Instant.fromEpochSeconds(0)
    private var state = TripState()
    private var effects = emptyList<TripEffect>()

    private fun send(event: TripEvent) {
        val (next, emitted) = tripSessionMachine(plan, state, event)
        state = next
        effects = emitted
    }

    /** A fix [east] metres east and [north] metres north of [stop], 5 seconds after the last one. */
    private fun fix(
        stop: RouteStop,
        east: Double = 0.0,
        north: Double = 0.0,
        speed: Double = walking,
    ) {
        clock += 5.seconds
        send(TripEvent.LocationFix(stop.lat + north * metre, stop.lng + east * metre, speed, 5.0, clock))
    }

    private fun spoken(): String? = effects.filterIsInstance<TripEffect.Speak>().firstOrNull()?.let { (it.message as Message.Text).text }

    private fun vibration(): VibrationPattern? = effects.filterIsInstance<TripEffect.Vibrate>().firstOrNull()?.pattern

    private fun startIn(phase: TripPhase) {
        state = TripState(currentPhase = phase)
    }

    @Test
    fun `start without permission asks for it and stays planned`() {
        send(TripEvent.UserTapsStart(locationPermissionGranted = false))

        assertEquals(TripPhase.PLANNED, state.currentPhase)
        assertEquals(listOf(TripEffect.RequestLocationPermission), effects)
    }

    @Test
    fun `start with permission begins the walk`() {
        send(TripEvent.UserTapsStart(locationPermissionGranted = true))

        assertEquals(TripPhase.WALKING_TO_STAGE, state.currentPhase)
        assertEquals("Walk 200m to Stop 0 stage", spoken())
        assertEquals(VibrationPattern.TICK, vibration())
    }

    @Test
    fun `reaching the stage needs three fixes in a row`() {
        startIn(TripPhase.WALKING_TO_STAGE)

        fix(stage, east = -10.0)
        fix(stage, east = -10.0)
        fix(stage, east = -200.0)
        fix(stage, east = -10.0)
        fix(stage, east = -10.0)
        assertEquals(TripPhase.WALKING_TO_STAGE, state.currentPhase)

        fix(stage, east = -10.0)
        assertEquals(TripPhase.AT_STAGE, state.currentPhase)
        assertEquals("You're at the stage. Look for Super Metro to Stop 3, fare around 100", spoken())
        assertEquals(VibrationPattern.MILESTONE, vibration())
    }

    @Test
    fun `walking away for thirty seconds goes off route, then three closer fixes recover`() {
        startIn(TripPhase.WALKING_TO_STAGE)

        // First fix only sets the baseline; growth is measured from the second.
        (0..6).forEach { fix(stage, east = -200.0 - it * 10) }
        assertEquals(TripPhase.WALKING_TO_STAGE, state.currentPhase)

        fix(stage, east = -270.0)
        assertEquals(TripPhase.OFF_ROUTE, state.currentPhase)
        assertEquals(VibrationPattern.WARNING, vibration())

        fix(stage, east = -260.0)
        fix(stage, east = -250.0)
        assertEquals(TripPhase.OFF_ROUTE, state.currentPhase)
        fix(stage, east = -240.0)
        assertEquals(TripPhase.WALKING_TO_STAGE, state.currentPhase)
        assertEquals("Back on track", spoken())
    }

    @Test
    fun `tapping on board at the stage starts the ride`() {
        startIn(TripPhase.AT_STAGE)

        send(TripEvent.UserTapsIOnBoard)

        assertEquals(TripPhase.ON_BOARD, state.currentPhase)
        assertEquals("Sawa, 3 stops to Stop 3. I'll tell you when to get off", spoken())
    }

    @Test
    fun `moving fast away from the stage is detected as boarding`() {
        startIn(TripPhase.AT_STAGE)

        // Fast but still inside 100m does not count.
        fix(stage, east = 50.0, speed = driving)
        fix(stage, east = 150.0, speed = driving)
        fix(stage, east = 200.0, speed = driving)
        assertEquals(TripPhase.AT_STAGE, state.currentPhase)

        fix(stage, east = 250.0, speed = driving)
        assertEquals(TripPhase.ON_BOARD, state.currentPhase)
    }

    @Test
    fun `waiting at the stage stays quiet`() {
        startIn(TripPhase.AT_STAGE)

        repeat(20) {
            fix(stage, east = 5.0, speed = 0.0)
            assertTrue(effects.isEmpty())
        }
        assertEquals(TripPhase.AT_STAGE, state.currentPhase)
    }

    @Test
    fun `full ride goes get ready, alight, arrived`() {
        startIn(TripPhase.ON_BOARD)

        // Still closest to stop 1, more than 800m out.
        repeat(3) { fix(stops[1], east = 100.0, speed = driving) }
        assertEquals(TripPhase.ON_BOARD, state.currentPhase)

        repeat(3) { fix(alight, east = -700.0, speed = driving) }
        assertEquals(TripPhase.APPROACHING_STOP, state.currentPhase)
        assertEquals("Get ready, Stop 3 is coming up", spoken())
        assertEquals(VibrationPattern.GET_READY, vibration())

        fix(alight, east = -280.0, speed = driving)
        assertEquals(TripPhase.APPROACHING_STOP, state.currentPhase)
        fix(alight, east = -200.0, speed = driving)
        assertEquals(TripPhase.ALIGHT_NOW, state.currentPhase)
        assertEquals("Alight at Stop 3 in 200 metres", spoken())
        assertEquals(VibrationPattern.ALIGHT, vibration())

        // Inside the radius but still at speed: not arrived yet.
        repeat(3) { fix(alight, east = -20.0, speed = driving) }
        assertEquals(TripPhase.ALIGHT_NOW, state.currentPhase)

        repeat(3) { fix(alight, east = 10.0, speed = walking) }
        assertEquals(TripPhase.ARRIVED, state.currentPhase)
        assertEquals("You've arrived at Stop 3", spoken())
        assertTrue(TripEffect.EndTrip in effects)
    }

    @Test
    fun `passing the stop before the alight stop triggers get ready`() {
        startIn(TripPhase.ON_BOARD)

        // 900m short of the alight stop, but stop 2 is now the nearest one.
        repeat(3) { fix(stops[2], east = 200.0, speed = driving) }

        assertEquals(TripPhase.APPROACHING_STOP, state.currentPhase)
    }

    @Test
    fun `wrong bus warning fires once per trip`() {
        startIn(TripPhase.ON_BOARD)

        repeat(4) { fix(stops[1], north = 500.0, speed = driving) }
        assertTrue(effects.isEmpty())
        fix(stops[1], north = 500.0, speed = driving)
        assertEquals("This bus may not be going to Stop 3, check with the conductor", spoken())
        assertEquals(TripPhase.ON_BOARD, state.currentPhase)

        repeat(10) {
            fix(stops[1], north = 500.0, speed = driving)
            assertTrue(effects.isEmpty())
        }
    }

    @Test
    fun `riding past the stop is a missed stop, then slowing for a minute ends the trip`() {
        startIn(TripPhase.ALIGHT_NOW)

        repeat(2) { fix(stops[4], east = -100.0, speed = driving) }
        assertEquals(TripPhase.ALIGHT_NOW, state.currentPhase)
        fix(stops[4], east = -100.0, speed = driving)
        assertEquals(TripPhase.MISSED_STOP, state.currentPhase)
        assertEquals(VibrationPattern.ALIGHT, vibration())

        // Baseline fix plus 55 seconds of walking pace is not enough.
        repeat(12) { fix(stops[4], speed = walking) }
        assertEquals(TripPhase.MISSED_STOP, state.currentPhase)

        fix(stops[4], speed = walking)
        assertEquals(TripPhase.ARRIVED, state.currentPhase)
        assertTrue(spoken()!!.startsWith("You're off the bus. Stop 3 stage is "))
        assertTrue(TripEffect.EndTrip in effects)
    }

    @Test
    fun `tapping alighted ends the trip from any riding phase`() {
        listOf(TripPhase.ON_BOARD, TripPhase.APPROACHING_STOP, TripPhase.ALIGHT_NOW, TripPhase.MISSED_STOP)
            .forEach { phase ->
                startIn(phase)
                send(TripEvent.UserTapsIHaveAlighted)
                assertEquals(TripPhase.ARRIVED, state.currentPhase)
                assertTrue(TripEffect.EndTrip in effects)
            }

        startIn(TripPhase.WALKING_TO_STAGE)
        send(TripEvent.UserTapsIHaveAlighted)
        assertEquals(TripPhase.WALKING_TO_STAGE, state.currentPhase)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `cancel ends the trip and final states ignore everything`() {
        startIn(TripPhase.ON_BOARD)

        send(TripEvent.UserTapsCancel)
        assertEquals(TripPhase.CANCELLED, state.currentPhase)
        assertEquals("Guidance stopped", spoken())
        assertTrue(TripEffect.EndTrip in effects)

        send(TripEvent.UserTapsCancel)
        assertTrue(effects.isEmpty())
        send(TripEvent.GpsLost)
        assertTrue(effects.isEmpty())
        assertEquals(TripPhase.CANCELLED, state.currentPhase)
    }

    @Test
    fun `gps lost warns once until the next fix`() {
        startIn(TripPhase.ON_BOARD)

        send(TripEvent.GpsLost)
        assertEquals(VibrationPattern.WARNING, vibration())
        assertEquals(TripPhase.ON_BOARD, state.currentPhase)

        send(TripEvent.GpsLost)
        assertTrue(effects.isEmpty())

        fix(stops[1], speed = driving)
        assertFalse(state.gpsLostWarningGiven)
        send(TripEvent.GpsLost)
        assertEquals(VibrationPattern.WARNING, vibration())
    }
}
