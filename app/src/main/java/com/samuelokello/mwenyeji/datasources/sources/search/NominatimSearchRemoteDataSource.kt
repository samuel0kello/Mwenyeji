package com.samuelokello.mwenyeji.datasources.sources.search

import com.samuelokello.mwenyeji.datasources.core.network.helpers.getJson
import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.core.result.RemoteError
import com.samuelokello.mwenyeji.datasources.core.result.map
import com.samuelokello.mwenyeji.datasources.core.result.onSuccess
import com.samuelokello.mwenyeji.datasources.sources.search.dto.NominatimResultDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.SearchRequestDto
import com.samuelokello.mwenyeji.datasources.sources.search.dto.SearchResultDto
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class NominatimSearchRemoteDataSource(
    private val client: HttpClient,
    private val config: SearchConfig,
) : SearchRemoteDataSource {
    private val cacheLock = Mutex()
    private var lastResults: Map<String, SearchResultDto> = emptyMap()

    override suspend fun search(request: SearchRequestDto): NetworkResult<List<SearchResultDto>> {
        val bbox = request.boundingBox ?: config.defaultBoundingBox
        val country = request.countryCode ?: config.countryCode

        val result =
            client
                .getJson<List<NominatimResultDto>>(ENDPOINT) {
                    header("User-Agent", config.userAgent)
                    parameter("q", request.query)
                    parameter("format", "jsonv2")
                    parameter("countrycodes", country)
                    parameter(
                        "viewbox",
                        "${bbox.minLng},${bbox.minLat},${bbox.maxLng},${bbox.maxLat}",
                    )
                    parameter("bounded", "1")
                    parameter("limit", request.limit)
                    parameter("addressdetails", "1")
                    parameter("accept-language", request.language)
                    parameter("layer", "address,poi,manmade")
                }.map { dtos -> dtos.map { it.toSearchResultDto() } }

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

    private companion object {
        const val ENDPOINT = "https://nominatim.openstreetmap.org/search"
    }
}
