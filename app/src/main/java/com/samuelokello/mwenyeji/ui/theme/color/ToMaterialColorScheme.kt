package com.samuelokello.mwenyeji.ui.theme.color

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Maps [MwenyejiColorScheme] to a Material 3 [ColorScheme].
 *
 * All surface container roles are now populated — the previous version left
 * surfaceContainerLowest…surfaceContainerHighest unmapped (they defaulted to
 * Material defaults instead of our custom tones).
 */
fun MwenyejiColorScheme.toMaterialColorScheme(darkTheme: Boolean): ColorScheme =
    if (darkTheme) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            outline = outline,
            outlineVariant = outlineVariant,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            scrim = scrim,
            surfaceDim = surfaceDim,
            surfaceBright = surfaceBright,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            outline = outline,
            outlineVariant = outlineVariant,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            scrim = scrim,
            surfaceDim = surfaceDim,
            surfaceBright = surfaceBright,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
        )
    }

/**
 * Builds a dynamic Material 3 [ColorScheme] from the device wallpaper
 * (Android 12+ / API 31+). Falls back to [LightColorScheme] / [DarkColorScheme]
 * on older devices.
 *
 * Dynamic color completely replaces our hand-crafted green palette with the
 * wallpaper-extracted scheme, which is correct M3 behaviour. Our custom
 * semantic roles (success, info, warning, border, etc.) are layered on top
 * in [MwenyejiAppTheme] via [LocalAppColorScheme] — they are NOT overridden
 * by dynamic color.
 *
 * Usage: call from [MwenyejiAppTheme] when [dynamicColor] = true.
 */
@Composable
fun resolveMaterialColorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme =
    when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> {
            DarkColorScheme.toMaterialColorScheme(darkTheme = true)
        }

        else -> {
            LightColorScheme.toMaterialColorScheme(darkTheme = false)
        }
    }

/**
 * Builds the [MwenyejiColorScheme] companion that adds our extra semantic/custom
 * roles on top of whatever Material scheme is active.
 *
 * When dynamic color is active we keep our semantic palette (success/info/
 * warning/border/…) but pull primary/secondary/surface roles from the wallpaper
 * scheme so everything feels coherent.
 *
 * On API < 31 this just returns [DarkColorScheme] or [LightColorScheme] as-is.
 */
@Composable
fun resolveAppColorScheme(darkTheme: Boolean, dynamicColor: Boolean): MwenyejiColorScheme {
    if (!dynamicColor || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return if (darkTheme) DarkColorScheme else LightColorScheme
    }

    // API 31+ with dynamic color:
    // Re-use our static scheme as base then override M3 roles with wallpaper values.
    val context = LocalContext.current
    val dynamic =
        if (darkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    val base = if (darkTheme) DarkColorScheme else LightColorScheme

    return base.copy(
        // Primary roles — from wallpaper
        primary = dynamic.primary,
        onPrimary = dynamic.onPrimary,
        primaryContainer = dynamic.primaryContainer,
        onPrimaryContainer = dynamic.onPrimaryContainer,
        // Keep primaryLight/primaryDark as tonal neighbours of wallpaper primary
        primaryLight = dynamic.primary.lighten(0.10f),
        primaryDark = dynamic.primary.darken(0.10f),
        // Secondary roles — from wallpaper
        secondary = dynamic.secondary,
        onSecondary = dynamic.onSecondary,
        secondaryContainer = dynamic.secondaryContainer,
        onSecondaryContainer = dynamic.onSecondaryContainer,
        // Tertiary roles — from wallpaper
        tertiary = dynamic.tertiary,
        onTertiary = dynamic.onTertiary,
        tertiaryContainer = dynamic.tertiaryContainer,
        onTertiaryContainer = dynamic.onTertiaryContainer,
        // Surface hierarchy — from wallpaper
        background = dynamic.background,
        onBackground = dynamic.onBackground,
        surface = dynamic.surface,
        onSurface = dynamic.onSurface,
        surfaceVariant = dynamic.surfaceVariant,
        onSurfaceVariant = dynamic.onSurfaceVariant,
        surfaceTint = dynamic.surfaceTint,
        surfaceDim = dynamic.surfaceDim,
        surfaceBright = dynamic.surfaceBright,
        surfaceContainerLowest = dynamic.surfaceContainerLowest,
        surfaceContainerLow = dynamic.surfaceContainerLow,
        surfaceContainer = dynamic.surfaceContainer,
        surfaceContainerHigh = dynamic.surfaceContainerHigh,
        surfaceContainerHighest = dynamic.surfaceContainerHighest,
        // Inverse — from wallpaper
        inverseSurface = dynamic.inverseSurface,
        inverseOnSurface = dynamic.inverseOnSurface,
        inversePrimary = dynamic.inversePrimary,
        // Outline — from wallpaper
        outline = dynamic.outline,
        outlineVariant = dynamic.outlineVariant,
        // border/divider are structural; keep static — wallpaper doesn't define them
        // border = base.border  (unchanged)
        // divider = base.divider (unchanged)
        // Error — from wallpaper (keeps system error colour coherent with dynamic scheme)
        error = dynamic.error,
        onError = dynamic.onError,
        errorContainer = dynamic.errorContainer,
        onErrorContainer = dynamic.onErrorContainer,
        // Semantic success/info/warning — ALWAYS our brand palette.
        // Dynamic color doesn't supply these roles.
        // success / info / warning stay as base.success etc. — no override needed.
    )
}

// ── Colour helpers ────────────────────────────────────────────────────────────

private fun androidx.compose.ui.graphics.Color.lighten(fraction: Float): androidx.compose.ui.graphics.Color =
    copy(
        red = (red + fraction).coerceAtMost(1f),
        green = (green + fraction).coerceAtMost(1f),
        blue = (blue + fraction).coerceAtMost(1f),
    )

private fun androidx.compose.ui.graphics.Color.darken(fraction: Float): androidx.compose.ui.graphics.Color =
    copy(
        red = (red - fraction).coerceAtLeast(0f),
        green = (green - fraction).coerceAtLeast(0f),
        blue = (blue - fraction).coerceAtLeast(0f),
    )
