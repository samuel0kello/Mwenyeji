package com.samuelokello.mwenyeji.di

import com.samuelokello.mwenyeji.datasources.preference.di.preferencesModule
import com.samuelokello.mwenyeji.feature.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    includes(preferencesModule)
    viewModelOf(::OnboardingViewModel)
}
