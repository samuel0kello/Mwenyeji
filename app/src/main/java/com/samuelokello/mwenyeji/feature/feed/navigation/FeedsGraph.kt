package com.samuelokello.mwenyeji.feature.feed.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import com.samuelokello.mwenyeji.feature.contribute.navigation.navigateToContribute
import com.samuelokello.mwenyeji.feature.feed.FeedScreen
import com.samuelokello.mwenyeji.feature.feed.route.AllRoutes
import com.samuelokello.mwenyeji.feature.feed.route.RouteDetailsScreen
import com.samuelokello.mwenyeji.feature.guidance.ui.GuidanceScreen
import com.samuelokello.mwenyeji.feature.planner.ui.AskScreen
import com.samuelokello.mwenyeji.feature.planner.ui.PlanTripScreen
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

@Serializable
data object GuidanceRoute

/** @param listen Open the speech recogniser straight away. */
@Serializable
data class PlanTripRoute(
    val listen: Boolean = false,
)

@Serializable
data object AskRoute

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
                onPlanTrip = { listen -> navController.navigate(PlanTripRoute(listen)) },
                onOpenChat = { navController.navigate(AskRoute) },
            )
        }

        slideScreen<PlanTripRoute> { backStackEntry ->
            PlanTripScreen(
                listenOnOpen = backStackEntry.arguments?.getBoolean("listen") ?: false,
                onNavigateBack = { navController.navigateBack() },
                onOpenChat = { navController.navigate(AskRoute) },
                onNavigateToGuidance = { navController.navigate(GuidanceRoute) { launchSingleTop = true } },
            )
        }

        slideScreen<AskRoute> {
            AskScreen(
                onNavigateBack = { navController.navigateBack() },
                onNavigateToGuidance = { navController.navigate(GuidanceRoute) { launchSingleTop = true } },
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
                onNavigateToGuidance = { navController.navigate(GuidanceRoute) { launchSingleTop = true } },
            )
        }

        slideScreen<GuidanceRoute> {
            GuidanceScreen(onNavigateBack = { navController.navigateBack() })
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
