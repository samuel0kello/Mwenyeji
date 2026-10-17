package com.samuelokello.mwenyeji.feature.guidance.session

/**
 * One member per sentence the trip can speak. Only values that change between trips are fields;
 * the wording, in each language, belongs to the announcer.
 *
 * Moments the user already knows about (they tapped the button) or that need no words
 * (back on track, boarding noticed) only vibrate, so they have no member here.
 */
sealed class Message {
    data class WalkToStage(
        val metres: Int?,
        val stageName: String,
    ) : Message()

    data class AtStage(
        val vehicleName: String,
        val stopName: String,
        val fare: Int?,
    ) : Message()

    data object WalkingAwayFromStage : Message()

    data class OnBoardConfirmed(
        val stopsToRide: Int,
        val stopName: String,
    ) : Message()

    data class GetReady(
        val stopName: String,
    ) : Message()

    /** The bus left every road it is known to use, but it is still getting closer to the stop. */
    data class Shortcut(
        val stopName: String,
    ) : Message()

    data class WrongBus(
        val stopName: String,
    ) : Message()

    data class Alight(
        val stopName: String,
        val metres: Int,
    ) : Message()

    data class Arrived(
        val stopName: String,
    ) : Message()

    data object MissedStop : Message()

    data class OffBus(
        val stopName: String,
        val metresBehind: Int,
    ) : Message()

    data object LostLocation : Message()
}
