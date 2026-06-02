package com.samuelokello.mwenyeji.datasources.sources.routes.di

import com.samuelokello.mwenyeji.datasources.sources.routes.FirebaseRoutesRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.routes.RoutesRemoteDataSource
import org.koin.dsl.module

val routesDataSourceModule =
    module {
        single<RoutesRemoteDataSource> { FirebaseRoutesRemoteDataSource(firestore = get()) }
    }
