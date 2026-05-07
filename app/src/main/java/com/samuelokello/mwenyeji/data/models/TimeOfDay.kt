package com.samuelokello.mwenyeji.data.models

/**
 * Time-of-day filter — Step 2 "Best time of day" chips.
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
 * Route tags — Step 4 multi-select chips.
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
 * A single step in the "How do you do it?" instructions.
 */
data class RouteStep(
    val order: Int,
    val instruction: String,
)

/**
 * Confidence level derived from community confirmations.
 * Drives the coloured dot on route cards.
 */
enum class RouteConfidence {
    HIGH, // green  — many confirmations, no outdated flags
    MEDIUM, // amber  — some confirmations or minor concerns
    STALE, // red    — more outdated flags than confirmations
    UNVERIFIED, // grey   — no community feedback yet
}

/**
 * Domain model for a Nairobi matatu route.
 *
 * Two source types coexist:
 *
 *   source = "community"       — contributed by a user through the contribute flow.
 *                                Has steps, warnings, fare, tags from day one.
 *                                Coordinates stored in fromLat/fromLng/toLat/toLng.
 *
 *   source = "digital_matatus" — seeded from the 2018 Digital Matatus GTFS dataset.
 *                                Has routeNumber, stopCount, headway data.
 *                                Coordinates in fromLat/fromLng (resolved from terminus1).
 *                                Community fields start empty — contributors enrich them.
 */
data class Route(
    val id: String = "",
    val from: String = "",
    val to: String = "",
    val via: String = "",
    val fareKsh: Double? = null,
    // Proximity coordinates — resolved from terminus1 (GTFS) or fromLat (community)
    val fromLat: Double? = null,
    val fromLng: Double? = null,
    val toLat: Double? = null,
    val toLng: Double? = null,
    // GTFS-only fields
    val routeNumber: String? = null,
    val stopCount: Int = 0,
    val peakHeadwayMins: Int? = null,
    val offPeakHeadwayMins: Int? = null,
    val searchTerms: List<String> = emptyList(),
    val sacco: List<String> = emptyList(),
    val bestTimeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val timingReason: String = "",
    val steps: List<RouteStep> = emptyList(),
    val warnings: String = "",
    val tags: Set<RouteTag> = emptySet(),
    val contributorId: String = "",
    val confirmedCount: Int = 0,
    val didntWorkCount: Int = 0,
    val outdatedCount: Int = 0,
    val lastConfirmedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val source: String = "community",
    val isEnriched: Boolean = false,
) {
    val title: String
        get() = if (routeNumber != null) "$routeNumber: $from → $to" else "$from → $to"

    val formattedFare: String?
        get() = fareKsh?.let { "Ksh ${it.toInt()}" }

    val isConfirmed: Boolean
        get() = confirmedCount > 0

    val isGtfsSeed: Boolean
        get() = source == "digital_matatus"

    /** Community routes that have steps/warnings/fare added on top of GTFS base. */
    val isCommunityEnriched: Boolean
        get() = isGtfsSeed && isEnriched

    val hasCoordinates: Boolean
        get() = fromLat != null && fromLng != null

    val hasSteps: Boolean
        get() = steps.isNotEmpty()

    /**
     * Confidence level for the route card dot.
     * GTFS-only routes (no community feedback) start as UNVERIFIED.
     */
    val confidence: RouteConfidence
        get() =
            when {
                confirmedCount >= 5 && outdatedCount == 0 -> RouteConfidence.HIGH
                confirmedCount >= 1 && outdatedCount <= 1 -> RouteConfidence.MEDIUM
                outdatedCount > confirmedCount -> RouteConfidence.STALE
                else -> RouteConfidence.UNVERIFIED
            }

    /**
     * Subtitle shown on route card below the title.
     * GTFS routes show stop count and headway. Community routes show via.
     */
    val subtitle: String
        get() =
            when {
                isGtfsSeed && stopCount > 0 && peakHeadwayMins != null -> {
                    "$stopCount stages · every ${peakHeadwayMins}min peak"
                }

                isGtfsSeed && stopCount > 0 -> {
                    "$stopCount stages"
                }

                via.isNotBlank() -> {
                    "via $via"
                }

                else -> {
                    ""
                }
            }
}
