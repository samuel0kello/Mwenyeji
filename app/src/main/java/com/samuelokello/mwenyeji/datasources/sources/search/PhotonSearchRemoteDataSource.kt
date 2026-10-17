package com.samuelokello.mwenyeji.datasources.sources.search

import com.samuelokello.mwenyeji.datasources.core.network.helpers.getJson
import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.core.result.RemoteError
import com.samuelokello.mwenyeji.datasources.core.result.map
import com.samuelokello.mwenyeji.datasources.core.result.onSuccess
import com.samuelokello.mwenyeji.datasources.sources.search.dto.PhotonResponseDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.SearchRequestDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.SearchResultDto
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Place search on OpenStreetMap data through Photon, a search-as-you-type geocoder. One phase: results
 * come with coordinates, so [selectResult] is a lookup in the last results.
 *
 * [SearchConfig.photonUrl] points at the public server (komoot's, shared and rate-limited), fine for
 * testing. For production, run Photon on our own Kenya data and point it there.
 */
internal class PhotonSearchRemoteDataSource(
    private val client: HttpClient,
    private val config: SearchConfig,
) : SearchRemoteDataSource {
    private val cacheLock = Mutex()
    private var lastResults: Map<String, SearchResultDto> = emptyMap()

    override suspend fun search(request: SearchRequestDto): NetworkResult<List<SearchResultDto>> {
        val bbox = request.boundingBox ?: config.defaultBoundingBox
        val near = request.proximity ?: config.defaultProximity

        val result =
            client
                .getJson<PhotonResponseDto>(config.photonUrl) {
                    header("User-Agent", config.userAgent)
                    parameter("q", request.query)
                    parameter("limit", request.limit)
                    // Rank nearby places first, and only within the city's box.
                    parameter("lat", near.latitude)
                    parameter("lon", near.longitude)
                    parameter("bbox", "${bbox.minLng},${bbox.minLat},${bbox.maxLng},${bbox.maxLat}")
                }.map { response -> response.features.mapNotNull { it.toSearchResultDto() } }

        result.onSuccess { mapped ->
            cacheLock.withLock { lastResults = mapped.associateBy { it.id } }
        }
        return result
    }

    override suspend fun selectResult(id: String): NetworkResult<SearchResultDto> {
        val cached = cacheLock.withLock { lastResults[id] }
        return cached
            ?.let { NetworkResult.Success(it) }
            ?: NetworkResult.Error(RemoteError.NotFound)
    }
}
