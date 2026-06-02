package com.samuelokello.mwenyeji.data.mappers

import com.samuelokello.mwenyeji.data.models.Coordinates
import com.samuelokello.mwenyeji.data.models.SearchRequest
import com.samuelokello.mwenyeji.data.models.SearchResult
import com.samuelokello.mwenyeji.datasources.sources.search.dto.CoordinatesDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.SearchRequestDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.SearchResultDto

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
        coordinates = coordinates?.toDto(),
    )

fun Coordinates.toDto(): CoordinatesDto =
    CoordinatesDto(
        latitude = latitude,
        longitude = longitude,
    )

fun CoordinatesDto.toDomain(): Coordinates =
    Coordinates(
        latitude = latitude,
        longitude = longitude,
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
        coordinates = coordinates?.toDomain(),
    )

fun List<SearchResultDto>.toDomainList(): List<SearchResult> = map { it.toDomain() }

fun List<SearchResult>.toDto(): List<SearchResultDto> = map { it.toDto() }
