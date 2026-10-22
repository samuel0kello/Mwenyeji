package com.samuelokello.mwenyeji.feature.guidance.announcer

import com.samuelokello.mwenyeji.feature.guidance.session.Message
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripEffect

/**
 * Plays the reducer's effects: sentences go to the [speaker], buzzes to [haptics].
 * Effects that are not sound or touch (permission, end of trip) belong to whoever runs the trip.
 *
 * @param language The user's language for this trip.
 * @param canSpeak Asked before every sentence. False means the trip is vibrate-only right now, for
 *                 example no earphones in a matatu, where a stop name said aloud tells everyone
 *                 where the rider is going.
 */
class Announcer(
    private val speaker: Speaker,
    private val haptics: Haptics,
    private val language: Language,
    private val canSpeak: () -> Boolean = { true },
) {
    fun announce(effects: List<TripEffect>) {
        effects.forEach { effect ->
            when (effect) {
                is TripEffect.Speak -> if (canSpeak()) speaker.speak(utteranceFor(effect.message))

                is TripEffect.Vibrate -> haptics.vibrate(effect.pattern)

                TripEffect.RequestLocationPermission,
                TripEffect.EndTrip,
                -> Unit
            }
        }
    }

    /** Silence now: the earphones came out, or the rider turned the speaker off. Buzzes carry on. */
    fun stopSpeaking() {
        speaker.stop()
    }

    /** Call when the trip ends. The last sentence ("You've arrived…") still finishes. */
    fun close() {
        speaker.shutdown()
    }

    private fun utteranceFor(message: Message) =
        Utterance(
            text = PhraseBook.phraseFor(message, language),
            language = language,
            englishText = PhraseBook.phraseFor(message, Language.ENGLISH),
            priority = message.priority,
        )
}
