package com.samuelokello.mwenyeji.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.AppEasing
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.Duration
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.appTween

/**
 * Centralized navigation transitions. All graphs reference these by name —
 * change a duration here, and every screen using that preset updates together.
 */
object NavAnimations {
    //  Horizontal slide (default for forward navigation)
    val slideForwardEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
        {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = appTween(Duration.LONG, AppEasing.emphasized),
            ) +
                fadeIn(animationSpec = appTween(Duration.NORMAL))
        }

    val slideForwardExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = appTween(Duration.LONG, AppEasing.emphasized),
        ) +
            fadeOut(animationSpec = appTween(Duration.NORMAL))
    }

    val slideForwardPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
        {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = appTween(Duration.LONG, AppEasing.emphasized),
            ) +
                fadeIn(animationSpec = appTween(Duration.NORMAL))
        }

    val slideForwardPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
        {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = appTween(Duration.LONG, AppEasing.emphasized),
            ) +
                fadeOut(animationSpec = appTween(Duration.NORMAL))
        }

    //  Vertical slide (modals, bottom-sheet-like screens)
    val slideUpEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = appTween(Duration.LONG, AppEasing.emphasized),
        ) +
            fadeIn(animationSpec = appTween(Duration.NORMAL))
    }

    val slideDownExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = appTween(Duration.LONG, AppEasing.emphasized),
        ) +
            fadeOut(animationSpec = appTween(Duration.NORMAL))
    }

    //  Fade (auth flows, root-level transitions, tab switches)
    val fadeEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = appTween(Duration.NORMAL))
    }

    val fadeExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = appTween(Duration.NORMAL))
    }

    //  None (instant — for tabbed bottom-nav siblings)
    val none: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        EnterTransition.None
    }
    val noneExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        ExitTransition.None
    }

    val tabEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
        {
            val initialIndex = getTabOrderIndex(initialState.destination.route)
            val targetIndex = getTabOrderIndex(targetState.destination.route)

            val direction =
                if (targetIndex > initialIndex) {
                    AnimatedContentTransitionScope.SlideDirection.Start
                } else {
                    AnimatedContentTransitionScope.SlideDirection.End
                }

            slideIntoContainer(
                towards = direction,
                animationSpec = appTween(Duration.LONG, AppEasing.emphasized),
            ) +
                fadeIn(animationSpec = appTween(Duration.NORMAL))
        }

    // Dynamic Tab Transitions based on relative index positioning
    val tabExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
        {
            val initialIndex = getTabOrderIndex(initialState.destination.route)
            val targetIndex = getTabOrderIndex(targetState.destination.route)

            val direction =
                if (targetIndex > initialIndex) {
                    AnimatedContentTransitionScope.SlideDirection.Start
                } else {
                    AnimatedContentTransitionScope.SlideDirection.End
                }

            slideOutOfContainer(
                towards = direction,
                animationSpec = appTween(Duration.LONG, AppEasing.emphasized),
            ) +
                fadeOut(animationSpec = appTween(Duration.NORMAL))
        }
}
