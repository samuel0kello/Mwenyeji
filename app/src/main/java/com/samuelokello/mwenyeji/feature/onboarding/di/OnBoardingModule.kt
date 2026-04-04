package com.samuelokello.mwenyeji.feature.onboarding.di

import com.samuelokello.mwenyeji.feature.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onBoardingModule = module {
    viewModelOf(::OnboardingViewModel)
}