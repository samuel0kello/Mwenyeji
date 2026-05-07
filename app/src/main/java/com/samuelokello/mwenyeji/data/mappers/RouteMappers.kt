package com.samuelokello.mwenyeji.data.mappers

import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStep
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.datasources.sources.routes.dto.RouteDto

/**
 * Converts a Firestore DTO to the domain Route model.
 * Works for both "community" and "digital_matatus" source types.
 */
fun RouteDto.toDomain(): Route {
    val resolvedFromLat = terminus1Lat ?: fromLat
    val resolvedFromLng = terminus1Lng ?: fromLng
    val resolvedToLat = terminus2Lat ?: toLat
    val resolvedToLng = terminus2Lng ?: toLng

    // Handle sacco being either a String or a List<String> from Firestore
    val resolvedSacco =
        when (val s = sacco) {
            is List<*> -> s.filterIsInstance<String>()
            is String -> if (s.isBlank()) emptyList() else listOf(s)
            else -> emptyList()
        }

    return Route(
        id = id,
        from = from,
        to = to,
        via = via,
        fareKsh = fareKsh,
        fromLat = resolvedFromLat,
        fromLng = resolvedFromLng,
        toLat = resolvedToLat,
        toLng = resolvedToLng,
        routeNumber = routeNumber,
        sacco = resolvedSacco,
        searchTerms = searchTerms,
        stopCount = stopCount,
        peakHeadwayMins = peakHeadwayMins,
        offPeakHeadwayMins = offPeakHeadwayMins,
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
        contributorId = contributorId,
        confirmedCount = confirmedCount,
        didntWorkCount = didntWorkCount,
        outdatedCount = outdatedCount,
        lastConfirmedAt = lastConfirmedAt?.toDate()?.time,
        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
        source = source,
        isEnriched = isEnriched,
    )
}

/**
 * Converts the domain Route to a DTO for writing back to Firestore.
 * Used when a contributor enriches a GTFS route or submits a new one.
 */
fun Route.toDto(): RouteDto =
    RouteDto(
        id = id,
        from = from,
        to = to,
        via = via,
        fareKsh = fareKsh,
        fromLat = fromLat,
        fromLng = fromLng,
        toLat = toLat,
        toLng = toLng,
        routeNumber = routeNumber,
        sacco = sacco,
        bestTimeOfDay = bestTimeOfDay.name,
        timingReason = timingReason,
        steps = steps.map { mapOf("order" to it.order, "instruction" to it.instruction) },
        warnings = warnings,
        tags = tags.map { it.name },
        contributorId = contributorId,
        confirmedCount = confirmedCount,
        didntWorkCount = didntWorkCount,
        outdatedCount = outdatedCount,
        source = source,
        isEnriched = steps.isNotEmpty() || fareKsh != null || warnings.isNotBlank(),
    )

private fun Map<String, Any>.toRouteStep(): RouteStep? {
    val order =
        (this["order"] as? Long)?.toInt()
            ?: (this["order"] as? Int)
            ?: return null
    val instruction =
        this["instruction"] as? String
            ?: return null
    return RouteStep(order = order, instruction = instruction)
}
