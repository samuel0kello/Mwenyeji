package com.samuelokello.mwenyeji.di

import com.samuelokello.mwenyeji.core.di.coreModule
import com.samuelokello.mwenyeji.data.di.dataModule
import com.samuelokello.mwenyeji.datasources.di.dataSourceModules
import com.samuelokello.mwenyeji.feature.auth.di.featureAuthModule
import com.samuelokello.mwenyeji.feature.feed.di.feedModule
import com.samuelokello.mwenyeji.feature.onboarding.di.onBoardingModule
import org.koin.dsl.module

val mwenyejiModules =
    module {

        includes(coreModule)
        includes(dataSourceModules)
        includes(dataModule)
        includes(onBoardingModule)
        includes(featureAuthModule)
        includes(feedModule)
    }
