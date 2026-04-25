package com.samuelokello.mwenyeji.data.repository

import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.toDataResult
import com.samuelokello.mwenyeji.data.mappers.toDomain
import com.samuelokello.mwenyeji.data.mappers.toDomainList
import com.samuelokello.mwenyeji.data.mappers.toDto
import com.samuelokello.mwenyeji.data.models.SearchRequest
import com.samuelokello.mwenyeji.data.models.SearchResult
import com.samuelokello.mwenyeji.datasources.sources.search.SearchRemoteDataSource

interface SearchRepository {
    suspend fun search(request: SearchRequest): DataResult<List<SearchResult>>

    suspend fun select(id: String): DataResult<SearchResult>
}

internal class SearchRepositoryImpl(
    private val searchDataSource: SearchRemoteDataSource,
) : SearchRepository {
    override suspend fun search(request: SearchRequest): DataResult<List<SearchResult>> =
        searchDataSource.search(request.toDto()).toDataResult { it.toDomainList() }

    override suspend fun select(id: String): DataResult<SearchResult> = searchDataSource.selectResult(id).toDataResult { it.toDomain() }
}
