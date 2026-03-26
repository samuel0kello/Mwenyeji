package com.samuelokello.mwenyeji.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.samuelokello.mwenyeji.ui.theme.color.*
import com.samuelokello.mwenyeji.ui.theme.typography.*
import com.samuelokello.mwenyeji.ui.theme.shape.*
import com.samuelokello.mwenyeji.ui.theme.spacing.*
import com.samuelokello.mwenyeji.ui.theme.elevation.*

/**
 * CompositionLocal providers for theme tokens
 */
val LocalAppColorScheme = staticCompositionLocalOf { LightColorScheme }
val LocalAppTypography = staticCompositionLocalOf { createTypography() }
val LocalAppShapes = staticCompositionLocalOf { Shapes }
val LocalAppSpacing = staticCompositionLocalOf { Spacing }
val LocalAppSizes = staticCompositionLocalOf { Sizes }
val LocalAppElevation = staticCompositionLocalOf { Elevation }
val LocalCornerRadius = staticCompositionLocalOf { CornerRadii }

/**
 * Main theme object for Mwenyeji app
 * Provides access to all design tokens
 *
 * Usage:
 * ```
 * Text(
 *     text = "Hello",
 *     color = MwenyejiTheme.colorScheme.primary,
 *     style = MwenyejiTheme.typography.bodyLarge
 * )
 * ```
 */
object MwenyejiTheme {

    /**
     * Color scheme tokens
     */
    val colorScheme: AppColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColorScheme.current

    /**
     * Typography tokens
     */
    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current

    /**
     * Shape tokens
     */
    val shapes: AppShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAppShapes.current

    /**
     * Spacing tokens
     */
    val spacing: AppSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAppSpacing.current

    /**
     * Size tokens
     */
    val sizes: AppSizes
        @Composable
        @ReadOnlyComposable
        get() = LocalAppSizes.current

    /**
     * Elevation tokens
     */
    val elevation: AppElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalAppElevation.current

    /**
     * Corner radius tokens (Dp values)
     */
    val cornerRadius: CornerRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalCornerRadius.current
}

/**
 * Main theme composable
 * Wraps your app content and provides all theme tokens
 *
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param dynamicColor Whether to use dynamic theming from Android 12+
 * @param content Your app content
 */
@Composable
fun MwenyejiAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkColorScheme else LightColorScheme
    val materialColors = appColors.toMaterialColorScheme(darkTheme)  // ← one line

    CompositionLocalProvider(
        LocalAppColorScheme provides appColors,
        LocalAppTypography provides createTypography(AppFontFamily),
        LocalAppShapes provides Shapes,
        LocalAppSpacing provides Spacing,
        LocalAppSizes provides Sizes,
        LocalAppElevation provides Elevation,
        LocalCornerRadius provides CornerRadii,
    ) {
        MaterialTheme(colorScheme = materialColors) {
            content()
        }
    }
}