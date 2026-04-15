package com.samuelokello.mwenyeji.datasources.network.sources.search.dto

data class SearchRequestDto(
    val query: String,
    val limit: Int = 5,
)

data class SearchResultDto(
    val id: String,
    val name: String,
    val fullAddress: String?,
    val distanceMeters: Double?,
    val lat: Double? = null,
    val lng: Double? = null,
)
