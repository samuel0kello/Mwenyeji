package com.samuelokello.mwenyeji.data.models

/**
 * Time-of-day filter for guides and feed chips.
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
 * Guide tags — contributor selects all that apply.
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
 * A single step in a contributor's guide.
 */
data class RouteStep(
    val order: Int,
    val instruction: String,
)

/**
 * Confidence level derived from community verdicts across all guides.
 */
enum class RouteConfidence {
    HIGH, // green  — many confirmations, no outdated flags
    MEDIUM, // amber  — some confirmations or minor concerns
    STALE, // red    — more outdated flags than confirmations
    UNVERIFIED, // grey   — no community feedback yet
}

/**
 * A Nairobi matatu route from the Digital Matatus GTFS dataset.
 *
 * This model contains only official route data — GPS coordinates,
 * stop counts, headway, route number. Community knowledge (steps,
 * warnings, fare) lives in Guide objects attached to this route.
 *
 * The feed shows BoardableRoute (this model + user's boarding context).
 * The route detail screen shows this model + its list of Guides.
 */
data class Route(
    val id: String = "",
    val routeNumber: String? = null,
    val longName: String? = null, // raw GTFS long name
    val from: String = "", // origin terminus name
    val to: String = "", // destination terminus name
    val via: String = "", // middle stages description
    val terminus1Lat: Double? = null,
    val terminus1Lng: Double? = null,
    val terminus1Geohash: String? = null,
    val terminus2Lat: Double? = null,
    val terminus2Lng: Double? = null,
    val terminus2Geohash: String? = null,
    val firstStopId: String? = null,
    val lastStopId: String? = null,
    val stopCount: Int = 0,
    val outboundShapeId: String? = null,
    val inboundShapeId: String? = null,
    val peakHeadwayMins: Int? = null,
    val offPeakHeadwayMins: Int? = null,
    val searchTerms: List<String> = emptyList(),
    // These are totals across ALL guides attached to this route.
    val guideCount: Int = 0, // how many guides exist
    val confirmedCount: Int = 0,
    val didntWorkCount: Int = 0,
    val outdatedCount: Int = 0,
    val lastConfirmedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val hasGuides: Boolean
        get() = guideCount > 0

    val formattedGuideCount: String
        get() =
            when (guideCount) {
                0 -> "No guides yet"
                1 -> "1 guide"
                else -> "$guideCount guides"
            }

    val confidence: RouteConfidence
        get() =
            when {
                confirmedCount >= 5 && outdatedCount == 0 -> RouteConfidence.HIGH
                confirmedCount >= 1 && outdatedCount <= 1 -> RouteConfidence.MEDIUM
                outdatedCount > confirmedCount -> RouteConfidence.STALE
                else -> RouteConfidence.UNVERIFIED
            }

    /**
     * Subtitle for the route card — stop count and peak frequency.
     * Shown below the route number and from → to.
     */
    val subtitle: String
        get() =
            when {
                stopCount > 0 && peakHeadwayMins != null -> {
                    "$stopCount stages · every ${peakHeadwayMins}min peak"
                }

                stopCount > 0 -> {
                    "$stopCount stages"
                }

                via.isNotBlank() -> {
                    "via $via"
                }

                else -> {
                    ""
                }
            }

    /**
     * Convenience for ProximityEngine — both termini as coordinate pairs.
     * Returns null for each terminus if coordinates are missing.
     */
    val terminus1: Pair<Double, Double>?
        get() =
            if (terminus1Lat != null && terminus1Lng != null) {
                terminus1Lat to terminus1Lng
            } else {
                null
            }

    val terminus2: Pair<Double, Double>?
        get() =
            if (terminus2Lat != null && terminus2Lng != null) {
                terminus2Lat to terminus2Lng
            } else {
                null
            }
}
