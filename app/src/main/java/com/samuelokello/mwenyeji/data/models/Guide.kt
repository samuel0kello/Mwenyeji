package com.samuelokello.mwenyeji.data.models

/**
 * A community guide attached to a GTFS route.
 *
 * Stored in Firestore as /routes/{routeId}/guides/{guideId}.
 *
 * A route can have multiple guides from different contributors.
 * Each guide is one person's local knowledge for navigating that route —
 * which stage to board at, what to say to the conductor, where to alight,
 * how much to pay, what time of day it works best.
 *
 * The route detail screen shows all guides for a route, sorted by
 * confirmedCount descending (most trusted guide first).
 */
data class Guide(
    val id: String = "",
    val routeId: String = "", // parent route this guide belongs to
    val fareKsh: Double? = null,
    val bestTimeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val timingReason: String = "",
    val steps: List<RouteStep> = emptyList(),
    val warnings: String = "",
    val tags: Set<RouteTag> = emptySet(),
    val sacco: String = "", // e.g. "City Hoppa", "Embassava"
    val contributorId: String = "",
    val confirmedCount: Int = 0,
    val didntWorkCount: Int = 0,
    val outdatedCount: Int = 0,
    val lastConfirmedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val hasSteps: Boolean
        get() = steps.isNotEmpty()

    val formattedFare: String?
        get() = fareKsh?.let { "Ksh ${it.toInt()}" }

    val confidence: RouteConfidence
        get() =
            when {
                confirmedCount >= 5 && outdatedCount == 0 -> RouteConfidence.HIGH
                confirmedCount >= 1 && outdatedCount <= 1 -> RouteConfidence.MEDIUM
                outdatedCount > confirmedCount -> RouteConfidence.STALE
                else -> RouteConfidence.UNVERIFIED
            }
}
