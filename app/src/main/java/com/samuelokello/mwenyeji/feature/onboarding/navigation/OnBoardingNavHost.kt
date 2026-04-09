package com.samuelokello.mwenyeji.feature.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.samuelokello.mwenyeji.feature.onboarding.OnBoardingScreen
import com.samuelokello.mwenyeji.navigation.navigateToMain
import kotlinx.serialization.Serializable


@Serializable
data object OnBoarding

fun NavGraphBuilder.onBoarding(
    navController: NavHostController,
) {
    composable <OnBoarding>{
        OnBoardingScreen(
            onFinish = { navController.navigateToMain() }
        )
    }
}

fun NavController.navigateToOnboarding() {
    navigate(OnBoarding)
}