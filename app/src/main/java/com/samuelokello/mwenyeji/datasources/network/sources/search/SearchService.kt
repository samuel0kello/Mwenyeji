package com.samuelokello.mwenyeji.datasources.network.sources.search

import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.samuelokello.mwenyeji.datasources.network.helpers.NetworkResult
import com.samuelokello.mwenyeji.datasources.network.sources.search.dto.SearchRequestDto
import com.samuelokello.mwenyeji.datasources.network.sources.search.dto.SearchResultDto
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface SearchService {
    suspend fun search(request: SearchRequestDto): NetworkResult<List<SearchResultDto>>

    suspend fun select(id: String): NetworkResult<SearchResultDto>
}

class SearchServiceImpl(
    private val engine: SearchEngine,
) : SearchService {
    private var lastSuggestions: List<SearchSuggestion> = emptyList()

    override suspend fun search(request: SearchRequestDto): NetworkResult<List<SearchResultDto>> =
        suspendCancellableCoroutine { cont ->
            val task =
                engine.search(
                    request.query,
                    SearchOptions(
                        limit = request.limit,
                        proximity = Point.fromLngLat(36.8219, -1.2921),
                    ),
                    object : SearchSuggestionsCallback {
                        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                            val filtered =
                                suggestions.filter {
                                    it.type !is SearchSuggestionType.Category
                                }
                            lastSuggestions = filtered
                            cont.resume(NetworkResult.Success(filtered.map { it.toDto() }))
                        }

                        override fun onError(e: Exception) {
                            cont.resume(NetworkResult.Error(e.message ?: "Search failed"))
                        }
                    },
                )
            cont.invokeOnCancellation { task.cancel() }
        }

    override suspend fun select(id: String): NetworkResult<SearchResultDto> {
        val suggestion =
            lastSuggestions.find { it.id == id }
                ?: return NetworkResult.Error("Suggestion expired — search again")

        return suspendCancellableCoroutine { cont ->
            val task =
                engine.select(
                    suggestion,
                    object : SearchSelectionCallback {
                        override fun onResult(suggestion: SearchSuggestion, result: SearchResult, responseInfo: ResponseInfo) {
                            cont.resume(NetworkResult.Success(result.toDto()))
                        }

                        override fun onResults(suggestion: SearchSuggestion, results: List<SearchResult>, responseInfo: ResponseInfo) {
                            results
                                .firstOrNull()
                                ?.let { cont.resume(NetworkResult.Success(it.toDto())) }
                                ?: cont.resume(NetworkResult.Error("No results"))
                        }

                        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                        }

                        override fun onError(e: Exception) {
                            cont.resume(NetworkResult.Error(e.message ?: "Select failed"))
                        }
                    },
                )
            cont.invokeOnCancellation { task.cancel() }
        }
    }

    private fun SearchSuggestion.toDto() =
        SearchResultDto(
            id = id,
            name = name,
            fullAddress = fullAddress,
            distanceMeters = distanceMeters,
        )

    private fun SearchResult.toDto() =
        SearchResultDto(
            id = id,
            name = name,
            fullAddress = fullAddress,
            distanceMeters = distanceMeters,
            lat = coordinate.latitude(),
            lng = coordinate.longitude(),
        )
}
