package com.samuelokello.mwenyeji.ui.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color


@Immutable
data class AppColorScheme(
    // Primary colors
    val primary: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,

    // Secondary colors
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,

    // Tertiary colors (optional accent)
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,

    // Background & Surface
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceTint: Color,
    val surfaceDim: Color,
    val surfaceBright: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,

    // Inverse colors
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color,

    // Outline & Border
    val outline: Color,
    val outlineVariant: Color,
    val border: Color,
    val divider: Color,

    // Semantic colors
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,

    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,

    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,

    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,

    // Overlays & Scrims
    val scrim: Color,
    val overlay: Color,

    // Interactive states
    val disabled: Color,
    val disabledContainer: Color,

    // Shadow
    val shadow: Color,
)