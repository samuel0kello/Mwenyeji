package com.samuelokello.mwenyeji.datasources.sources.confirmation.di

import com.samuelokello.mwenyeji.datasources.sources.confirmation.ConfirmationsRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.confirmation.FirebaseConfirmationsRemoteDataSource
import org.koin.dsl.module

val confirmationsDataSourceModule =
    module {
        single<ConfirmationsRemoteDataSource> {
            FirebaseConfirmationsRemoteDataSource(firestore = get())
        }
    }
