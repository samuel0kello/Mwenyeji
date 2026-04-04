package com.samuelokello.mwenyeji.di

import com.samuelokello.mwenyeji.datasources.preference.di.preferencesModule
import com.samuelokello.mwenyeji.feature.feed.di.feedModule
import com.samuelokello.mwenyeji.feature.onboarding.OnboardingViewModel
import com.samuelokello.mwenyeji.feature.onboarding.di.onBoardingModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mwenyejiModules = listOf(
    preferencesModule,
    onBoardingModule,
    feedModule
)