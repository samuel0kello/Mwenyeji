package com.samuelokello.mwenyeji.datasources.network.sources.search

import com.samuelokello.mwenyeji.data.mappers.toDto
import com.samuelokello.mwenyeji.datasources.network.helpers.NetworkResult
import com.samuelokello.mwenyeji.datasources.network.sources.search.dto.NominatimResultDto
import com.samuelokello.mwenyeji.datasources.network.sources.search.dto.SearchRequestDto
import com.samuelokello.mwenyeji.datasources.network.sources.search.dto.SearchResultDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.CancellationException

interface NominatimService {
    suspend fun search(request: SearchRequestDto): NetworkResult<List<SearchResultDto>>
}

class NominatimServiceImpl(
    private val client: HttpClient,
) : NominatimService {
    override suspend fun search(request: SearchRequestDto): NetworkResult<List<SearchResultDto>> =
        try {
            val results: List<NominatimResultDto> =
                client
                    .get(
                        "https://nominatim.openstreetmap.org/search",
                    ) {
                        // required by usage policy
                        header("User-Agent", "Mwenyeji/1.0 (com.samuelokello.mwenyeji)")

                        parameter("q", request.query)
                        parameter("format", "jsonv2")
                        parameter("countrycodes", "ke")
                        parameter("viewbox", "36.6,-1.5,37.1,-1.1") // Nairobi bounding box
                        parameter("bounded", "1")
                        parameter("limit", request.limit)
                        parameter("addressdetails", "1")
                        parameter("accept-language", "en")
                        parameter(
                            "layer",
                            "address,poi,manmade",
                        ) // covers stops, landmarks, buildings
                    }.body()

            val mapped = results.map { it.toDto() }
            NetworkResult.Success(mapped)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Nominatim search failed")
        }
}
