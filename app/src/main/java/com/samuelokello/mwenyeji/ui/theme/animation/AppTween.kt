package com.samuelokello.mwenyeji.ui.theme.animation

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween

/** A tween using the app's design tokens. */
fun <T> appTween(durationMillis: Int = Duration.MEDIUM, easing: Easing = AppEasing.standard): TweenSpec<T> =
    tween(durationMillis = durationMillis, easing = easing)
