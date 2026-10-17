package com.samuelokello.mwenyeji.feature.guidance.announcer

import com.samuelokello.mwenyeji.feature.guidance.session.Message
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripEffect
import com.samuelokello.mwenyeji.feature.guidance.session.VibrationPattern
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncerTest {
    private val calls = mutableListOf<String>()

    private val speaker = object : Speaker {
        val spoken = mutableListOf<Utterance>()
        var shutDown = false
        override val state: StateFlow<SpeakerState> = MutableStateFlow(SpeakerState.Ready(Language.entries.toSet()))

        override fun speak(utterance: Utterance) {
            spoken += utterance
            calls += "speak"
        }

        var stopped = false

        override fun stop() {
            stopped = true
        }

        override fun shutdown() {
            shutDown = true
        }
    }

    private val haptics = object : Haptics {
        val patterns = mutableListOf<VibrationPattern>()

        override fun vibrate(pattern: VibrationPattern) {
            patterns += pattern
            calls += "vibrate"
        }

        override fun stop() = Unit
    }

    private fun announcer(language: Language = Language.ENGLISH) = Announcer(speaker, haptics, language)

    @Test
    fun `with no private way to hear, sentences are dropped and buzzes still come`() {
        Announcer(speaker, haptics, Language.ENGLISH, canSpeak = { false }).announce(
            listOf(TripEffect.Vibrate(VibrationPattern.ALIGHT), TripEffect.Speak(Message.Alight("Ngara", 200))),
        )

        assertTrue(speaker.spoken.isEmpty())
        assertEquals(listOf(VibrationPattern.ALIGHT), haptics.patterns)
    }

    @Test
    fun `whether it may speak is asked at each sentence`() {
        var earphones = false
        val announcer = Announcer(speaker, haptics, Language.ENGLISH, canSpeak = { earphones })

        announcer.announce(listOf(TripEffect.Speak(Message.GetReady("Ngara"))))
        earphones = true
        announcer.announce(listOf(TripEffect.Speak(Message.GetReady("Ngara"))))

        assertEquals(1, speaker.spoken.size)
    }

    @Test
    fun `stop speaking silences the speaker`() {
        announcer().stopSpeaking()

        assertTrue(speaker.stopped)
    }

    @Test
    fun `speak and vibrate effects reach the speaker and haptics in order`() {
        announcer().announce(
            listOf(TripEffect.Vibrate(VibrationPattern.ALIGHT), TripEffect.Speak(Message.Alight("Ngara", 200))),
        )

        assertEquals(listOf("vibrate", "speak"), calls)
        assertEquals(listOf(VibrationPattern.ALIGHT), haptics.patterns)
        assertEquals(
            Utterance("Alight at Ngara in 200 metres", Language.ENGLISH, "Alight at Ngara in 200 metres", SpeechPriority.URGENT),
            speaker.spoken.single(),
        )
    }

    @Test
    fun `utterance carries the user's language and the english fallback`() {
        announcer(Language.SWAHILI).announce(listOf(TripEffect.Speak(Message.GetReady("Ngara"))))

        val utterance = speaker.spoken.single()
        assertEquals(Language.SWAHILI, utterance.language)
        assertEquals(PhraseBook.phraseFor(Message.GetReady("Ngara"), Language.SWAHILI), utterance.text)
        assertEquals("Get ready, Ngara is coming up", utterance.englishText)
        assertEquals(SpeechPriority.NORMAL, utterance.priority)
    }

    @Test
    fun `end of trip and permission requests are left to the trip runner`() {
        announcer().announce(listOf(TripEffect.RequestLocationPermission, TripEffect.EndTrip))

        assertTrue(calls.isEmpty())
        assertEquals(false, speaker.shutDown)
    }

    @Test
    fun `close shuts the speaker down`() {
        announcer().close()

        assertTrue(speaker.shutDown)
    }

    @Test
    fun `only messages that need action now, or warn the app cannot help, are urgent`() {
        val urgent = PhraseBookTest.samples.filter { it.priority == SpeechPriority.URGENT }.map { it::class }

        assertEquals(
            setOf(Message.WrongBus::class, Message.Alight::class, Message.MissedStop::class, Message.LostLocation::class),
            urgent.toSet(),
        )
    }
}
