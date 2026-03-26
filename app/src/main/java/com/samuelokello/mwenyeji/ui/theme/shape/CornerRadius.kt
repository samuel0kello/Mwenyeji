package com.samuelokello.mwenyeji.ui.theme.shape

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Corner radius values as Dp units
 * Use these when you need raw Dp values instead of Shape objects
 */
@Immutable
data class CornerRadius(
    val none: Dp,
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
)

val CornerRadii = CornerRadius(
    none = 0.dp,
    extraSmall = 4.dp,
    small = 8.dp,
    medium = 12.dp,
    large = 16.dp,
    extraLarge = 28.dp,
)