package com.samuelokello.mwenyeji.datasources.sources.search.di

import com.mapbox.search.ApiType
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.samuelokello.mwenyeji.datasources.sources.search.MapboxSearchRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.search.NominatimSearchRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.search.SearchConfig
import com.samuelokello.mwenyeji.datasources.sources.search.SearchRemoteDataSource
import org.koin.core.qualifier.named
import org.koin.dsl.module

object SearchQualifiers {
    val Mapbox = named("mapbox")
    val Nominatim = named("nominatim")
}

val searchDataSourceModule =
    module {
        single<SearchConfig> { SearchConfig.NAIROBI }

        single<SearchEngine> {
            SearchEngine.createSearchEngine(
                apiType = ApiType.SEARCH_BOX,
                settings = SearchEngineSettings(),
            )
        }

        single<SearchRemoteDataSource>(SearchQualifiers.Mapbox) {
            MapboxSearchRemoteDataSource(engine = get(), config = get())
        }

        single<SearchRemoteDataSource>(SearchQualifiers.Nominatim) {
            NominatimSearchRemoteDataSource(client = get(), config = get())
        }
    }
