package com.samuelokello.mwenyeji.navigation

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
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Composable
fun MwenyejiNavGraph(navController: NavHostController) {
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
        ) {
            mainGraph(navController)
            onBoarding(navController)
        }
    }
}

fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation<Main>(startDestination = FeedsGraph) {
        feedsNavGraph(navController)
        // ← contributeNavGraph removed — sheet is shown from App.kt
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
