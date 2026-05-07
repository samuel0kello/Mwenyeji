package com.samuelokello.mwenyeji.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import com.samuelokello.mwenyeji.feature.contribute.navigation.contributeNavGraph
import com.samuelokello.mwenyeji.feature.feed.navigation.FeedsGraph
import com.samuelokello.mwenyeji.feature.feed.navigation.feedsNavGraph
import com.samuelokello.mwenyeji.feature.onboarding.navigation.OnBoarding
import com.samuelokello.mwenyeji.feature.onboarding.navigation.onBoarding
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.AppEasing
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.Duration
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.appTween
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Composable
fun MwenyejiNavGraph(navController: NavHostController, onRequireAuth: (onAuthenticated: () -> Unit) -> Unit = {}) {
    val prefs: MwenyejiPrefs = koinInject()
    var startDestination by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(Unit) {
        val isComplete = prefs.isOnBoardingComplete().first()
        startDestination = if (isComplete) Main else OnBoarding
    }

    startDestination?.let { destination ->
        NavHost(
            navController = navController,
            startDestination = destination,
            enterTransition = {
                fadeIn(appTween(Duration.NORMAL, AppEasing.decelerated))
            },
            exitTransition = {
                slideOutOfContainer(SlideDirection.Up, appTween(easing = AppEasing.accelerated)) +
                    fadeOut(appTween(easing = AppEasing.accelerated))
            },
            popEnterTransition = {
                slideIntoContainer(
                    SlideDirection.Right,
                    tween(Duration.MEDIUM, easing = AppEasing.decelerated),
                ) +
                    fadeIn(tween(Duration.MEDIUM))
            },
            popExitTransition = {
                slideOutOfContainer(
                    SlideDirection.Right,
                    tween(Duration.MEDIUM, easing = AppEasing.accelerated),
                ) +
                    fadeOut(tween(Duration.MEDIUM))
            },
        ) {
            mainGraph(navController, onRequireAuth)
            onBoarding(navController)
        }
    }
}

fun NavGraphBuilder.mainGraph(navController: NavHostController, onRequireAuth: (onAuthenticated: () -> Unit) -> Unit = {}) {
    navigation<Main>(startDestination = FeedsGraph) {
        feedsNavGraph(navController, onRequireAuth)
        contributeNavGraph(navController)
    }
}

@Serializable
data object Main

fun NavHostController.navigateToMain() {
    navigate(Main) {
        popUpTo(graph.startDestinationId) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
