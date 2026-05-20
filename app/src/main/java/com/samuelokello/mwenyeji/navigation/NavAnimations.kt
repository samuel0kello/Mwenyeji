package com.samuelokello.mwenyeji.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

/**
 * Centralized navigation transitions. All graphs reference these by name —
 * change a duration here, and every screen using that preset updates together.
 */
object NavAnimations {
    private const val DURATION_STANDARD = 500
    private const val DURATION_FAST = 300
    private const val DURATION_SLOW = 600

    //  Horizontal slide (default for forward navigation)
    val slideForwardEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
        {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(DURATION_STANDARD),
            ) +
                fadeIn(animationSpec = tween(DURATION_STANDARD))
        }

    val slideForwardExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(DURATION_STANDARD),
        ) +
            fadeOut(animationSpec = tween(DURATION_STANDARD))
    }

    val slideForwardPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
        {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(DURATION_STANDARD),
            ) +
                fadeIn(animationSpec = tween(DURATION_STANDARD))
        }

    val slideForwardPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
        {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(DURATION_STANDARD),
            ) +
                fadeOut(animationSpec = tween(DURATION_STANDARD))
        }

    //  Vertical slide (modals, bottom-sheet-like screens)
    val slideUpEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = tween(DURATION_STANDARD),
        ) +
            fadeIn(animationSpec = tween(DURATION_FAST))
    }

    val slideDownExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = tween(DURATION_STANDARD),
        ) +
            fadeOut(animationSpec = tween(DURATION_FAST))
    }

    //  Fade (auth flows, root-level transitions, tab switches)
    val fadeEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(DURATION_SLOW))
    }

    val fadeExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(DURATION_SLOW))
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
                animationSpec = tween(DURATION_STANDARD),
            ) +
                fadeIn(animationSpec = tween(DURATION_STANDARD))
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
                animationSpec = tween(DURATION_STANDARD),
            ) +
                fadeOut(animationSpec = tween(DURATION_STANDARD))
        }
}
