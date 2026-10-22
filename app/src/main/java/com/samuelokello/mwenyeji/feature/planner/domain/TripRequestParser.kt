package com.samuelokello.mwenyeji.feature.planner.domain

/**
 * Pulls a trip out of what the user typed or said, with no network and no language model: a few
 * trigger phrases per language around place names. "From my location I want to go to Embakasi",
 * "take me to tao", "kutoka Westlands hadi Embakasi", or just "Embakasi".
 */
object TripRequestParser {
    /** @param from Null means "from where I am". */
    data class ParsedTrip(
        val from: String?,
        val to: String,
    )

    fun parse(text: String): ParsedTrip? {
        var rest = text.lowercase().replace(Regex("[?!.,]"), " ").replace(Regex("\\s+"), " ").trim()
        if (rest.isEmpty()) return null

        // "From my location …" is the default anyway.
        HERE_PREFIXES.forEach { prefix -> rest = rest.removePrefix(prefix).trim() }

        // "from X to Y" in English and Swahili.
        FROM_TO.forEach { pattern ->
            pattern.find(rest)?.let { match ->
                val from = match.groupValues[1].clean()
                val to = afterTrigger(match.groupValues[2]) ?: match.groupValues[2].clean()
                if (to.isNotEmpty()) return ParsedTrip(from.takeUnless { it.isEmpty() || it in HERE }, to)
            }
        }

        afterTrigger(rest)?.let { return ParsedTrip(from = null, to = it) }
        // No trigger: the whole message is the destination, as long as it is not a sentence.
        val words = rest.clean()
        return if (words.isNotEmpty() && words.split(' ').size <= MAX_BARE_WORDS) ParsedTrip(from = null, to = words) else null
    }

    /** The place after the first trigger phrase in [text], if there is one. */
    private fun afterTrigger(text: String): String? {
        TO_TRIGGERS.forEach { trigger ->
            val at = text.indexOf(trigger)
            if (at >= 0) text.substring(at + trigger.length).clean().takeIf { it.isNotEmpty() }?.let { return it }
        }
        return null
    }

    private fun String.clean(): String {
        var place = trim()
        TRAILING.forEach { tail -> place = place.removeSuffix(tail).trim() }
        LEADING.forEach { head -> place = place.removePrefix(head).trim() }
        return place
    }

    private val FROM_TO =
        listOf(
            Regex("(?:^|\\s)from (.+?) to (.+)$"),
            Regex("(?:^|\\s)kutoka (.+?) (?:hadi|mpaka|kwenda|kuenda) (.+)$"),
        )

    private val HERE = setOf("here", "my location", "where i am", "hapa", "niko", "hapa nilipo")

    private val HERE_PREFIXES = listOf("from my location", "from here", "kutoka hapa")

    // Longest first, so "i want to go to" wins over "to".
    private val TO_TRIGGERS =
        listOf(
            "how do i get to",
            "i want to go to",
            "i'm going to",
            "im going to",
            "i am going to",
            "take me to",
            "directions to",
            "route to",
            "go to",
            "nataka kwenda",
            "nataka kuenda",
            "nipeleke",
            "naenda",
            "nafika aje",
            "nitafika aje",
            "nishow njia ya",
            "njia ya",
        ).sortedByDescending { it.length }

    private val LEADING = listOf("the ", "to ", "at ", "kwa ", "hadi ", "mpaka ")
    private val TRAILING = listOf(" please", " tafadhali", " stage", " now", " sasa")

    private const val MAX_BARE_WORDS = 4
}
