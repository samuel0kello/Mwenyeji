package com.samuelokello.mwenyeji.data.mappers

import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStep
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.datasources.firebase.dto.RouteDto

/**
 * Converts domain [Route] → Firestore [RouteDto].
 * Called before writing to Firestore.
 */
fun Route.toDto(): RouteDto =
    RouteDto(
        id = id,
        from = from,
        to = to,
        via = via,
        fareKsh = fareKsh,
        bestTimeOfDay = bestTimeOfDay.name,
        timingReason = timingReason,
        steps =
            steps.map { step ->
                mapOf(
                    "order" to step.order,
                    "instruction" to step.instruction,
                )
            },
        warnings = warnings,
        tags = tags.map { it.name },
        contributorId = contributorId,
        confirmedCount = confirmedCount,
        didntWorkCount = didntWorkCount,
        outdatedCount = outdatedCount,
        fromLat = fromLat,
        fromLng = fromLng,
        toLat = toLat,
        toLng = toLng,
        routeNumber = routeNumber,
        saccos = saccos,
    )

/**
 * Converts Firestore [RouteDto] → domain [Route].
 * Called after reading from Firestore.
 */
fun RouteDto.toDomain(): Route =
    Route(
        id = id,
        from = from,
        to = to,
        via = via,
        fareKsh = fareKsh,
        bestTimeOfDay =
            runCatching {
                TimeOfDay.valueOf(bestTimeOfDay)
            }.getOrDefault(TimeOfDay.ANYTIME),
        timingReason = timingReason,
        steps =
            steps.mapIndexed { index, map ->
                RouteStep(
                    order = (map["order"] as? Long)?.toInt() ?: (index + 1),
                    instruction = map["instruction"] as? String ?: "",
                )
            },
        warnings = warnings,
        tags =
            tags
                .mapNotNull { tagName ->
                    runCatching { RouteTag.valueOf(tagName) }.getOrNull()
                }.toSet(),
        contributorId = contributorId,
        confirmedCount = confirmedCount,
        didntWorkCount = didntWorkCount,
        outdatedCount = outdatedCount,
        lastConfirmedAt = lastConfirmedAt?.toDate()?.time,
        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
        fromLat = fromLat,
        fromLng = fromLng,
        toLat = toLat,
        toLng = toLng,
        routeNumber = routeNumber,
        saccos = saccos,
    )
