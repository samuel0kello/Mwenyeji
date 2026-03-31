package com.samuelokello.mwenyeji.ui.theme.color

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

/**
 * Convenience accessors for frequently-used semantic colour roles.
 *
 * These are thin wrappers around [MwenyejiTheme.colorScheme] — they exist so
 * call-sites can write `SemanticColors.textPrimary` instead of
 * `MwenyejiTheme.colorScheme.onSurface`, making intent explicit.
 *
 * All values are composable-only (they read the current composition local).
 */
object SemanticColors {

    // ── Text ──────────────────────────────────────────────────────────────────

    /** Primary body text and headings. */
    val textPrimary: Color
        @Composable get() = MwenyejiTheme.colorScheme.onSurface

    /** Supporting / secondary text (metadata, captions). */
    val textSecondary: Color
        @Composable get() = MwenyejiTheme.colorScheme.onSurfaceVariant

    /** Tertiary / hint text (placeholder, muted labels). */
    val textTertiary: Color
        @Composable get() = MwenyejiTheme.colorScheme.outline

    /** Disabled text. */
    val textDisabled: Color
        @Composable get() = MwenyejiTheme.colorScheme.disabled

    // ── Icons ─────────────────────────────────────────────────────────────────

    val iconPrimary: Color
        @Composable get() = MwenyejiTheme.colorScheme.onSurface

    val iconSecondary: Color
        @Composable get() = MwenyejiTheme.colorScheme.onSurfaceVariant

    val iconDisabled: Color
        @Composable get() = MwenyejiTheme.colorScheme.disabled

    // ── Interactive state layers (M3 state-layer spec) ────────────────────────

    /** Ripple layer — primary @ 12 %. */
    val ripple: Color
        @Composable get() = MwenyejiTheme.colorScheme.primary.copy(alpha = 0.12f)

    /** Focus indicator layer — primary @ 12 %. */
    val focus: Color
        @Composable get() = MwenyejiTheme.colorScheme.primary.copy(alpha = 0.12f)

    /** Hover layer — primary @ 8 %. */
    val hover: Color
        @Composable get() = MwenyejiTheme.colorScheme.primary.copy(alpha = 0.08f)

    /** Pressed / dragged layer — primary @ 16 %. */
    val pressed: Color
        @Composable get() = MwenyejiTheme.colorScheme.primary.copy(alpha = 0.16f)

    // ── Status chips / badges ─────────────────────────────────────────────────

    /** Active / confirmed status (green dot). */
    val statusActive: Color
        @Composable get() = MwenyejiTheme.colorScheme.success

    /** Inactive / unverified status. */
    val statusInactive: Color
        @Composable get() = MwenyejiTheme.colorScheme.outline

    /** Pending / needs review status. */
    val statusPending: Color
        @Composable get() = MwenyejiTheme.colorScheme.warning

    // ── Route tags ────────────────────────────────────────────────────────────

    /** "Fast / Cheap / Reliable" tag — green tonal surface. */
    val tagPositiveContainer: Color
        @Composable get() = MwenyejiTheme.colorScheme.primaryContainer

    val tagPositiveContent: Color
        @Composable get() = MwenyejiTheme.colorScheme.onPrimaryContainer

    /** Matatu / bus tag — info/blue tonal surface. */
    val tagInfoContainer: Color
        @Composable get() = MwenyejiTheme.colorScheme.infoContainer

    val tagInfoContent: Color
        @Composable get() = MwenyejiTheme.colorScheme.onInfoContainer

    /** Warning / outdated tag — amber tonal surface. */
    val tagWarningContainer: Color
        @Composable get() = MwenyejiTheme.colorScheme.warningContainer

    val tagWarningContent: Color
        @Composable get() = MwenyejiTheme.colorScheme.onWarningContainer
}