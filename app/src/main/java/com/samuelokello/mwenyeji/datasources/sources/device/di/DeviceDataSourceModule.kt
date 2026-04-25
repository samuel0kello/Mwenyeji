package com.samuelokello.mwenyeji.datasources.sources.device.di

import com.samuelokello.mwenyeji.datasources.sources.device.DeviceIdentityRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.device.FirebaseDeviceIdentityRemoteDataSource
import org.koin.dsl.module

val deviceDataSourceModule =
    module {
        single<DeviceIdentityRemoteDataSource> {
            FirebaseDeviceIdentityRemoteDataSource(
                firestore = get(),
                auth = get(),
            )
        }
    }
