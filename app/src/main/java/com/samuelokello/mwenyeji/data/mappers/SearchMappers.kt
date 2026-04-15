package com.samuelokello.mwenyeji.data.mappers

import com.samuelokello.mwenyeji.data.models.SearchRequest
import com.samuelokello.mwenyeji.data.models.SearchResult
import com.samuelokello.mwenyeji.datasources.network.sources.search.dto.NominatimResultDto
import com.samuelokello.mwenyeji.datasources.network.sources.search.dto.SearchRequestDto
import com.samuelokello.mwenyeji.datasources.network.sources.search.dto.SearchResultDto

fun SearchRequest.toDto(): SearchRequestDto =
    SearchRequestDto(
        query = query,
        limit = limit,
    )

fun SearchResult.toDto(): SearchResultDto =
    SearchResultDto(
        id = id,
        name = name,
        fullAddress = fullAddress,
        distanceMeters = distanceMeters,
        lat = lat,
        lng = lng,
    )

fun SearchRequestDto.toDomain(): SearchRequest =
    SearchRequest(
        query = query,
        limit = limit,
    )

fun SearchResultDto.toDomain(): SearchResult =
    SearchResult(
        id = id,
        name = name,
        fullAddress = fullAddress,
        distanceMeters = distanceMeters,
        lat = lat,
        lng = lng,
    )

fun NominatimResultDto.toDto() =
    SearchResultDto(
        id = placeId.toString(),
        name = name ?: displayName.split(",").first().trim(),
        fullAddress = displayName,
        distanceMeters = null, // Nominatim doesn't return distance
        lat = lat.toDoubleOrNull(),
        lng = lon.toDoubleOrNull(),
    )

fun List<SearchResultDto>.toDomainList(): List<SearchResult> = map { it.toDomain() }

fun List<SearchResult>.toDto(): List<SearchResultDto> = map { it.toDto() }
