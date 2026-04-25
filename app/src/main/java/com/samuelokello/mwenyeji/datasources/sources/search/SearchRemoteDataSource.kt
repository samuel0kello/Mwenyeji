package com.samuelokello.mwenyeji.datasources.sources.search

import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.core.result.RemoteError
import com.samuelokello.mwenyeji.datasources.sources.search.dto.CoordinatesDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.SearchRequestDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.SearchResultDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

interface SearchRemoteDataSource {
    /**
     * Search for places matching [request].
     */
    suspend fun search(request: SearchRequestDto): NetworkResult<List<SearchResultDto>>

    /**
     * Resolve a result by id to one guaranteed to have coordinates populated.
     * For two-phase backends (Mapbox) this hits the select API.
     * For one-phase backends (Nominatim) this is a cache lookup from the last search.
     */
    suspend fun selectResult(id: String): NetworkResult<SearchResultDto>
}

internal class MapboxSearchRemoteDataSource(
    private val engine: SearchEngine,
    private val config: SearchConfig,
) : SearchRemoteDataSource {
    private val suggestionsLock = Mutex()
    private var suggestionsById: Map<String, SearchSuggestion> = emptyMap()

    override suspend fun search(request: SearchRequestDto): NetworkResult<List<SearchResultDto>> =
        try {
            val suggestions = awaitSuggestions(request)
            suggestionsLock.withLock {
                suggestionsById = suggestions.associateBy { it.id }
            }
            NetworkResult.Success(suggestions.map { it.toDto() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            NetworkResult.Error(SearchErrorMapper.map(e))
        }

    override suspend fun selectResult(id: String): NetworkResult<SearchResultDto> {
        val suggestion =
            suggestionsLock.withLock { suggestionsById[id] }
                ?: return NetworkResult.Error(
                    RemoteError.Unknown("Suggestion expired — search again"),
                )

        return try {
            NetworkResult.Success(awaitSelect(suggestion))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            NetworkResult.Error(SearchErrorMapper.map(e))
        }
    }

    private suspend fun awaitSuggestions(request: SearchRequestDto): List<SearchSuggestion> =
        suspendCancellableCoroutine { cont ->
            val proximity = request.proximity ?: config.defaultProximity
            val options =
                SearchOptions(
                    limit = request.limit,
                    proximity = Point.fromLngLat(proximity.longitude, proximity.latitude),
                )
            val task =
                engine.search(
                    request.query,
                    options,
                    object : SearchSuggestionsCallback {
                        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                            val filtered =
                                suggestions.filter { it.type !is SearchSuggestionType.Category }
                            if (cont.isActive) cont.resume(filtered)
                        }

                        override fun onError(e: Exception) {
                            if (cont.isActive) cont.resumeWith(Result.failure(e))
                        }
                    },
                )
            cont.invokeOnCancellation { task.cancel() }
        }

    private suspend fun awaitSelect(suggestion: SearchSuggestion): SearchResultDto =
        suspendCancellableCoroutine { cont ->
            val task =
                engine.select(
                    suggestion,
                    object : SearchSelectionCallback {
                        override fun onResult(suggestion: SearchSuggestion, result: SearchResult, responseInfo: ResponseInfo) {
                            if (cont.isActive) cont.resume(result.toDto())
                        }

                        override fun onResults(suggestion: SearchSuggestion, results: List<SearchResult>, responseInfo: ResponseInfo) {
                            val first = results.firstOrNull()
                            if (cont.isActive) {
                                if (first != null) {
                                    cont.resume(first.toDto())
                                } else {
                                    cont.resumeWith(Result.failure(NoSuchElementException("No results")))
                                }
                            }
                        }

                        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                        }

                        override fun onError(e: Exception) {
                            if (cont.isActive) cont.resumeWith(Result.failure(e))
                        }
                    },
                )
            cont.invokeOnCancellation { task.cancel() }
        }

    private fun SearchSuggestion.toDto() =
        SearchResultDto(
            id = id,
            name = name,
            fullAddress = fullAddress,
            distanceMeters = distanceMeters,
            coordinates = null, // hydrated by selectResult
        )

    private fun SearchResult.toDto() =
        SearchResultDto(
            id = id,
            name = name,
            fullAddress = fullAddress,
            distanceMeters = distanceMeters,
            coordinates =
                CoordinatesDto(
                    latitude = coordinate.latitude(),
                    longitude = coordinate.longitude(),
                ),
        )
}
