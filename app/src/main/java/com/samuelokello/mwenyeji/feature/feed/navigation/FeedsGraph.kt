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
    val from: String? = null,
    val to: String? = null,
)

@Serializable
data object SeeAllRoutesRoute

fun NavGraphBuilder.feedsNavGraph(
    navController: NavHostController,
    onRequireAuth: (onAuthenticated: () -> Unit) -> Unit = {},
) {
    navigation<FeedsGraph>(startDestination = FeedsRoute) {
        tabScreen<FeedsRoute> {
            FeedScreen(
                onNavigateToRouteDetail = { routeId, from, to ->
                    navController.navigateToRouteDetails(routeId, from, to)
                },
                onNavigateToSeeAll = { navController.navigateToAllRoutes() },
                onNavigateToContribute = {
                    onRequireAuth { navController.navigateToContribute() }
                },
            )
        }

        slideScreen<RouteDetailsRoute> { backStackEntry ->
            val args = backStackEntry.arguments
            val routeId = args?.getString("routeId") ?: ""
            val from = args?.getString("from")
            val to = args?.getString("to")
            RouteDetailsScreen(
                routeId = routeId,
                from = from,
                to = to,
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

fun NavController.navigateToRouteDetails(
    routeId: String,
    from: String? = null,
    to: String? = null,
) {
    navigate(RouteDetailsRoute(routeId, from, to))
}

fun NavController.navigateToAllRoutes() {
    navigate(SeeAllRoutesRoute)
}
