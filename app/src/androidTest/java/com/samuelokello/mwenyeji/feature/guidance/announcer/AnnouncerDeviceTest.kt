package com.samuelokello.mwenyeji.feature.guidance.announcer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.samuelokello.mwenyeji.feature.guidance.session.Message
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripEffect
import com.samuelokello.mwenyeji.feature.guidance.session.VibrationPattern
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs on a real phone with its volume up: listen and feel rather than read.
 * Many emulators have no speech engine, so the readiness test fails there.
 */
@RunWith(AndroidJUnit4::class)
class AnnouncerDeviceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var speaker: TtsSpeaker

    private fun startSpeaker(): TtsSpeaker {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { speaker = TtsSpeaker(context) }
        return speaker
    }

    private fun awaitReady(): SpeakerState.Ready =
        runBlocking {
            withTimeout(10_000) { speaker.state.first { it !is SpeakerState.Starting } } as SpeakerState.Ready
        }

    @After
    fun tearDown() {
        if (::speaker.isInitialized) speaker.shutdown()
    }

    @Test
    fun engineStartsWithAnEnglishVoice() {
        startSpeaker()

        val ready = awaitReady()

        assertTrue("Voices: ${ready.voices}", Language.ENGLISH in ready.voices)
    }

    /** You should hear every sentence in order, each after its buzz. */
    @Test
    fun speaksEveryMessage() {
        startSpeaker()
        val announcer = Announcer(speaker, VibratorHaptics(context), Language.ENGLISH)
        awaitReady()

        samples.forEach { message ->
            announcer.announce(listOf(TripEffect.Vibrate(VibrationPattern.TICK), TripEffect.Speak(message)))
            Thread.sleep(5_000)
        }
    }

    /** You should hear "Get ready" cut off by "Alight", and "Alight" finish in full. */
    @Test
    fun alightInterruptsGetReady() {
        startSpeaker()
        val announcer = Announcer(speaker, VibratorHaptics(context), Language.ENGLISH)
        awaitReady()

        announcer.announce(listOf(TripEffect.Speak(Message.GetReady("Ngara"))))
        Thread.sleep(800)
        announcer.announce(listOf(TripEffect.Vibrate(VibrationPattern.ALIGHT), TripEffect.Speak(Message.Alight("Ngara", 200))))
        Thread.sleep(800)
        // Urgent never cuts off urgent: this waits until "Alight" is done.
        announcer.announce(listOf(TripEffect.Speak(Message.MissedStop)))
        Thread.sleep(10_000)
    }

    /** "You've arrived" must play to the end even though the trip ends straight after it. */
    @Test
    fun lastSentenceFinishesAfterClose() {
        startSpeaker()
        val announcer = Announcer(speaker, VibratorHaptics(context), Language.ENGLISH)
        awaitReady()

        announcer.announce(listOf(TripEffect.Vibrate(VibrationPattern.MILESTONE), TripEffect.Speak(Message.Arrived("Ngara"))))
        announcer.close()
        Thread.sleep(5_000)
    }

    /** The Swahili wording is still English text; this checks which voice the phone has. */
    @Test
    fun swahiliFallsBackToEnglishWithoutAVoice() {
        startSpeaker()
        val ready = awaitReady()
        val announcer = Announcer(speaker, VibratorHaptics(context), Language.SWAHILI)

        announcer.announce(listOf(TripEffect.Speak(Message.GetReady("Ngara"))))
        Thread.sleep(5_000)
        println("Swahili voice installed: ${Language.SWAHILI in ready.voices}")
    }

    private val samples = listOf(
        Message.WalkToStage(200, "Kencom"),
        Message.AtStage("Super Metro", "Ngara", 100),
        Message.WalkingAwayFromStage,
        Message.OnBoardConfirmed(3, "Ngara"),
        Message.GetReady("Ngara"),
        Message.WrongBus("Ngara"),
        Message.Alight("Ngara", 200),
        Message.Arrived("Ngara"),
        Message.MissedStop,
        Message.OffBus("Ngara", 400),
        Message.LostLocation,
    )
}
