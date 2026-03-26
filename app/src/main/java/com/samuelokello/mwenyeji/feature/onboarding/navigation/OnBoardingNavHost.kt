package com.samuelokello.mwenyeji.feature.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.samuelokello.mwenyeji.feature.onboarding.OnBoardingScreen
import kotlinx.serialization.Serializable


@Serializable
data object OnBoarding

fun NavGraphBuilder.onBoarding(
    navController: NavHostController,
    onFinishOnBoarding: () -> Unit
) {
    composable <OnBoarding>{
        OnBoardingScreen(
            onFinish = onFinishOnBoarding
        )
    }
}