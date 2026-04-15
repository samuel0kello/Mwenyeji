package com.samuelokello.mwenyeji.datasources.network.sources.search.di

import com.mapbox.search.ApiType
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.samuelokello.mwenyeji.datasources.network.sources.search.NominatimService
import com.samuelokello.mwenyeji.datasources.network.sources.search.NominatimServiceImpl
import com.samuelokello.mwenyeji.datasources.network.sources.search.SearchService
import com.samuelokello.mwenyeji.datasources.network.sources.search.SearchServiceImpl
import org.koin.dsl.module

val searchModule =
    module {
        single<SearchEngine> {
            SearchEngine.createSearchEngine(
                apiType = ApiType.SEARCH_BOX,
                settings = SearchEngineSettings(),
            )
        }
        single<SearchService> { SearchServiceImpl(get()) }
        single<NominatimService> { NominatimServiceImpl(get()) } // reuses existing HttpClient
    }
