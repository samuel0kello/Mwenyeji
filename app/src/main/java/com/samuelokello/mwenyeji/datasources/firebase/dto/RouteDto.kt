package com.samuelokello.mwenyeji.datasources.firebase.dto

import androidx.annotation.Keep
import com.google.firebase.Timestamp

/**
 * Firestore-compatible DTO for Route.
 */
@Keep
data class RouteDto(
    val id: String = "",
    val from: String = "",
    val to: String = "",
    val via: String = "",
    val fareKsh: Double? = null,
    // Coordinates
    val fromLat: Double? = null,
    val fromLng: Double? = null,
    val toLat: Double? = null,
    val toLng: Double? = null,
    // Route identity
    val routeNumber: String? = null,
    val saccos: List<String> = emptyList(),
    val bestTimeOfDay: String = "ANYTIME",
    val timingReason: String = "",
    val steps: List<Map<String, Any>> = emptyList(),
    val warnings: String = "",
    val tags: List<String> = emptyList(),
    val contributorId: String = "",
    val confirmedCount: Int = 0,
    val didntWorkCount: Int = 0,
    val outdatedCount: Int = 0,
    val lastConfirmedAt: Timestamp? = null,
    val createdAt: Timestamp? = null,
)
