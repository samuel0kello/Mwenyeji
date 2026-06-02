package com.samuelokello.mwenyeji.presentation.ui.theme.icons

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Icon size tokens for consistent icon sizing
 */
@Immutable
data class IconSize(
    val tiny: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
)

val IconSizes =
    IconSize(
        tiny = 16.dp,
        small = 20.dp,
        medium = 24.dp,
        large = 32.dp,
        extraLarge = 48.dp,
    )
