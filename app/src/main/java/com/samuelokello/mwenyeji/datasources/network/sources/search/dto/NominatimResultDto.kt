package com.samuelokello.mwenyeji.datasources.network.sources.search.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimResultDto(
    @SerialName("place_id") val placeId: Long = 0,
    @SerialName("display_name") val displayName: String = "",
    @SerialName("name") val name: String? = null,
    @SerialName("lat") val lat: String = "",
    @SerialName("lon") val lon: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("importance") val importance: Double = 0.0,
)
