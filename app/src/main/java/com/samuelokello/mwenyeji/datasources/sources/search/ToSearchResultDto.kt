package com.samuelokello.mwenyeji.datasources.sources.search

import com.samuelokello.mwenyeji.datasources.sources.search.dto.CoordinatesDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.NominatimResultDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.PhotonFeatureDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.SearchResultDto

internal fun NominatimResultDto.toSearchResultDto(): SearchResultDto =
    SearchResultDto(
        id = placeId.toString(),
        name = name ?: displayName.substringBefore(","),
        fullAddress = displayName,
        distanceMeters = null, // Nominatim doesn't provide this
        coordinates =
            CoordinatesDto(
                latitude = lat.toDoubleOrNull() ?: 0.0,
                longitude = lon.toDoubleOrNull() ?: 0.0,
            ),
    )

/** Null for a place with no name or no point: there is nothing to show or go to. */
internal fun PhotonFeatureDto.toSearchResultDto(): SearchResultDto? {
    val name = properties.name ?: return null
    val (longitude, latitude) = geometry.coordinates.takeIf { it.size >= 2 } ?: return null
    val address =
        listOf(properties.street, properties.locality, properties.district, properties.city ?: properties.county, properties.state)
            .filterNotNull()
            .filter { it != name }
            .distinct()
            .joinToString(", ")
    return SearchResultDto(
        // OSM ids are only unique per type (node, way, relation).
        id = "${properties.osmType}${properties.osmId}",
        name = name,
        fullAddress = address.ifBlank { null },
        distanceMeters = null,
        coordinates = CoordinatesDto(latitude = latitude, longitude = longitude),
    )
}
