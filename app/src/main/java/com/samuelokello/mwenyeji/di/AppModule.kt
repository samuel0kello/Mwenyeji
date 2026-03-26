package com.samuelokello.mwenyeji.di

import com.samuelokello.mwenyeji.feature.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::OnboardingViewModel)
}
