package com.samuelokello.mwenyeji.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import com.samuelokello.mwenyeji.feature.feed.navigation.FeedsRoute
import com.samuelokello.mwenyeji.feature.feed.navigation.RouteDetailsRoute
import com.samuelokello.mwenyeji.feature.feed.navigation.SeeAllRoutesRoute

fun NavHostController.shouldShowBottomBar(): Boolean {
    val destination = currentBackStackEntry?.destination ?: return false
    return destination.hierarchy.any { dest ->
        dest.hasRoute<FeedsRoute>() ||
                dest.hasRoute<BottomScreenRoutes.Contribute>()
    } && destination.hierarchy.none { dest ->
        dest.hasRoute<RouteDetailsRoute>() ||
                dest.hasRoute<SeeAllRoutesRoute>()
    }
}
fun NavHostController.navigateBack() {
    if (this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        this.popBackStack()
    }
}