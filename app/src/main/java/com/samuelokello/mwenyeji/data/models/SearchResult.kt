package com.samuelokello.mwenyeji.data.models

data class SearchResult(
    val id: String,
    val name: String,
    val fullAddress: String?,
    val distanceMeters: Double?,
    val coordinates: Coordinates? = null,
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)

data class SearchRequest(
    val query: String,
    val limit: Int = 5,
)
