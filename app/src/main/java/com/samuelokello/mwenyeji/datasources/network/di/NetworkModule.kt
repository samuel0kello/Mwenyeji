package com.samuelokello.mwenyeji.datasources.network.di

import com.samuelokello.mwenyeji.datasources.network.helpers.createHttpClient
import com.samuelokello.mwenyeji.datasources.network.sources.search.di.searchModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

val networkModule =
    module {
        includes(searchModule)
        single<CoroutineContext> { Dispatchers.IO }
        single<HttpClient> { createHttpClient(engine = Android) }
    }
