package com.samuelokello.mwenyeji.data.repository

import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.mappers.toDomainList
import com.samuelokello.mwenyeji.data.mappers.toDto
import com.samuelokello.mwenyeji.data.models.SearchRequest
import com.samuelokello.mwenyeji.data.models.SearchResult
import com.samuelokello.mwenyeji.datasources.network.helpers.NetworkResult
import com.samuelokello.mwenyeji.datasources.network.sources.search.NominatimService

interface SearchRepository {
    suspend fun search(query: SearchRequest): DataResult<List<SearchResult>>

    suspend fun select(id: String): DataResult<SearchResult>
}

class SearchRepositoryImpl(
    private val nominatimDataSource: NominatimService,
) : SearchRepository {
    override suspend fun search(query: SearchRequest): DataResult<List<SearchResult>> =
        when (val result = nominatimDataSource.search(query.toDto())) {
            is NetworkResult.Success -> DataResult.Success(result.data.toDomainList())
            is NetworkResult.Error -> DataResult.Error(result.errorMessage)
        }

    override suspend fun select(id: String): DataResult<SearchResult> {
        // Nominatim returns coordinates directly in search response
        // select() is not needed — coordinates are already in SearchResult
        // This should never be called for Nominatim results
        return DataResult.Error("Select not supported for Nominatim — use coordinates from search result directly")
    }
}
