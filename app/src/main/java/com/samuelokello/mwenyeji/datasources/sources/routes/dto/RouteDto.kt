package com.samuelokello.mwenyeji.datasources.sources.routes.dto

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore DTO for /routes/{routeId} documents.
 *
 * Maps only to GTFS-seeded route documents. Community fields
 * (steps, warnings, fare, tags) are not read here — they live
 * in the /routes/{routeId}/guides subcollection (see GuideDto).
 *
 * All fields default to null/empty so Firestore deserialization
 * never throws on documents that are missing optional fields.
 */
@Keep
@IgnoreExtraProperties
data class RouteDto(
    // Identity — populated manually via .copy(id = snapshot.id)
    val id: String = "",
    val routeNumber: String? = null,
    val longName: String? = null,
    val from: String = "",
    val to: String = "",
    val via: String = "",
    // Termini coordinates — both ends stored for ProximityEngine
    val terminus1Lat: Double? = null,
    val terminus1Lng: Double? = null,
    val terminus1Geohash: String? = null,
    val terminus2Lat: Double? = null,
    val terminus2Lng: Double? = null,
    val terminus2Geohash: String? = null,
    // Stop summary
    val firstStopId: String? = null,
    val lastStopId: String? = null,
    val stopCount: Int = 0,
    // Shape references
    val outboundShapeId: String? = null,
    val inboundShapeId: String? = null,
    // Frequency
    val peakHeadwayMins: Int? = null,
    val offPeakHeadwayMins: Int? = null,
    // Search
    val searchTerms: List<String> = emptyList(),
    // Community aggregate signals — totals across all guides
    val guideCount: Int = 0,
    val confirmedCount: Int = 0,
    val didntWorkCount: Int = 0,
    val outdatedCount: Int = 0,
    val lastConfirmedAt: Timestamp? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
)
