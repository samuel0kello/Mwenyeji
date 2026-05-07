package com.samuelokello.mwenyeji.presentation.ui.theme.spacing

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Size tokens for component dimensions
 * Use these for fixed-size elements like icons, avatars, etc.
 */
@Immutable
data class AppSizes(
    // Icon sizes
    val iconExtraSmall: Dp,
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,
    val iconExtraLarge: Dp,
    // Avatar/Profile picture sizes
    val avatarSmall: Dp,
    val avatarMedium: Dp,
    val avatarLarge: Dp,
    val avatarExtraLarge: Dp,
    // Button heights
    val buttonSmall: Dp,
    val buttonMedium: Dp,
    val buttonLarge: Dp,
    // TextField heights
    val textFieldSmall: Dp,
    val textFieldMedium: Dp,
    val textFieldLarge: Dp,
    // Common fixed widths
    val minimumTouchTarget: Dp,
    val fabSize: Dp,
    val bottomBarHeight: Dp,
    val topBarHeight: Dp,
    // Borders
    val borderWidth: Dp,
    val borderWidthThick: Dp,
)

val Sizes =
    AppSizes(
        // Icons
        iconExtraSmall = 16.dp,
        iconSmall = 20.dp,
        iconMedium = 24.dp,
        iconLarge = 32.dp,
        iconExtraLarge = 48.dp,
        // Avatars
        avatarSmall = 32.dp,
        avatarMedium = 40.dp,
        avatarLarge = 56.dp,
        avatarExtraLarge = 96.dp,
        // Buttons
        buttonSmall = 32.dp,
        buttonMedium = 40.dp,
        buttonLarge = 48.dp,
        // TextFields
        textFieldSmall = 40.dp,
        textFieldMedium = 48.dp,
        textFieldLarge = 56.dp,
        // Common
        minimumTouchTarget = 48.dp,
        fabSize = 56.dp,
        bottomBarHeight = 80.dp,
        topBarHeight = 64.dp,
        // Borders
        borderWidth = 1.dp,
        borderWidthThick = 2.dp,
    )
