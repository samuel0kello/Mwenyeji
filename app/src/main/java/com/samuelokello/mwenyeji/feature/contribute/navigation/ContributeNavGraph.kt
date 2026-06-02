package com.samuelokello.mwenyeji.feature.contribute.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import com.samuelokello.mwenyeji.feature.contribute.ContributeScreen
import com.samuelokello.mwenyeji.navigation.navigateBack
import com.samuelokello.mwenyeji.navigation.screen
import kotlinx.serialization.Serializable

@Serializable
data object ContributeNavGraph

@Serializable
data object ContributeRoute

fun NavGraphBuilder.contributeNavGraph(navController: NavHostController) {
    navigation<ContributeNavGraph>(startDestination = ContributeRoute) {
        screen<ContributeRoute> {
            ContributeScreen(
                onNavigateBack = { navController.navigateBack() },
                onNavigateToSuccess = { navController.navigateBack() },
            )
        }
    }
}

fun NavController.navigateToContribute(onRequireAuth: ((onAuthenticated: () -> Unit) -> Unit)? = null) {
    if (onRequireAuth != null) {
        onRequireAuth {
            navigate(ContributeRoute)
        }
    } else {
        navigate(ContributeRoute)
    }
}
