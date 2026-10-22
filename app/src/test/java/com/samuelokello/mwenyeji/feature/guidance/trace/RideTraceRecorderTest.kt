package com.samuelokello.mwenyeji.feature.guidance.trace

import com.samuelokello.mwenyeji.feature.guidance.session.TripPhase
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RideTraceRecorderTest {
    private val metre = 1 / 111_320.0
    private val recorder = RideTraceRecorder()
    private var clock = 0L

    /** A fix [east] metres east of a fixed point, 2 seconds after the last one. */
    private fun fix(
        east: Double,
        phase: TripPhase,
    ) {
        clock += 2_000L
        recorder.onFix(TripEvent.LocationFix(-1.28, 36.82 + east * metre, 8.0, 5.0, clock), phase)
    }

    @Test
    fun `only the ride is kept, not the walk before or after`() {
        repeat(20) { fix(it * 30.0, TripPhase.WALKING_TO_STAGE) }
        repeat(20) { fix(1_000.0 + it * 30.0, TripPhase.ON_BOARD) }
        repeat(20) { fix(2_000.0 + it * 30.0, TripPhase.ARRIVED) }

        val points = recorder.finish()!!
        assertEquals(20, points.size)
        // The first kept point is where the ride began, and time counts from there.
        assertEquals(36.82 + 1_000.0 * metre, points.first().lng, 0.00001)
        assertEquals(0, points.first().secondsFromStart)
        assertEquals(38, points.last().secondsFromStart)
    }

    @Test
    fun `points closer than twenty metres to the last one are dropped`() {
        // Every 15m: each second fix is 30m on from the last kept one.
        repeat(40) { fix(it * 15.0, TripPhase.ON_BOARD) }

        assertEquals(20, recorder.finish()!!.size)
    }

    @Test
    fun `a ride too short to learn from gives nothing`() {
        repeat(9) { fix(it * 30.0, TripPhase.APPROACHING_STOP) }

        assertNull(recorder.finish())
    }
}
