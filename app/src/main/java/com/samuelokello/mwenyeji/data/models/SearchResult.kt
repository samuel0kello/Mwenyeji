package com.samuelokello.mwenyeji.data.models

data class SearchResult(
    val id: String,
    val name: String,
    val fullAddress: String?,
    val distanceMeters: Double?,
    val lat: Double? = null,
    val lng: Double? = null,
)

data class SearchRequest(
    val query: String,
    val limit: Int = 5,
)
