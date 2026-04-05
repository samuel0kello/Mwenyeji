package com.samuelokello.mwenyeji.feature.feed.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.samuelokello.mwenyeji.feature.feed.FeedScreen
import com.samuelokello.mwenyeji.feature.feed.route.AllRoutes
import com.samuelokello.mwenyeji.feature.feed.route.RouteDetailsScreen
import com.samuelokello.mwenyeji.navigation.navigateBack
import kotlinx.serialization.Serializable

@Serializable
data object FeedsGraph

@Serializable
data object FeedsRoute

@Serializable
data class RouteDetailsRoute(
    val routeId: String,
)

@Serializable
data object SeeAllRoutesRoute

fun NavGraphBuilder.feedsNavGraph(navController: NavHostController) {
    navigation<FeedsGraph>(
        startDestination = FeedsRoute,
    ) {
        composable<FeedsRoute> {
            FeedScreen(
                onNavigateToRouteDetail = { routeId -> navController.navigateToRouteDetails(routeId) },
                onNavigateToSeeAll = { navController.navigateToAllRoutes()},
            )
        }
        composable<RouteDetailsRoute> { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
            RouteDetailsScreen(
                routeId = routeId,
                onNavigateBack = { navController.navigateBack() },
            )
        }

        composable<SeeAllRoutesRoute> {
            AllRoutes(
                onNavigateToRouteDetail = { navController.navigateToRouteDetails(it)},
                onNavigateBack = { navController.navigateBack()}
            )
        }
    }
}

fun NavController.navigateToFeeds() {
    navigate(FeedsGraph)
}

fun NavController.navigateToRouteDetails(routeId: String) {
    navigate(RouteDetailsRoute(routeId))
}

fun NavController.navigateToAllRoutes() {
    navigate(SeeAllRoutesRoute)
}