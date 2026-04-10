package com.samuelokello.mwenyeji.di

import com.samuelokello.mwenyeji.data.dataModule
import com.samuelokello.mwenyeji.datasources.di.dataSourceModule
import com.samuelokello.mwenyeji.feature.feed.di.feedModule
import com.samuelokello.mwenyeji.feature.onboarding.di.onBoardingModule

val mwenyejiModules = listOf(
    dataSourceModule,
    dataModule,
    onBoardingModule,
    feedModule
)