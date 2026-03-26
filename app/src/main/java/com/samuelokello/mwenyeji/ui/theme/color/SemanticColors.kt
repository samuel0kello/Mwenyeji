package com.samuelokello.mwenyeji.ui.theme.color

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme


object SemanticColors {

    /**
     * Text colors for different hierarchy levels
     */
    val textPrimary: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.onSurface

    val textSecondary: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.onSurfaceVariant

    val textTertiary: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.outline

    val textDisabled: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.disabled

    /**
     * Icon colors
     */
    val iconPrimary: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.onSurface

    val iconSecondary: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.onSurfaceVariant

    val iconDisabled: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.disabled

    /**
     * Interactive element colors
     */
    val ripple: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.primary.copy(alpha = 0.12f)

    val focus: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.primary.copy(alpha = 0.12f)

    val hover: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.primary.copy(alpha = 0.08f)

    val pressed: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.primary.copy(alpha = 0.16f)

    /**
     * Status colors for badges, chips, etc.
     */
    val statusActive: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.success

    val statusInactive: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.outline

    val statusPending: Color
        @Composable
        get() = MwenyejiTheme.colorScheme.warning
}