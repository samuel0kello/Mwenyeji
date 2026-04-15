package com.samuelokello.mwenyeji.data.models

/**
 * Time-of-day filter — Step 2 "Best time of day" chips.
 * [displayName] is the human-readable label shown on the chip.
 */
enum class TimeOfDay(
    val displayName: String,
) {
    MORNING_RUSH("Morning rush"),
    MIDDAY("Midday"),
    EVENING_RUSH("Evening rush"),
    LATE_NIGHT("Late night"),
    ANYTIME("Anytime"),
}

/**
 * Route tags — Step 4 "Tags (select all that apply)".
 * Multiple can be selected, stored as a Set<RouteTag>.
 */
enum class RouteTag(
    val displayName: String,
) {
    CHEAP("Cheap"),
    FAST("Fast"),
    LESS_CROWDED("Less crowded"),
    AVOID_CBD("Avoid CBD"),
    RELIABLE("Reliable"),
}

/**
 * A single step in the "How do you do it?" instructions — Step 3.
 * Steps are ordered; the list index determines display order.
 *
 * @param order   1-based position (Step 1, Step 2, …).
 * @param instruction The contributor's text for this step.
 */
data class RouteStep(
    val order: Int,
    val instruction: String,
)

/**
 * A crowdsourced transit guide contributed by a Mwenyeji user.
 *
 * Maps 1-to-1 to the 4-step contribution flow:
 *
 *  Step 1 — Route:   [from], [to], [via], [fareKsh]
 *  Step 2 — Timing:  [bestTimeOfDay], [timingReason]
 *  Step 3 — Steps:   [steps]
 *  Step 4 — Warnings:[warnings], [tags]
 *
 * @param id                Unique identifier (UUID or Firestore doc ID).
 * @param from              Boarding location e.g. "CBD, Kencom".
 * @param to                Destination e.g. "Westlands, Sarit".
 * @param via               Route description e.g. "via Uhuru Highway".
 * @param fareKsh           Fare in Kenyan shillings. Null if contributor skipped.
 * @param bestTimeOfDay     When the route works best (single selection from Step 2).
 * @param timingReason      Optional explanation for the timing choice.
 * @param steps             Ordered list of instructions from Step 3.
 * @param warnings          Free-text warnings from Step 4 e.g. "Don't board from Tom Mboya".
 * @param tags              Set of tags selected in Step 4.
 * @param contributorId     UID of the user who submitted this guide.
 * @param confirmedCount    How many users confirmed this route works.
 * @param didntWorkCount    How many users said it didn't work.
 * @param outdatedCount     How many users flagged it as outdated.
 * @param lastConfirmedAt   Epoch millis of the most recent confirmation.
 * @param createdAt         Epoch millis when the guide was first submitted.
 */
data class Route(
    // Identity
    val id: String = "",
    // Step 1 — Route
    val from: String = "",
    val to: String = "",
    val via: String = "",
    val fareKsh: Double? = null,
    // Coordinates — captured from map search
    val fromLat: Double? = null,
    val fromLng: Double? = null,
    val toLat: Double? = null,
    val toLng: Double? = null,
    // Route identity
    val routeNumber: String? = null,
    val saccos: List<String> = emptyList(),
    // Step 2 — Timing
    val bestTimeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val timingReason: String = "",
    // Step 3 — Steps
    val steps: List<RouteStep> = emptyList(),
    // Step 4 — Warnings & Tags
    val warnings: String = "",
    val tags: Set<RouteTag> = emptySet(),
    // Metadata
    val contributorId: String = "",
    val confirmedCount: Int = 0,
    val didntWorkCount: Int = 0,
    val outdatedCount: Int = 0,
    val lastConfirmedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val title: String get() = "$from → $to"
    val isConfirmed: Boolean get() = confirmedCount > 0
    val hasCoordinates: Boolean
        get() =
            fromLat != null && fromLng != null && toLat != null && toLng != null

    val confidence: RouteConfidence
        get() =
            when {
                confirmedCount >= 5 && outdatedCount == 0 -> RouteConfidence.HIGH
                confirmedCount >= 1 && outdatedCount <= 1 -> RouteConfidence.MEDIUM
                outdatedCount > confirmedCount -> RouteConfidence.STALE
                else -> RouteConfidence.UNVERIFIED
            }

    val hasSteps: Boolean get() = steps.isNotEmpty()
    val formattedFare: String? get() = fareKsh?.let { "Ksh ${it.toInt()}" }
}

/**
 * Confidence level derived from community confirmations.
 * Drives the coloured dot on route cards.
 */
enum class RouteConfidence {
    HIGH, // green dot  — many confirmations, no outdated flags
    MEDIUM, // amber dot  — some confirmations or minor concerns
    STALE, // red dot    — more outdated flags than confirmations
    UNVERIFIED, // grey dot   — no community feedback yet
}
