package com.samuelokello.mwenyeji.ui.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Mwenyeji custom color scheme.
 */
@Immutable
data class MwenyejiColorScheme(
    // ── Primary ──────────────────────────────────────────────────────────────
    val primary: Color,
    val primaryLight: Color, // hover / lighter tonal stop
    val primaryDark: Color, // pressed / deeper tonal stop
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    // ── Secondary ─────────────────────────────────────────────────────────────
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    // ── Tertiary ──────────────────────────────────────────────────────────────
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    // ── Background & Surface
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceTint: Color, // always = primary
    val surfaceDim: Color,
    val surfaceBright: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color, // card / illustration bg
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    // ── Inverse
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color,
    // ── Outline & Border
    val outline: Color,
    val outlineVariant: Color,
    val border: Color, // structural dividers / card strokes
    val divider: Color, // subtle row separators
    // ── Semantic: Success
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    // ── Semantic: Info
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    // ── Semantic: Warning
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    // ── Semantic: Error
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    // ── Overlays & Scrims
    val scrim: Color,
    val overlay: Color,
    // ── Interactive States
    val disabled: Color,
    val disabledContainer: Color,
    // ── Shadow
    val shadow: Color,
)
