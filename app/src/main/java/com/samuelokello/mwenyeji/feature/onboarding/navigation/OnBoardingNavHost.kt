package com.samuelokello.mwenyeji.feature.onboarding.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.samuelokello.mwenyeji.feature.onboarding.OnboardingScreen
import com.samuelokello.mwenyeji.navigation.navigateToMain
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.AppEasing
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.Duration
import kotlinx.serialization.Serializable

@Serializable
data object OnBoarding

fun NavGraphBuilder.onBoarding(navController: NavHostController) {
    composable<OnBoarding>(
        enterTransition = {
            fadeIn(tween(Duration.NORMAL, easing = AppEasing.decelerated))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = SlideDirection.Up,
                animationSpec = tween(Duration.MEDIUM, easing = AppEasing.accelerated),
            ) +
                fadeOut(tween(Duration.MEDIUM, easing = AppEasing.accelerated))
        },
    ) {
        OnboardingScreen(
            onFinish = {
                navController.navigateToMain()
            },
        )
    }
}

fun NavController.navigateToOnboarding() {
    navigate(OnBoarding)
}
