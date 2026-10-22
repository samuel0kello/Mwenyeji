package com.samuelokello.mwenyeji.feature.guidance.announcer

import com.samuelokello.mwenyeji.feature.guidance.session.CompassDirection
import com.samuelokello.mwenyeji.feature.guidance.session.Message

/**
 * Turns a [Message] into the sentence spoken in [Language]. Cases follow the order of a trip.
 * Distances are written as "metres" in full so the voice does not read "200m" as "200 em".
 */
object PhraseBook {
    fun phraseFor(
        message: Message,
        language: Language,
    ): String =
        when (language) {
            Language.ENGLISH -> english(message)

            // Wording to come. English until then, so a trip can run end to end.
            Language.SWAHILI -> english(message)

            Language.SHENG -> english(message)
        }

    private fun english(message: Message): String =
        when (message) {
            is Message.WalkToStage ->
                "Walk " + (message.metres?.let { "$it metres " } ?: "") + "to ${message.stageName} stage"

            is Message.WalkProgress ->
                "${message.stageName} stage, ${message.metres} metres ${message.direction.english()}"

            is Message.AtStage ->
                "You're at the stage. Look for ${message.vehicleName} to ${message.stopName}" +
                    (message.fare?.let { ", fare around $it" } ?: "")

            Message.WalkingAwayFromStage -> "You're walking away from the stage"

            is Message.OnBoardConfirmed ->
                "Sawa, ${message.stopsToRide} stops to ${message.stopName}. I'll tell you when to get off"

            is Message.GetReady -> "Get ready, ${message.stopName} is coming up"

            is Message.Shortcut ->
                "The driver is taking another road. I'll still tell you when to get off at ${message.stopName}"

            is Message.WrongBus -> "This bus may not be going to ${message.stopName}, check with the conductor"

            is Message.Alight -> "Alight at ${message.stopName} in ${message.metres} metres"

            is Message.Arrived -> "You've arrived at ${message.stopName}"

            Message.MissedStop ->
                "You've passed your stop. Ask to be dropped now if it's safe, otherwise at the next stage"

            is Message.OffBus -> "You're off the bus. ${message.stopName} stage is ${message.metresBehind} metres behind you"

            Message.LostLocation -> "I've lost your location, watch for your stop"
        }

    private fun CompassDirection.english(): String = name.lowercase().replace('_', '-')
}
