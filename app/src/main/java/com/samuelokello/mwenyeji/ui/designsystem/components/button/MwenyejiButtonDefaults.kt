package com.samuelokello.mwenyeji.ui.designsystem.components.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

/**
 * Default values for button components
 */
object MwenyejiButtonDefaults {
    val contentPadding: PaddingValues
        @Composable
        get() =
            PaddingValues(
                horizontal = MwenyejiTheme.spacing.medium,
                vertical = MwenyejiTheme.spacing.small,
            )

    val compactContentPadding: PaddingValues
        @Composable
        get() =
            PaddingValues(
                horizontal = MwenyejiTheme.spacing.small,
                vertical = MwenyejiTheme.spacing.extraSmall,
            )

    val largeContentPadding: PaddingValues
        @Composable
        get() =
            PaddingValues(
                horizontal = MwenyejiTheme.spacing.large,
                vertical = MwenyejiTheme.spacing.medium,
            )

    val iconSpacing
        @Composable
        get() = MwenyejiTheme.spacing.small
}
