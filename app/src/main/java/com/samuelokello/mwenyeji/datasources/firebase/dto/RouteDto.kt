package com.samuelokello.mwenyeji.datasources.firebase.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.samuelokello.mwenyeji.data.models.TimeOfDay

/**
 * Firestore-compatible DTO for Route.
 */
data class RouteDto(
    @DocumentId
    val id: String = "",
    val from: String = "",
    val to: String = "",
    val via: String = "",
    val fareKsh: Double? = null,
    val bestTimeOfDay: String = TimeOfDay.ANYTIME.name,
    val timingReason: String = "",
    val steps: List<Map<String, Any>> = emptyList(),
    val warnings: String = "",
    val tags: List<String> = emptyList(),
    val contributorId: String = "",
    val confirmedCount: Int = 0,
    val didntWorkCount: Int = 0,
    val outdatedCount: Int = 0,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val lastConfirmedAt: Timestamp? = null,
)


