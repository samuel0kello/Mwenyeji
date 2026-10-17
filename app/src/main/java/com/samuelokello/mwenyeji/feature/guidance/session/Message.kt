package com.samuelokello.mwenyeji.feature.guidance.session

/**
 * One member per sentence the trip can speak. Only values that change between trips are fields;
 * the wording, in each language, belongs to the announcer.
 */
sealed class Message {
    data class WalkToStage(
        val metres: Int,
        val stageName: String,
    ) : Message()

    data class AtStage(
        val vehicleName: String,
        val stopName: String,
        val fare: Int?,
    ) : Message()

    data object WalkingAwayFromStage : Message()

    data object BackOnTrack : Message()

    data class OnBoardConfirmed(
        val stopsToRide: Int,
        val stopName: String,
    ) : Message()

    data object BoardingDetected : Message()

    data class GetReady(
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

    data object TripEnded : Message()

    data object GuideStopped : Message()

    data object LostLocation : Message()
}
