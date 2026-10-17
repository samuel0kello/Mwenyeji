package com.samuelokello.mwenyeji.feature.guidance.announcer

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.samuelokello.mwenyeji.feature.guidance.session.VibrationPattern

/** [Haptics] backed by the device [Vibrator]. */
class VibratorHaptics(
    context: Context,
) : Haptics {
    private val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    private val mainHandler = Handler(Looper.getMainLooper())
    private val stopRepeating = Runnable { vibrator.cancel() }

    override fun vibrate(pattern: VibrationPattern) {
        mainHandler.removeCallbacks(stopRepeating)
        val timings = pattern.timings()
        val repeatFor = pattern.repeatsForMs
        // Index 0 repeats the whole waveform; -1 plays it once.
        val repeatFrom = if (repeatFor != null) 0 else -1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, repeatFrom))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, repeatFrom)
        }
        if (repeatFor != null) mainHandler.postDelayed(stopRepeating, repeatFor)
    }

    override fun stop() {
        mainHandler.removeCallbacks(stopRepeating)
        vibrator.cancel()
    }
}
