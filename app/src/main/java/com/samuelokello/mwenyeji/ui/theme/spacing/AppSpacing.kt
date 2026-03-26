package com.samuelokello.mwenyeji.ui.theme.spacing

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing system for Mwenyeji app
 * Provides consistent spacing/padding values throughout the app
 * Based on 4dp grid system
 */
@Immutable
data class AppSpacing(
    val none: Dp,
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
    val xxLarge: Dp,
    val xxxLarge: Dp,
)

val Spacing = AppSpacing(
    none = 0.dp,
    extraSmall = 4.dp,
    small = 8.dp,
    medium = 16.dp,
    large = 24.dp,
    extraLarge = 32.dp,
    xxLarge = 48.dp,
    xxxLarge = 64.dp,
)