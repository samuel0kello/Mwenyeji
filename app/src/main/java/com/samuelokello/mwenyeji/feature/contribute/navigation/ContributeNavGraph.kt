package com.samuelokello.mwenyeji.feature.contribute.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.samuelokello.mwenyeji.feature.contribute.ContributeScreen
import com.samuelokello.mwenyeji.navigation.navigateBack
import kotlinx.serialization.Serializable

@Serializable
data object ContributeNavGraph

@Serializable
data object ContributeRoute

fun NavGraphBuilder.contributeNavGraph(navController: NavHostController) {
    navigation<ContributeNavGraph>(startDestination = ContributeRoute) {
        composable<ContributeRoute> {
            ContributeScreen(
                onNavigateBack = { navController.navigateBack() },
                onNavigateToSuccess = { navController.navigateBack() },
            )
        }
    }
}

fun NavController.navigateToContribute() {
    navigate(ContributeRoute)
}
