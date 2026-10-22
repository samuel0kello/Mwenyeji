package com.samuelokello.mwenyeji.feature.planner.domain

/**
 * Finds a place by name with no network: every stop on every route, plus the names Nairobi actually
 * uses for places ("tao", "Archives") that are not stop names. Stop names like "Kencom/Ambassadeur"
 * answer to each of their parts. Close misspellings match too ("embakassi").
 */
class PlaceDirectory(
    stops: List<Place>,
    aliases: Map<String, Place> = NAIROBI_ALIASES,
) {
    private val byKey: Map<String, Place>
    private val keys: List<String>

    init {
        val entries = LinkedHashMap<String, Place>()
        aliases.forEach { (alias, place) -> entries.putIfAbsent(alias.key(), place) }
        stops.forEach { stop ->
            entries.putIfAbsent(stop.name.key(), stop)
            stop.name.split('/').map { it.key() }.filter { it.length >= 3 }.forEach { part -> entries.putIfAbsent(part, stop) }
        }
        byKey = entries
        keys = entries.keys.toList()
    }

    /** The best match for [name], or null when nothing is close enough. */
    fun find(name: String): Place? {
        val wanted = name.key()
        if (wanted.length < 2) return null
        byKey[wanted]?.let { return it }
        // "embakasi" finds "embakasivillage"; the shortest such name is the most general.
        keys.filter { it.startsWith(wanted) }.minByOrNull { it.length }?.let { return byKey[it] }
        if (wanted.length >= MIN_FUZZY_LENGTH) {
            keys
                .map { it to editDistance(it, wanted) }
                .filter { (_, distance) -> distance <= MAX_TYPOS }
                .minByOrNull { (_, distance) -> distance }
                ?.let { (key, _) -> return byKey[key] }
        }
        return null
    }

    private fun String.key(): String = lowercase().filter { it.isLetterOrDigit() }

    private fun editDistance(
        a: String,
        b: String,
    ): Int {
        if (kotlin.math.abs(a.length - b.length) > MAX_TYPOS) return MAX_TYPOS + 1
        var previous = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val current = IntArray(b.length + 1)
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(current[j - 1] + 1, previous[j] + 1, previous[j - 1] + cost)
            }
            previous = current
        }
        return previous[b.length]
    }

    companion object {
        private const val MIN_FUZZY_LENGTH = 5
        private const val MAX_TYPOS = 2

        private val CBD = Place("Nairobi CBD", -1.2841, 36.8250)

        /** Names riders use that are not stop names. Add to this as riders teach us. */
        val NAIROBI_ALIASES: Map<String, Place> =
            mapOf(
                "tao" to CBD,
                "town" to CBD,
                "taon" to CBD,
                "cbd" to CBD,
                "archives" to Place("Archives", -1.2840, 36.8262),
                "jkia" to Place("JKIA", -1.3322, 36.9264),
                "airport" to Place("JKIA", -1.3322, 36.9264),
                "knh" to Place("Kenyatta National Hospital", -1.2975, 36.8047),
                "westie" to Place("Westlands", -1.2622, 36.8037),
                "westi" to Place("Westlands", -1.2622, 36.8037),
                "easti" to Place("Eastleigh", -1.2723, 36.8488),
            )
    }
}
