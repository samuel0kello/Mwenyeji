package com.samuelokello.mwenyeji.feature.guidance.trace

import com.samuelokello.mwenyeji.data.helpers.ProximityEngine
import com.samuelokello.mwenyeji.data.models.TracePoint
import com.samuelokello.mwenyeji.feature.guidance.session.TripPhase
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripEvent
import kotlin.math.roundToInt

/**
 * Keeps the path of the ride, and only the ride: fixes before boarding and after alighting are dropped,
 * so the walk from home or to the destination never leaves the phone. Points are thinned to one every
 * [MIN_SPACING_M], which is plenty to tell one road from another.
 */
internal class RideTraceRecorder {
    private val points = mutableListOf<TracePoint>()
    private var startMillis: Long? = null

    fun onFix(
        fix: TripEvent.LocationFix,
        phase: TripPhase,
    ) {
        if (!phase.isRiding) return
        val start = startMillis ?: fix.elapsedMillis.also { startMillis = it }

        val last = points.lastOrNull()
        if (last != null && metresBetween(last, fix) < MIN_SPACING_M) return

        points +=
            TracePoint(
                lat = fix.lat.roundTo(COORDINATE_DECIMALS),
                lng = fix.lng.roundTo(COORDINATE_DECIMALS),
                secondsFromStart = ((fix.elapsedMillis - start) / 1000).toInt(),
                speedMps = fix.speed.roundTo(1),
            )
    }

    /** The ride, or null when it is too short to learn anything from. */
    fun finish(): List<TracePoint>? = points.toList().takeIf { it.size >= MIN_POINTS }

    companion object {
        const val MIN_SPACING_M = 20.0
        const val MIN_POINTS = 10

        // Five decimals is about a metre: no more precise than GPS, so nothing is lost.
        private const val COORDINATE_DECIMALS = 5
    }
}

private fun metresBetween(
    point: TracePoint,
    fix: TripEvent.LocationFix,
): Double = ProximityEngine.haversineKm(point.lat, point.lng, fix.lat, fix.lng) * 1000

private fun Double.roundTo(decimals: Int): Double {
    var scale = 1.0
    repeat(decimals) { scale *= 10 }
    return (this * scale).roundToInt() / scale
}
