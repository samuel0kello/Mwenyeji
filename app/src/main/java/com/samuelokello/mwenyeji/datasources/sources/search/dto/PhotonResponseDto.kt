package com.samuelokello.mwenyeji.datasources.sources.search.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Photon's answer: a GeoJSON FeatureCollection of OpenStreetMap places. */
@Serializable
internal data class PhotonResponseDto(
    val features: List<PhotonFeatureDto> = emptyList(),
)

@Serializable
internal data class PhotonFeatureDto(
    val geometry: PhotonGeometryDto = PhotonGeometryDto(),
    val properties: PhotonPropertiesDto = PhotonPropertiesDto(),
)

/** [coordinates] is longitude, latitude, as in all GeoJSON. */
@Serializable
internal data class PhotonGeometryDto(
    val coordinates: List<Double> = emptyList(),
)

@Serializable
internal data class PhotonPropertiesDto(
    @SerialName("osm_type") val osmType: String = "",
    @SerialName("osm_id") val osmId: Long = 0,
    val name: String? = null,
    val street: String? = null,
    val locality: String? = null,
    val district: String? = null,
    val city: String? = null,
    val county: String? = null,
    val state: String? = null,
)
