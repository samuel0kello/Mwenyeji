package com.samuelokello.mwenyeji.datasources.sources.search

import com.samuelokello.mwenyeji.datasources.sources.search.dto.BoundingBox
import com.samuelokello.mwenyeji.datasources.sources.search.dto.CoordinatesDto

/**
 * App-level defaults for search. Injected via DI so tests can override
 * and future expansion (other cities) is trivial.
 */
data class SearchConfig(
    val defaultProximity: CoordinatesDto,
    val defaultBoundingBox: BoundingBox,
    val countryCode: String,
    val userAgent: String,
) {
    companion object {
        val NAIROBI =
            SearchConfig(
                defaultProximity = CoordinatesDto(latitude = -1.2921, longitude = 36.8219),
                defaultBoundingBox =
                    BoundingBox(
                        minLng = 36.6,
                        minLat = -1.5,
                        maxLng = 37.1,
                        maxLat = -1.1,
                    ),
                countryCode = "ke",
                userAgent = "Mwenyeji/1.0 (com.samuelokello.mwenyeji)",
            )
    }
}
