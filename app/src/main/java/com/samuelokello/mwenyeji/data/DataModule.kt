package com.samuelokello.mwenyeji.data

import com.samuelokello.mwenyeji.data.helpers.DeviceIdProvider
import com.samuelokello.mwenyeji.data.repository.AuthRepository
import com.samuelokello.mwenyeji.data.repository.AuthRepositoryImpl
import com.samuelokello.mwenyeji.data.repository.RouteRepository
import com.samuelokello.mwenyeji.data.repository.RouteRepositoryImpl
import org.koin.dsl.module

val dataModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<RouteRepository> { RouteRepositoryImpl(get()) }
    single { DeviceIdProvider(get()) }
}