package com.samuelokello.mwenyeji.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import com.samuelokello.mwenyeji.feature.feed.navigation.FeedsGraph

fun NavHostController.shouldShowBottomBar(): Boolean {
    val currentDestination = currentBackStackEntry?.destination ?: return false
    return currentDestination.hierarchy.any { dest ->
        dest.hasRoute<FeedsGraph>() ||
                dest.hasRoute<BottomScreenRoutes.Contribute>()
    }
}
fun NavHostController.navigateBack() {
    if (this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        this.popBackStack()
    }
}