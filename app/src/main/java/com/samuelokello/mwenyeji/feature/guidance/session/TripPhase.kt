package com.samuelokello.mwenyeji.feature.guidance.session

enum class TripPhase {
    PLANNED,
    WALKING_TO_STAGE,
    OFF_ROUTE,
    AT_STAGE,
    ON_BOARD,
    APPROACHING_STOP,
    ALIGHT_NOW,
    MISSED_STOP,
    ARRIVED,
    CANCELLED,
    ;

    val isFinal: Boolean get() = this == ARRIVED || this == CANCELLED
    val isRiding: Boolean get() = this == ON_BOARD || this == APPROACHING_STOP || this == ALIGHT_NOW
}
