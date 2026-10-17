package com.samuelokello.mwenyeji.feature.guidance.announcer

import com.samuelokello.mwenyeji.feature.guidance.session.Message
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhraseBookTest {
    // One sample of every message. The first test fails if a new member is missing here.
    private val samples = listOf(
        Message.WalkToStage(200, "Kencom"),
        Message.AtStage("Super Metro", "Ngara", 100),
        Message.WalkingAwayFromStage,
        Message.BackOnTrack,
        Message.OnBoardConfirmed(3, "Ngara"),
        Message.BoardingDetected,
        Message.GetReady("Ngara"),
        Message.WrongBus("Ngara"),
        Message.Alight("Ngara", 200),
        Message.Arrived("Ngara"),
        Message.MissedStop,
        Message.OffBus("Ngara", 400),
        Message.TripEnded,
        Message.GuideStopped,
        Message.LostLocation,
    )

    @Test
    fun `every message has a sentence in every language`() {
        val members = Message::class.java.declaredClasses.filter { Message::class.java.isAssignableFrom(it) }.toSet()
        assertEquals(members, samples.map { it.javaClass }.toSet())
        Language.entries.forEach { language ->
            samples.forEach { message ->
                assertTrue("$message in $language", PhraseBook.phraseFor(message, language).isNotBlank())
            }
        }
    }

    @Test
    fun `values fill the blanks`() {
        assertEquals(
            "Walk 200 metres to Kencom stage",
            PhraseBook.phraseFor(Message.WalkToStage(200, "Kencom"), Language.ENGLISH),
        )
        assertEquals(
            "Alight at Ngara in 200 metres",
            PhraseBook.phraseFor(Message.Alight("Ngara", 200), Language.ENGLISH),
        )
    }

    @Test
    fun `fare is only mentioned when known`() {
        assertEquals(
            "You're at the stage. Look for Super Metro to Ngara, fare around 100",
            PhraseBook.phraseFor(Message.AtStage("Super Metro", "Ngara", 100), Language.ENGLISH),
        )
        assertEquals(
            "You're at the stage. Look for Super Metro to Ngara",
            PhraseBook.phraseFor(Message.AtStage("Super Metro", "Ngara", null), Language.ENGLISH),
        )
    }
}
