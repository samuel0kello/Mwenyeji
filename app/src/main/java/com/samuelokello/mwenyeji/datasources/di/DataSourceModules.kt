package com.samuelokello.mwenyeji.datasources.di

import com.samuelokello.mwenyeji.datasources.core.di.coreDataSourceModule
import com.samuelokello.mwenyeji.datasources.preference.di.preferenceModule
import com.samuelokello.mwenyeji.datasources.sources.auth.di.authDataSourceModule
import com.samuelokello.mwenyeji.datasources.sources.confirmation.di.confirmationsDataSourceModule
import com.samuelokello.mwenyeji.datasources.sources.device.di.deviceDataSourceModule
import com.samuelokello.mwenyeji.datasources.sources.routes.di.routesDataSourceModule
import com.samuelokello.mwenyeji.datasources.sources.search.di.searchDataSourceModule

val dataSourceModules =
    listOf(
        coreDataSourceModule,
        preferenceModule,
        authDataSourceModule,
        deviceDataSourceModule,
        routesDataSourceModule,
        confirmationsDataSourceModule,
        searchDataSourceModule,
    )
