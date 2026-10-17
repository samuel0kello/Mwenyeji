package com.samuelokello.mwenyeji.datasources.sources.learning.dto

import androidx.annotation.Keep

/**
 * DTO for /route_variants/{routeId}_{direction}_{n}: a road matatus on a route were seen taking
 * instead of the main line, in at least a few riders' traces. Points are maps of lat and lng.
 */
@Keep
data class RouteVariantDto(
    val routeId: String = "",
    val direction: String = "",
    val points: List<Map<String, Any>> = emptyList(),
    val tripCount: Long = 0,
    val hours: List<Long> = emptyList(),
)
