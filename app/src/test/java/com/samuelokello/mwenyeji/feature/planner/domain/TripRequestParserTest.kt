package com.samuelokello.mwenyeji.feature.planner.domain

import com.samuelokello.mwenyeji.feature.planner.domain.TripRequestParser.ParsedTrip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TripRequestParserTest {
    private fun parse(text: String) = TripRequestParser.parse(text)

    @Test
    fun `the headline request goes from here`() {
        assertEquals(ParsedTrip(null, "embakasi"), parse("From my location I want to go to Embakasi"))
    }

    @Test
    fun `english trigger phrases`() {
        assertEquals(ParsedTrip(null, "tao"), parse("Take me to tao please"))
        assertEquals(ParsedTrip(null, "kencom"), parse("How do I get to Kencom stage?"))
        assertEquals(ParsedTrip(null, "westlands"), parse("I'm going to Westlands"))
    }

    @Test
    fun `from somewhere to somewhere`() {
        assertEquals(ParsedTrip("westlands", "embakasi"), parse("from Westlands to Embakasi"))
        assertEquals(ParsedTrip("ngara", "kikuyu terminus"), parse("How do I get from Ngara to Kikuyu Terminus"))
        assertEquals(ParsedTrip(null, "odeon"), parse("from here to Odeon"))
    }

    @Test
    fun `swahili and sheng trigger phrases`() {
        assertEquals(ParsedTrip(null, "rongai"), parse("Nataka kwenda Rongai"))
        assertEquals(ParsedTrip(null, "westie"), parse("nipeleke westie"))
        assertEquals(ParsedTrip("ngara", "kencom"), parse("Kutoka Ngara hadi Kencom"))
    }

    @Test
    fun `a bare place name is the destination`() {
        assertEquals(ParsedTrip(null, "kikuyu terminus"), parse("Kikuyu Terminus"))
    }

    @Test
    fun `a sentence with no place is not a trip`() {
        assertNull(parse("what is the weather like in the city today"))
        assertNull(parse("   "))
    }
}
