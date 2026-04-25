package com.samuelokello.mwenyeji.data.di

import com.samuelokello.mwenyeji.data.helpers.DeviceIdProvider
import com.samuelokello.mwenyeji.data.repository.AuthRepository
import com.samuelokello.mwenyeji.data.repository.AuthRepositoryImpl
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import com.samuelokello.mwenyeji.data.repository.RoutesRepositoryImpl
import com.samuelokello.mwenyeji.data.repository.SearchRepository
import com.samuelokello.mwenyeji.data.repository.SearchRepositoryImpl
import org.koin.dsl.module

val dataModule =
    module {
        single<AuthRepository> { AuthRepositoryImpl(get()) }
        single<RoutesRepository> { RoutesRepositoryImpl(get(), get()) }
        single { DeviceIdProvider(get()) }
        single<SearchRepository> { SearchRepositoryImpl(get()) }
    }
