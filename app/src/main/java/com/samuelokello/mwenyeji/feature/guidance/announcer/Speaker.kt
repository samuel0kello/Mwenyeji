package com.samuelokello.mwenyeji.feature.guidance.announcer

import kotlinx.coroutines.flow.StateFlow

/**
 * One sentence to speak.
 *
 * @param englishText Spoken instead of [text] when the device has no voice for [language].
 */
data class Utterance(
    val text: String,
    val language: Language,
    val englishText: String,
    val priority: SpeechPriority,
)

sealed interface SpeakerState {
    data object Starting : SpeakerState

    /** @param voices Languages the device can speak. Anything else falls back to English. */
    data class Ready(
        val voices: Set<Language>,
    ) : SpeakerState

    /** No speech engine. The trip carries on with haptics only. */
    data object Unavailable : SpeakerState
}

interface Speaker {
    val state: StateFlow<SpeakerState>

    /** Sentences that arrive while the engine is starting are held and spoken once it is ready. */
    fun speak(utterance: Utterance)

    /** Lets the sentences already queued finish, then releases the engine. */
    fun shutdown()
}
