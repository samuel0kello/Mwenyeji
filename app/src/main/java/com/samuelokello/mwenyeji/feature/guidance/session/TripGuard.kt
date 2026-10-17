package com.samuelokello.mwenyeji.feature.guidance.session

/** Guards that need several fixes in a row before they fire. */
enum class TripGuard {
    REACHED_STAGE,
    BACK_ON_TRACK,
    BOARDED,
    APPROACHING,
    DETOUR,
    ALIGHT,
    ARRIVED,
    MISSED_STOP,
}
