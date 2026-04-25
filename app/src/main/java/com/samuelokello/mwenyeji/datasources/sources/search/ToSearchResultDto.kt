package com.samuelokello.mwenyeji.datasources.sources.search

import com.samuelokello.mwenyeji.datasources.sources.search.dto.CoordinatesDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.NominatimResultDto
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
