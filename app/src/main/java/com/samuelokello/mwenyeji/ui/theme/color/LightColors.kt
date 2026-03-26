package com.samuelokello.mwenyeji.ui.theme.color

import androidx.compose.ui.graphics.Color

/**
 * Light theme color palette for Mwenyeji
 */
val LightColorScheme = AppColorScheme(
    // Primary
    primary = Color(0xFF2D6B4D),              // dark green — visible on light bg
    primaryLight = Color(0xFF3D8B5D),
    primaryDark = Color(0xFF1D3D2A),          // deep green — used for icon circle bg
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8EDDA),      // pale green tint for containers
    onPrimaryContainer = Color(0xFF00210F),

    // Secondary
    secondary = Color(0xFF4F6354),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E8D5),
    onSecondaryContainer = Color(0xFF0D1F13),

    // Tertiary
    tertiary = Color(0xFF3E616F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC1E8FB),
    onTertiaryContainer = Color(0xFF001E28),

    // Background & Surface
    background = Color(0xFFF6FAF7),            // near white with subtle green tint
    onBackground = Color(0xFF1A1C19),          // dark text on light bg
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDEE5DA),
    onSurfaceVariant = Color(0xFF424940),
    surfaceTint = Color(0xFF2D6B4D),           // always matches primary
    surfaceDim = Color(0xFFD9DED9),            // surface in shadow — slightly darker
    surfaceBright = Color(0xFFF6FAF7),         // fully lit — matches background
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F5F1),
    surfaceContainer = Color(0xFFE4EBE5),      // illustration card — light green-gray
    surfaceContainerHigh = Color(0xFFDEE5DF),
    surfaceContainerHighest = Color(0xFFD8DFD9),

    // Inverse — flipped for snackbars, tooltips
    inverseSurface = Color(0xFF2F312D),
    inverseOnSurface = Color(0xFFF1F1EB),
    inversePrimary = Color(0xFF9BD4B1),

    // Outline & Border
    outline = Color(0xFF5C6358),               // darker than dark theme for light bg contrast
    outlineVariant = Color(0xFFC2C9BE),
    border = Color(0xFFE0E0E0),
    divider = Color(0xFFE8EDE9),

    // Semantic - Success
    success = Color(0xFF4CAF50),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFC8E6C9),
    onSuccessContainer = Color(0xFF1B5E20),

    // Semantic - Info
    info = Color(0xFF2196F3),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFBBDEFB),
    onInfoContainer = Color(0xFF01579B),

    // Semantic - Warning
    warning = Color(0xFFFF9800),
    onWarning = Color(0xFF000000),
    warningContainer = Color(0xFFFFE0B2),
    onWarningContainer = Color(0xFFE65100),

    // Semantic - Error
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Overlays
    scrim = Color(0x99000000),
    overlay = Color(0x52000000),

    // Interactive states
    disabled = Color(0x61000000),
    disabledContainer = Color(0x1F000000),

    // Shadow
    shadow = Color(0xFF000000),
)