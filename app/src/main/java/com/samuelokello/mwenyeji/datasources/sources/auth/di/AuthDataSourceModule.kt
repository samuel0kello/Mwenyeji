package com.samuelokello.mwenyeji.datasources.sources.auth.di

import com.samuelokello.mwenyeji.datasources.sources.auth.AuthRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.auth.FirebaseAuthRemoteDataSource
import org.koin.dsl.module

val authDataSourceModule =
    module {
        single<AuthRemoteDataSource> { FirebaseAuthRemoteDataSource(get()) }
    }
