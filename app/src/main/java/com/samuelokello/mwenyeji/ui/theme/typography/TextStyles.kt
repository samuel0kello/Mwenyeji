package com.samuelokello.mwenyeji.ui.theme.typography

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

/**
 * Convenient text style helpers for common use cases
 */
object TextStyles {
    // Hero text for landing pages
    val hero: TextStyle
        @Composable
        get() = MwenyejiTheme.typography.displayLarge

    // Page titles
    val pageTitle: TextStyle
        @Composable
        get() = MwenyejiTheme.typography.headlineLarge

    // Section headers
    val sectionHeader: TextStyle
        @Composable
        get() = MwenyejiTheme.typography.headlineMedium

    // Card titles
    val cardTitle: TextStyle
        @Composable
        get() = MwenyejiTheme.typography.titleLarge

    // List item titles
    val listItemTitle: TextStyle
        @Composable
        get() = MwenyejiTheme.typography.titleMedium

    // Body text
    val body: TextStyle
        @Composable
        get() = MwenyejiTheme.typography.bodyMedium

    // Captions and helper text
    val caption: TextStyle
        @Composable
        get() = MwenyejiTheme.typography.bodySmall

    // Button text
    val button: TextStyle
        @Composable
        get() = MwenyejiTheme.typography.labelLarge

    // Input field text
    val input: TextStyle
        @Composable
        get() = MwenyejiTheme.typography.bodyLarge

    // Overline text (small all-caps labels)
    val overline: TextStyle
        @Composable
        get() = MwenyejiTheme.typography.labelSmall
}
