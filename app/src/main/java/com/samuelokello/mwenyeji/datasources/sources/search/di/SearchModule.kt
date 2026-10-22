package com.samuelokello.mwenyeji.datasources.sources.search.di

import com.samuelokello.mwenyeji.datasources.sources.search.NominatimSearchRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.search.PhotonSearchRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.search.SearchConfig
import com.samuelokello.mwenyeji.datasources.sources.search.SearchRemoteDataSource
import org.koin.core.qualifier.named
import org.koin.dsl.module

object SearchQualifiers {
    val Nominatim = named("nominatim")
}

val searchDataSourceModule =
    module {
        single<SearchConfig> { SearchConfig.NAIROBI }

        // The default: search-as-you-type over OpenStreetMap.
        single<SearchRemoteDataSource> { PhotonSearchRemoteDataSource(client = get(), config = get()) }

        single<SearchRemoteDataSource>(SearchQualifiers.Nominatim) {
            NominatimSearchRemoteDataSource(client = get(), config = get())
        }
    }
