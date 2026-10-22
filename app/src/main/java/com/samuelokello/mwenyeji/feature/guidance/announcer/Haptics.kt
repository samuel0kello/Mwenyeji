package com.samuelokello.mwenyeji.feature.guidance.announcer

import com.samuelokello.mwenyeji.feature.guidance.session.VibrationPattern

interface Haptics {
    /** Replaces whatever is vibrating now. */
    fun vibrate(pattern: VibrationPattern)

    /** Stops a repeating pattern early, for example when the user taps the screen. */
    fun stop()
}

/**
 * Off and on durations in milliseconds, starting with an off delay, as Android's waveform expects.
 * See the haptic patterns table in StateMachine.md.
 */
internal fun VibrationPattern.timings(): LongArray =
    when (this) {
        VibrationPattern.TICK -> longArrayOf(0, SHORT_BUZZ_MS)

        VibrationPattern.MILESTONE -> longArrayOf(0, SHORT_BUZZ_MS, GAP_MS, SHORT_BUZZ_MS)

        VibrationPattern.WARNING -> longArrayOf(0, LONG_BUZZ_MS)

        VibrationPattern.GET_READY -> longArrayOf(0, LONG_BUZZ_MS, GAP_MS, LONG_BUZZ_MS)

        // The trailing pause separates one round from the next while it repeats.
        VibrationPattern.ALIGHT -> longArrayOf(0, LONG_BUZZ_MS, GAP_MS, LONG_BUZZ_MS, GAP_MS, LONG_BUZZ_MS, ROUND_PAUSE_MS)
    }

/** Alight repeats until the user taps or this much time has passed. */
internal val VibrationPattern.repeatsForMs: Long?
    get() = if (this == VibrationPattern.ALIGHT) ALIGHT_REPEAT_MS else null

private const val SHORT_BUZZ_MS = 80L
private const val LONG_BUZZ_MS = 400L
private const val GAP_MS = 150L
private const val ROUND_PAUSE_MS = 700L
private const val ALIGHT_REPEAT_MS = 10_000L
