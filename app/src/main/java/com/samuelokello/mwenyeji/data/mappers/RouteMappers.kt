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
fun RouteDto.toDomain(): Route =
    Route(
        id = id,
        routeNumber = routeNumber,
        longName = longName,
        from = from,
        to = to,
        via = via,
        terminus1Lat = terminus1Lat,
        terminus1Lng = terminus1Lng,
        terminus1Geohash = terminus1Geohash,
        terminus2Lat = terminus2Lat,
        terminus2Lng = terminus2Lng,
        terminus2Geohash = terminus2Geohash,
        firstStopId = firstStopId,
        lastStopId = lastStopId,
        stopCount = stopCount,
        outboundShapeId = outboundShapeId,
        inboundShapeId = inboundShapeId,
        peakHeadwayMins = peakHeadwayMins,
        offPeakHeadwayMins = offPeakHeadwayMins,
        searchTerms = searchTerms,
        guideCount = guideCount,
        confirmedCount = confirmedCount,
        didntWorkCount = didntWorkCount,
        outdatedCount = outdatedCount,
        lastConfirmedAt = lastConfirmedAt?.toDate()?.time,
        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
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
