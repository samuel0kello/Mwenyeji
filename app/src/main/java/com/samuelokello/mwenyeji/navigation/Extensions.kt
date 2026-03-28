package com.samuelokello.mwenyeji.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun NavController.shouldShowBottomBar(): Boolean {
    val navBackStackEntry = currentBackStackEntryAsState().value

    return navBackStackEntry?.destination?.let { destination ->
        destination.hasRoute<BottomScreenRoutes.Home>() ||
                destination.hasRoute<BottomScreenRoutes.Contribute>() ||
                destination.hasRoute<BottomScreenRoutes.profile>()
    } ?: false
}
fun NavHostController.navigateBack() {
    if (this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        this.popBackStack()
    }
}