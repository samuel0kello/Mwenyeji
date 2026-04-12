package com.samuelokello.mwenyeji.ui.designsystem.components.card

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

/**
 * Default values for card components
 */
object MwenyejiCardDefaults {
    val contentPadding: PaddingValues
        @Composable
        get() = PaddingValues(MwenyejiTheme.spacing.medium)

    val compactContentPadding: PaddingValues
        @Composable
        get() = PaddingValues(MwenyejiTheme.spacing.small)

    val largeContentPadding: PaddingValues
        @Composable
        get() = PaddingValues(MwenyejiTheme.spacing.large)

    val borderWidth: Dp = 1.dp
}
