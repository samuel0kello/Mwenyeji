package com.samuelokello.mwenyeji.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.AppEasing
import com.samuelokello.mwenyeji.presentation.ui.theme.animation.Duration
import com.samuelokello.mwenyeji.presentation.ui.theme.color.LightColorScheme
import com.samuelokello.mwenyeji.presentation.ui.theme.color.MwenyejiColorScheme
import com.samuelokello.mwenyeji.presentation.ui.theme.color.resolveAppColorScheme
import com.samuelokello.mwenyeji.presentation.ui.theme.color.resolveMaterialColorScheme
import com.samuelokello.mwenyeji.presentation.ui.theme.elevation.AppElevation
import com.samuelokello.mwenyeji.presentation.ui.theme.elevation.Elevation
import com.samuelokello.mwenyeji.presentation.ui.theme.shape.AppShapes
import com.samuelokello.mwenyeji.presentation.ui.theme.shape.CornerRadii
import com.samuelokello.mwenyeji.presentation.ui.theme.shape.CornerRadius
import com.samuelokello.mwenyeji.presentation.ui.theme.shape.Shapes
import com.samuelokello.mwenyeji.presentation.ui.theme.shape.toMaterialShapes
import com.samuelokello.mwenyeji.presentation.ui.theme.spacing.AppSizes
import com.samuelokello.mwenyeji.presentation.ui.theme.spacing.AppSpacing
import com.samuelokello.mwenyeji.presentation.ui.theme.spacing.Sizes
import com.samuelokello.mwenyeji.presentation.ui.theme.spacing.Spacing
import com.samuelokello.mwenyeji.presentation.ui.theme.typography.AppFontFamily
import com.samuelokello.mwenyeji.presentation.ui.theme.typography.MwenyejiTypography
import com.samuelokello.mwenyeji.presentation.ui.theme.typography.createTypography
import com.samuelokello.mwenyeji.presentation.ui.theme.typography.toMaterialTypography

val LocalAppColorScheme = staticCompositionLocalOf { LightColorScheme }
val LocalAppTypography = staticCompositionLocalOf { createTypography() }
val LocalAppShapes = staticCompositionLocalOf { Shapes }
val LocalAppSpacing = staticCompositionLocalOf { Spacing }
val LocalAppSizes = staticCompositionLocalOf { Sizes }
val LocalAppElevation = staticCompositionLocalOf { Elevation }
val LocalCornerRadius = staticCompositionLocalOf { CornerRadii }

val LocalDuration = staticCompositionLocalOf { Duration }

val LocalEasing = staticCompositionLocalOf { AppEasing }

object MwenyejiTheme {
    val colorScheme: MwenyejiColorScheme
        @Composable @ReadOnlyComposable
        get() = LocalAppColorScheme.current

    val typography: MwenyejiTypography
        @Composable @ReadOnlyComposable
        get() = LocalAppTypography.current

    val shapes: AppShapes
        @Composable @ReadOnlyComposable
        get() = LocalAppShapes.current

    val spacing: AppSpacing
        @Composable @ReadOnlyComposable
        get() = LocalAppSpacing.current

    val sizes: AppSizes
        @Composable @ReadOnlyComposable
        get() = LocalAppSizes.current

    val elevation: AppElevation
        @Composable @ReadOnlyComposable
        get() = LocalAppElevation.current

    val cornerRadius: CornerRadius
        @Composable @ReadOnlyComposable
        get() = LocalCornerRadius.current

    val duration: Duration
        @Composable @ReadOnlyComposable
        get() = LocalDuration.current

    val easing: AppEasing
        @Composable @ReadOnlyComposable
        get() = LocalEasing.current
}

@Composable
fun MwenyejiAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val appColors = resolveAppColorScheme(darkTheme, dynamicColor)
    val materialColors = resolveMaterialColorScheme(darkTheme, dynamicColor)

    CompositionLocalProvider(
        LocalAppColorScheme provides appColors,
        LocalAppTypography provides createTypography(AppFontFamily),
        LocalAppShapes provides Shapes,
        LocalAppSpacing provides Spacing,
        LocalAppSizes provides Sizes,
        LocalAppElevation provides Elevation,
        LocalCornerRadius provides CornerRadii,
        LocalDuration provides Duration,
        LocalEasing provides AppEasing,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = createTypography(AppFontFamily).toMaterialTypography(),
            shapes = Shapes.toMaterialShapes(),
        ) {
            content()
        }
    }
}
