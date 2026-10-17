package com.samuelokello.mwenyeji.feature.guidance.session

/** Starting guesses. Tune them on real trips. */
object TripThresholds {
    const val STAGE_RADIUS_M = 30.0
    const val BOARDED_DISTANCE_M = 100.0
    const val GET_READY_DISTANCE_M = 800.0
    const val ALIGHT_DISTANCE_M = 300.0
    const val ARRIVAL_RADIUS_M = 50.0
    const val MISSED_STOP_DISTANCE_M = 150.0

    // Further than this from the route and every learned variant, the bus is on a detour.
    const val OFF_ROUTE_RIDE_DISTANCE_M = 300.0

    // On a detour, this much closer to the stop than where the detour began means a shortcut...
    const val DETOUR_PROGRESS_M = 200.0

    // ...and this much further means the bus is heading somewhere else. Standing in traffic changes
    // neither, so a jam on a detour stays quiet.
    const val DETOUR_LOSS_M = 500.0
    const val VEHICLE_SPEED_MPS = 15 / 3.6
    const val WALKING_SPEED_MPS = 7 / 3.6
    const val OFF_ROUTE_TIME_MS = 30_000L
    const val OFF_BUS_TIME_MS = 60_000L
    const val GPS_LOST_TIMEOUT_MS = 60_000L

    // Fixes less accurate than this are ignored, and count towards GPS lost.
    const val MAX_FIX_ACCURACY_M = 50.0
}
