package com.samuelokello.mwenyeji.feature.onboarding.di

import com.samuelokello.mwenyeji.feature.onboarding.OnboardingReducer
import com.samuelokello.mwenyeji.feature.onboarding.OnboardingViewModel
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.SnackBarManager
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onBoardingModule =
    module {
        single { SnackBarManager() }
        viewModelOf(::OnboardingViewModel)
        single { OnboardingReducer() }
    }
