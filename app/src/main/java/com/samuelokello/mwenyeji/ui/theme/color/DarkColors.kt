package com.samuelokello.mwenyeji.ui.theme.color

import androidx.compose.ui.graphics.Color

/**
 * Dark theme color palette for Mwenyeji
 */
val DarkColorScheme = AppColorScheme(
    // Primary
    primary = Color(0xFF9BD4B1),
    primaryLight = Color(0xFFB7F0CC),
    primaryDark = Color(0xFF1D3D2A),          // deep green — used for icon circle bg
    onPrimary = Color(0xFF003921),
    primaryContainer = Color(0xFF1D3D2A),      // matches primaryDark for circle consistency
    onPrimaryContainer = Color(0xFFB7F0CC),

    // Secondary
    secondary = Color(0xFFB6CCB9),
    onSecondary = Color(0xFF223527),
    secondaryContainer = Color(0xFF384B3C),
    onSecondaryContainer = Color(0xFFD2E8D5),

    // Tertiary
    tertiary = Color(0xFFA5CCDE),
    onTertiary = Color(0xFF073543),
    tertiaryContainer = Color(0xFF244C5A),
    onTertiaryContainer = Color(0xFFC1E8FB),

    // Background & Surface
    background = Color(0xFF0E1210),            // very dark green-black, richer than neutral black
    onBackground = Color(0xFFE5E6E0),
    surface = Color(0xFF161A17),               // slightly lighter than background
    onSurface = Color(0xFFE5E6E0),
    surfaceVariant = Color(0xFF424940),
    onSurfaceVariant = Color(0xFFC2C9BE),
    surfaceTint = Color(0xFF9BD4B1),           // always matches primary
    surfaceDim = Color(0xFF0E1210),            // matches background — dimmest surface
    surfaceBright = Color(0xFF383A35),         // elevated surfaces
    surfaceContainerLowest = Color(0xFF0A0F0C),
    surfaceContainerLow = Color(0xFF161A17),
    surfaceContainer = Color(0xFF1C2119),      // illustration card bg — dark green tinted
    surfaceContainerHigh = Color(0xFF252B22),
    surfaceContainerHighest = Color(0xFF303530),

    // Inverse
    inverseSurface = Color(0xFFE5E6E0),
    inverseOnSurface = Color(0xFF2F312D),
    inversePrimary = Color(0xFF2D6B4D),

    // Outline & Border
    outline = Color(0xFF8C9388),
    outlineVariant = Color(0xFF424940),
    border = Color(0xFF2E3330),
    divider = Color(0xFF252925),

    // Semantic - Success
    success = Color(0xFF81C784),
    onSuccess = Color(0xFF003909),
    successContainer = Color(0xFF00530F),
    onSuccessContainer = Color(0xFF9EDD9F),

    // Semantic - Info
    info = Color(0xFF64B5F6),
    onInfo = Color(0xFF003258),
    infoContainer = Color(0xFF00497D),
    onInfoContainer = Color(0xFFBBDEFB),

    // Semantic - Warning
    warning = Color(0xFFFFB74D),
    onWarning = Color(0xFF4A2800),
    warningContainer = Color(0xFF6A3C00),
    onWarningContainer = Color(0xFFFFDDB3),

    // Semantic - Error
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Overlays
    scrim = Color(0xB3000000),
    overlay = Color(0x80000000),

    // Interactive states
    disabled = Color(0x61FFFFFF),
    disabledContainer = Color(0x1FFFFFFF),

    // Shadow
    shadow = Color(0xFF000000),
)