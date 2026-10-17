package com.samuelokello.mwenyeji.feature.guidance.session

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
    const val OFF_ROUTE_TIME_MS = 30_000L
    const val OFF_BUS_TIME_MS = 60_000L
    const val GPS_LOST_TIMEOUT_MS = 60_000L
}
