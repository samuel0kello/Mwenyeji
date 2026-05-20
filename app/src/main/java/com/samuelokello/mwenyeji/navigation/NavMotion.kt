package com.samuelokello.mwenyeji.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry

data class NavMotion(
    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition,
    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition,
    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = enter,
    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = exit,
)

object NavMotions {
    val slide =
        NavMotion(
            enter = NavAnimations.slideForwardEnter,
            exit = NavAnimations.slideForwardExit,
            popEnter = NavAnimations.slideForwardPopEnter,
            popExit = NavAnimations.slideForwardPopExit,
        )

    val dynamicTab =
        NavMotion(
            enter = NavAnimations.tabEnterTransition,
            exit = NavAnimations.tabExitTransition,
            popEnter = NavAnimations.tabEnterTransition,
            popExit = NavAnimations.tabExitTransition,
        )

    val modal =
        NavMotion(
            enter = NavAnimations.slideUpEnter,
            exit = NavAnimations.slideDownExit,
            // Pop reverses the entrance — slide back down
            popEnter = NavAnimations.slideUpEnter,
            popExit = NavAnimations.slideDownExit,
        )

    val fade =
        NavMotion(
            enter = NavAnimations.fadeEnter,
            exit = NavAnimations.fadeExit,
        )

    val none =
        NavMotion(
            enter = NavAnimations.none,
            exit = NavAnimations.noneExit,
        )

    val sheetUp =
        NavMotion(
            enter = NavAnimations.slideUpEnter,
            exit = NavAnimations.slideDownExit,
            popEnter = NavAnimations.slideUpEnter,
            popExit = NavAnimations.slideDownExit,
        )
}
