package com.samuelokello.mwenyeji.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.samuelokello.mwenyeji.feature.contribute.navigation.ContributeNavGraph
import com.samuelokello.mwenyeji.feature.feed.navigation.FeedsRoute
import com.samuelokello.mwenyeji.feature.feed.navigation.RouteDetailsRoute
import com.samuelokello.mwenyeji.feature.feed.navigation.SeeAllRoutesRoute
import kotlin.reflect.KType

fun NavHostController.shouldShowBottomBar(): Boolean {
    val destination = currentBackStackEntry?.destination ?: return false
    return destination.hierarchy.any { dest ->
        dest.hasRoute<FeedsRoute>() ||
            dest.hasRoute<ContributeNavGraph>()
    } &&
        destination.hierarchy.none { dest ->
            dest.hasRoute<RouteDetailsRoute>() ||
                dest.hasRoute<SeeAllRoutesRoute>()
//                ||
//                dest.hasRoute<ContributeRoute>()
        }
}

fun NavHostController.navigateBack() {
    if (this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        this.popBackStack()
    }
}

fun getTabOrderIndex(route: Any?): Int {
    if (route == null) return 0
    return when {
        route.toString().contains("FeedRoute") -> 0
        route.toString().contains("SavedRoute") -> 1
        route.toString().contains("ProfileRoute") -> 2
        else -> 0
    }
}

/**
 * Type-safe composable destination with a NavMotion preset.
 */
inline fun <reified T : Any> NavGraphBuilder.screen(
    motion: NavMotion = NavMotions.slide,
    typeMap: Map<KType, NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composable<T>(
    typeMap = typeMap,
    deepLinks = deepLinks,
    enterTransition = motion.enter,
    exitTransition = motion.exit,
    popEnterTransition = motion.popEnter,
    popExitTransition = motion.popExit,
    content = content,
)

/**
 * Type-safe nested nav graph with a NavMotion preset applied to every screen inside.
 */
inline fun <reified T : Any> NavGraphBuilder.graph(
    startDestination: Any,
    motion: NavMotion = NavMotions.slide,
    noinline builder: NavGraphBuilder.() -> Unit,
) = navigation<T>(
    startDestination = startDestination,
    enterTransition = motion.enter,
    exitTransition = motion.exit,
    popEnterTransition = motion.popEnter,
    popExitTransition = motion.popExit,
    builder = builder,
)

// ---- Named aliases for the common cases ----

inline fun <reified T : Any> NavGraphBuilder.slideScreen(noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) =
    screen<T>(motion = NavMotions.slide, content = content)

inline fun <reified T : Any> NavGraphBuilder.modalScreen(noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) =
    screen<T>(motion = NavMotions.modal, content = content)

inline fun <reified T : Any> NavGraphBuilder.fadeScreen(noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) =
    screen<T>(motion = NavMotions.fade, content = content)

inline fun <reified T : Any> NavGraphBuilder.tabScreen(noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) =
    screen<T>(motion = NavMotions.dynamicTab, content = content)

inline fun <reified T : Any> NavGraphBuilder.sheetScreen(noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) =
    screen<T>(motion = NavMotions.sheetUp, content = content)
