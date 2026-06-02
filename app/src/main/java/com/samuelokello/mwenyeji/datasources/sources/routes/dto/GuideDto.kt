package com.samuelokello.mwenyeji.datasources.sources.routes.dto

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.samuelokello.mwenyeji.data.models.Guide
import com.samuelokello.mwenyeji.data.models.RouteStep
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.data.models.TimeOfDay

/**
 * Firestore DTO for /routes/{routeId}/guides/{guideId} documents.
 *
 * A guide is one contributor's local knowledge for navigating a route.
 * Multiple guides can exist per route.
 */
@Keep
data class GuideDto(
    val id: String = "",
    val routeId: String = "",
    val fareKsh: Double? = null,
    val bestTimeOfDay: String = "ANYTIME",
    val timingReason: String = "",
    val steps: List<Map<String, Any>> = emptyList(),
    val warnings: String = "",
    val tags: List<String> = emptyList(),
    val sacco: String = "",
    val contributorId: String = "",
    val confirmedCount: Int = 0,
    val didntWorkCount: Int = 0,
    val outdatedCount: Int = 0,
    val lastConfirmedAt: Timestamp? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
)

fun GuideDto.toDomain(): Guide =
    Guide(
        id = id,
        routeId = routeId,
        fareKsh = fareKsh,
        bestTimeOfDay =
            TimeOfDay.entries.firstOrNull { it.name == bestTimeOfDay }
                ?: TimeOfDay.ANYTIME,
        timingReason = timingReason,
        steps = steps.mapNotNull { it.toRouteStep() },
        warnings = warnings,
        tags =
            tags
                .mapNotNull { tag ->
                    RouteTag.entries.firstOrNull { it.name == tag }
                }.toSet(),
        sacco = sacco,
        contributorId = contributorId,
        confirmedCount = confirmedCount,
        didntWorkCount = didntWorkCount,
        outdatedCount = outdatedCount,
        lastConfirmedAt = lastConfirmedAt?.toDate()?.time,
        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
    )

fun Guide.toDto(): GuideDto =
    GuideDto(
        id = id,
        routeId = routeId,
        fareKsh = fareKsh,
        bestTimeOfDay = bestTimeOfDay.name,
        timingReason = timingReason,
        steps = steps.map { mapOf("order" to it.order, "instruction" to it.instruction) },
        warnings = warnings,
        tags = tags.map { it.name },
        sacco = sacco,
        contributorId = contributorId,
        confirmedCount = confirmedCount,
        didntWorkCount = didntWorkCount,
        outdatedCount = outdatedCount,
    )

private fun Map<String, Any>.toRouteStep(): RouteStep? {
    val order =
        (this["order"] as? Long)?.toInt()
            ?: (this["order"] as? Int)
            ?: return null
    val instruction = this["instruction"] as? String ?: return null
    return RouteStep(order = order, instruction = instruction)
}
