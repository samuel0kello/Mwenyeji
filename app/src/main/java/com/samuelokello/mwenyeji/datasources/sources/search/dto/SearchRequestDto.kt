package com.samuelokello.mwenyeji.datasources.sources.search.dto

data class SearchRequestDto(
    val query: String,
    val limit: Int = 5,
    /**
     * Optional proximity bias. When null, backends use their default (none or configured).
     */
    val proximity: CoordinatesDto? = null,
    /**
     * Optional bounding box to constrain results.
     */
    val boundingBox: BoundingBox? = null,
    val countryCode: String? = null,
    val language: String = "en",
)

data class SearchResultDto(
    val id: String,
    val name: String,
    val fullAddress: String?,
    val distanceMeters: Double?,
    val coordinates: CoordinatesDto? = null,
)

data class CoordinatesDto(
    val latitude: Double,
    val longitude: Double,
)

data class BoundingBox(
    val minLng: Double,
    val minLat: Double,
    val maxLng: Double,
    val maxLat: Double,
)
