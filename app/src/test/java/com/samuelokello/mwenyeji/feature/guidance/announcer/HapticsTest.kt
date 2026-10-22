package com.samuelokello.mwenyeji.feature.guidance.announcer

import com.samuelokello.mwenyeji.feature.guidance.session.VibrationPattern
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HapticsTest {
    // Odd positions in a waveform are the buzzes; even positions are pauses.
    private fun VibrationPattern.buzzes() = timings().filterIndexed { index, _ -> index % 2 == 1 }

    @Test
    fun `buzz counts match the haptic table`() {
        assertEquals(1, VibrationPattern.TICK.buzzes().size)
        assertEquals(2, VibrationPattern.MILESTONE.buzzes().size)
        assertEquals(1, VibrationPattern.WARNING.buzzes().size)
        assertEquals(2, VibrationPattern.GET_READY.buzzes().size)
        assertEquals(3, VibrationPattern.ALIGHT.buzzes().size)
    }

    @Test
    fun `long buzzes are longer than short ones`() {
        assertTrue(VibrationPattern.WARNING.buzzes().single() > VibrationPattern.TICK.buzzes().single())
    }

    @Test
    fun `every pattern feels different`() {
        val shapes = VibrationPattern.entries.map { it.timings().toList() }
        assertEquals(shapes.size, shapes.toSet().size)
    }

    @Test
    fun `only alight repeats, for ten seconds`() {
        assertEquals(10_000L, VibrationPattern.ALIGHT.repeatsForMs)
        VibrationPattern.entries.filter { it != VibrationPattern.ALIGHT }.forEach { assertNull(it.repeatsForMs) }
    }
}
