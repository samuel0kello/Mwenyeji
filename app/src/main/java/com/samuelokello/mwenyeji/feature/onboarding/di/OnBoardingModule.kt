package com.samuelokello.mwenyeji.feature.onboarding.di

import com.samuelokello.mwenyeji.feature.onboarding.OnboardingReducer
import com.samuelokello.mwenyeji.feature.onboarding.OnboardingViewModel
import com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.SnackBarManager
import com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.SnackBarManagerImpl
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onBoardingModule =
    module {
        single<SnackBarManager> { SnackBarManagerImpl() }
        viewModelOf(::OnboardingViewModel)
        single { OnboardingReducer() }
    }
