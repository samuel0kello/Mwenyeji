package com.samuelokello.mwenyeji.feature.feed.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import com.samuelokello.mwenyeji.feature.contribute.navigation.navigateToContribute
import com.samuelokello.mwenyeji.feature.feed.FeedScreen
import com.samuelokello.mwenyeji.feature.feed.route.AllRoutes
import com.samuelokello.mwenyeji.feature.feed.route.RouteDetailsScreen
import com.samuelokello.mwenyeji.navigation.navigateBack
import com.samuelokello.mwenyeji.navigation.slideScreen
import com.samuelokello.mwenyeji.navigation.tabScreen
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

fun NavGraphBuilder.feedsNavGraph(navController: NavHostController, onRequireAuth: (onAuthenticated: () -> Unit) -> Unit = {}) {
    navigation<FeedsGraph>(startDestination = FeedsRoute) {
        tabScreen<FeedsRoute> {
            FeedScreen(
                onNavigateToRouteDetail = { routeId -> navController.navigateToRouteDetails(routeId) },
                onNavigateToSeeAll = { navController.navigateToAllRoutes() },
                onNavigateToContribute = {
                    onRequireAuth { navController.navigateToContribute() }
                },
            )
        }

        slideScreen<RouteDetailsRoute> { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
            RouteDetailsScreen(
                routeId = routeId,
                onNavigateBack = { navController.navigateBack() },
                onNavigateToContribute = {
                    onRequireAuth { navController.navigateToContribute() }
                },
            )
        }

        slideScreen<SeeAllRoutesRoute> {
            AllRoutes(
                onNavigateToRouteDetail = { navController.navigateToRouteDetails(it) },
                onNavigateBack = { navController.navigateBack() },
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
