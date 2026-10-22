package com.samuelokello.mwenyeji.feature.planner.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaceDirectoryTest {
    private val directory =
        PlaceDirectory(
            listOf(
                Place("Kencom/Ambassadeur", -1.2846, 36.8261),
                Place("Marigu-ini", -1.2600, 36.7400),
                Place("Embakasi Village", -1.3200, 36.9000),
                Place("Embakasi", -1.3150, 36.8950),
                Place("Kikuyu Terminus", -1.2467, 36.6651),
            ),
        )

    @Test
    fun `stop names match however they are written`() {
        assertEquals("Marigu-ini", directory.find("mariguini")?.name)
        assertEquals("Kikuyu Terminus", directory.find("KIKUYU terminus")?.name)
    }

    @Test
    fun `each part of a combined stop name finds it`() {
        assertEquals("Kencom/Ambassadeur", directory.find("Kencom")?.name)
        assertEquals("Kencom/Ambassadeur", directory.find("ambassadeur")?.name)
    }

    @Test
    fun `an exact name beats a longer one, and a start of a name finds the shortest`() {
        assertEquals("Embakasi", directory.find("embakasi")?.name)
        assertEquals("Kikuyu Terminus", directory.find("kikuyu")?.name)
    }

    @Test
    fun `nairobi nicknames find their places`() {
        assertEquals("Nairobi CBD", directory.find("tao")?.name)
        assertEquals("Archives", directory.find("Archives")?.name)
        assertEquals("Westlands", directory.find("westie")?.name)
    }

    @Test
    fun `small misspellings are forgiven, wild guesses are not`() {
        assertEquals("Embakasi", directory.find("embakassi")?.name)
        assertNull(directory.find("mombasa"))
    }
}
