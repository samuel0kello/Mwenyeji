package com.samuelokello.mwenyeji.presentation.designsystem.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.AppEasing
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.Duration
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.appTween

private fun offsetIdentity(fullHeight: Int): Int = fullHeight

private fun offsetNegativeIdentity(fullHeight: Int): Int = -fullHeight

private fun offsetWithPadding(fullHeight: Int): Int = -fullHeight - 200

object MwenyejiAnimations {
    val fadeIn: EnterTransition get() = fadeIn(animationSpec = appTween(Duration.MEDIUM))
    val fadeOut: ExitTransition get() = fadeOut(animationSpec = appTween(Duration.MEDIUM))

    val scaleIn: EnterTransition
        get() =
            scaleIn(
                animationSpec =
                    appTween(
                        Duration.MEDIUM,
                        AppEasing.emphasized,
                    ),
            )
    val scaleOut: ExitTransition
        get() =
            scaleOut(
                animationSpec =
                    appTween(
                        Duration.MEDIUM,
                        AppEasing.emphasized,
                    ),
            )

    val slideUpEnter: EnterTransition
        get() =
            slideInVertically(
                initialOffsetY = ::offsetIdentity,
                animationSpec = appTween(Duration.NORMAL, AppEasing.emphasized),
            ) +
                fadeIn

    val slideUpExit: ExitTransition
        get() =
            slideOutVertically(
                targetOffsetY = ::offsetNegativeIdentity,
                animationSpec = appTween(Duration.NORMAL, AppEasing.emphasized),
            ) +
                fadeOut

    val slideDownEnter: EnterTransition
        get() =
            slideInVertically(
                initialOffsetY = ::offsetWithPadding, // Start further up to ensure it's behind the app bar
                animationSpec = appTween(Duration.NORMAL, AppEasing.emphasized),
            ) +
                fadeIn

    val slideDownExit: ExitTransition
        get() =
            slideOutVertically(
                targetOffsetY = ::offsetIdentity,
                animationSpec = appTween(Duration.NORMAL, AppEasing.emphasized),
            ) +
                fadeOut

    val slideInEnd: EnterTransition
        get() =
            slideInHorizontally(
                initialOffsetX = ::offsetIdentity,
                animationSpec = appTween(Duration.NORMAL, AppEasing.emphasized),
            ) +
                fadeIn

    val slideOutEnd: ExitTransition
        get() =
            slideOutHorizontally(
                targetOffsetX = ::offsetIdentity,
                animationSpec = appTween(Duration.NORMAL, AppEasing.emphasized),
            ) +
                fadeOut

    val expandVertically: EnterTransition
        get() =
            expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = appTween(Duration.MEDIUM, AppEasing.standard),
            ) +
                fadeIn

    val shrinkVertically: ExitTransition
        get() =
            shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = appTween(Duration.MEDIUM, AppEasing.standard),
            ) +
                fadeOut
}

@Composable
fun MwenyejiAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = MwenyejiAnimations.fadeIn + MwenyejiAnimations.scaleIn,
    exit: ExitTransition = MwenyejiAnimations.fadeOut + MwenyejiAnimations.scaleOut,
    label: String = "MwenyejiAnimatedVisibility",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        label = label,
        content = content,
    )
}

@Composable
fun FadeAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    MwenyejiAnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = MwenyejiAnimations.fadeIn,
        exit = MwenyejiAnimations.fadeOut,
        label = "FadeAnimatedVisibility",
        content = content,
    )
}

@Composable
fun SlideAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    MwenyejiAnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = MwenyejiAnimations.slideUpEnter,
        exit = MwenyejiAnimations.slideDownExit,
        label = "SlideAnimatedVisibility",
        content = content,
    )
}

@Composable
fun ScaleAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    MwenyejiAnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = MwenyejiAnimations.scaleIn + MwenyejiAnimations.fadeIn,
        exit = MwenyejiAnimations.scaleOut + MwenyejiAnimations.fadeOut,
        label = "ScaleAnimatedVisibility",
        content = content,
    )
}

@Composable
fun ExpandAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    MwenyejiAnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = MwenyejiAnimations.expandVertically,
        exit = MwenyejiAnimations.shrinkVertically,
        label = "ExpandAnimatedVisibility",
        content = content,
    )
}
