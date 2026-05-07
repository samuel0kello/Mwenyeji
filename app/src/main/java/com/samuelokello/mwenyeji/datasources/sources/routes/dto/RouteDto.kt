package com.samuelokello.mwenyeji.datasources.sources.routes.dto

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore DTO for Route.
 *
 * Handles two document shapes that coexist in /routes:
 *
 *   source = "community" — contributed by beta testers, has steps/warnings/fare
 *   source = "digital_matatus" — seeded from GTFS, has routeNumber/terminus/stopCount
 *
 * All fields default to null/empty so Firestore deserialization never throws
 * on missing fields regardless of which source type the document is.
 */
@Keep
data class RouteDto(
    val id: String = "",
    val from: String = "",
    val to: String = "",
    val via: String = "",
    val fareKsh: Double? = null,
    val bestTimeOfDay: String = "ANYTIME",
    val timingReason: String = "",
    val steps: List<Map<String, Any>> = emptyList(),
    val warnings: String = "",
    val tags: List<String> = emptyList(),
    // sacco can be a List<String> (new) or String (legacy/seed)
    val sacco: Any? = emptyList<String>(),
    val confirmedCount: Int = 0,
    val didntWorkCount: Int = 0,
    val outdatedCount: Int = 0,
    val lastConfirmedAt: Timestamp? = null,
    val contributorId: String = "",
    val routeNumber: String? = null,
    val longName: String? = null,
    val stopCount: Int = 0,
    val firstStopId: String? = null,
    val lastStopId: String? = null,
    val terminus1Lat: Double? = null,
    val terminus1Lng: Double? = null,
    val terminus1Geohash: String? = null,
    val terminus2Lat: Double? = null,
    val terminus2Lng: Double? = null,
    val terminus2Geohash: String? = null,
    val outboundShapeId: String? = null,
    val inboundShapeId: String? = null,
    val peakHeadwayMins: Int? = null,
    val offPeakHeadwayMins: Int? = null,
    val searchTerms: List<String> = emptyList(),
    val fromLat: Double? = null,
    val fromLng: Double? = null,
    val toLat: Double? = null,
    val toLng: Double? = null,
    val source: String = "community",
    val isEnriched: Boolean = false,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
)
