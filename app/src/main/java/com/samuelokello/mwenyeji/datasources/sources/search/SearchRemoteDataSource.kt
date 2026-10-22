package com.samuelokello.mwenyeji.datasources.sources.search

import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.sources.search.dto.SearchRequestDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.SearchResultDto

interface SearchRemoteDataSource {
    /**
     * Search for places matching [request].
     */
    suspend fun search(request: SearchRequestDto): NetworkResult<List<SearchResultDto>>

    /**
     * Resolve a result by id to one guaranteed to have coordinates populated.
     * Photon and Nominatim return coordinates with every result, so this is a lookup in the last search.
     */
    suspend fun selectResult(id: String): NetworkResult<SearchResultDto>
}
