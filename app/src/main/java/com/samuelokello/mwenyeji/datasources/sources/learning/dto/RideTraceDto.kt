package com.samuelokello.mwenyeji.datasources.sources.learning.dto

import androidx.annotation.Keep
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * DTO for /ride_traces/{auto id}. [createdAt] is set by the server and only used to delete old traces.
 * Points are maps of lat, lng, t (seconds from the start) and v (metres per second).
 */
@Keep
data class RideTraceDto(
    val routeId: String = "",
    val direction: String = "",
    val boardingStopId: String = "",
    val alightStopId: String = "",
    val weekday: Int = 0,
    val hour: Int = 0,
    val points: List<Map<String, Any>> = emptyList(),
    @ServerTimestamp val createdAt: Date? = null,
)
