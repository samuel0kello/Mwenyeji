package com.samuelokello.mwenyeji.ui.theme.color

/**
 * Light theme color scheme for Mwenyeji.
 *
 * Source of truth: Mwenyeji Design System v1.0 — Figma "ColorScheme" frame.
 *
 * M3 light-theme tonal mapping:
 *   primary role  → Green40  (#2D6B4D)  — Figma brand primary, AA on white
 *   on-primary    → Green100 (#FFFFFF)
 *   surface roles → white → Neutral95 tonal ramp (near-whites with green tint)
 *
 * Problem with the previous version:
 *   • primaryLight/primaryDark were unused hover shades — corrected to
 *     Green50 (hover) / Green30 (pressed).
 *   • background was #F6FAF7 but surfaceVariant and outline didn't provide
 *     enough contrast on that tinted white. Fixed.
 *   • Semantic warning.onWarning was Color(0xFF000000) — replaced with
 *     Amber10 for proper M3 role usage.
 */
val LightColorScheme =
    MwenyejiColorScheme(
        // ── Primary ───────────────────────────────────────────────────────────────
        primary = AppColors.Green40, // #2D6B4D  — Figma brand primary
        primaryLight = AppColors.Green50, // #3D8B5D  — hover
        primaryDark = AppColors.Green30, // #1D4B35  — pressed
        onPrimary = AppColors.Green100, // #FFFFFF
        primaryContainer = AppColors.Green90, // #B7F0CC  — pale green container
        onPrimaryContainer = AppColors.Green10, // #002111
        // ── Secondary ─────────────────────────────────────────────────────────────
        secondary = AppColors.GreenGray40, // #4F6354
        onSecondary = AppColors.GreenGray100, // #FFFFFF
        secondaryContainer = AppColors.GreenGray80, // #D2E8D5
        onSecondaryContainer = AppColors.GreenGray10, // #0D1F13
        // ── Tertiary (info / matatu tag) ─────────────────────────────────────────
        tertiary = AppColors.Blue40, // #2196F3  — Figma Info
        onTertiary = AppColors.Blue100, // #FFFFFF
        tertiaryContainer = AppColors.Blue90, // #BBDEFB
        onTertiaryContainer = AppColors.Blue10, // #003258
        // ── Background & Surface ──────────────────────────────────────────────────
        // background = near-white with subtle green brand tint (Figma GreenGray99)
        background = AppColors.GreenGray99, // #F6FAF7
        onBackground = AppColors.Neutral10, // #121212  — near-black text
        surface = AppColors.Neutral100, // #FFFFFF
        onSurface = AppColors.Neutral10, // #121212
        surfaceVariant = AppColors.NeutralVariant90, // #DEE5DA
        onSurfaceVariant = AppColors.NeutralVariant40, // #565E59
        surfaceTint = AppColors.Green40, // = primary
        surfaceDim = AppColors.NeutralVariant90, // slightly darker than surface
        surfaceBright = AppColors.GreenGray99, // fully lit = background
        surfaceContainerLowest = AppColors.Neutral100, // #FFFFFF
        surfaceContainerLow = AppColors.Neutral94, // #F0F5F1
        surfaceContainer = AppColors.Neutral92, // #ECEDE7  — card bg
        surfaceContainerHigh = AppColors.NeutralVariant90, // #DEE5DA
        surfaceContainerHighest = AppColors.Neutral87, // #DEE5DA
        // ── Inverse ───────────────────────────────────────────────────────────────
        inverseSurface = AppColors.Neutral17, // #1C2119  — dark snackbar bg
        inverseOnSurface = AppColors.Neutral94, // #F0F5F1  — text on dark snackbar
        inversePrimary = AppColors.Green80, // #9BD4B1  — on dark surfaces
        // ── Outline & Border ──────────────────────────────────────────────────────
        // Increased contrast vs previous version — GreenGray40 is darker / more legible
        outline = AppColors.GreenGray60, // #8C9B8F  — sufficient on white
        outlineVariant = AppColors.NeutralVariant60, // #8C9B8F  — subtle on light bg
        border = AppColors.Neutral87, // #DEE5DA  — card strokes light
        divider = AppColors.Neutral94, // #F0F5F1  — row separators
        // ── Semantic: Success (#4CAF50) ───────────────────────────────────────────
        success = AppColors.SuccessGreen40, // #4CAF50  — exact Figma
        onSuccess = AppColors.SuccessGreen100, // #FFFFFF
        successContainer = AppColors.SuccessGreen90, // #C8E6C9
        onSuccessContainer = AppColors.SuccessGreen10, // #1B5E20
        // ── Semantic: Info (#2196F3) ──────────────────────────────────────────────
        info = AppColors.Blue40, // #2196F3  — exact Figma
        onInfo = AppColors.Blue100, // #FFFFFF
        infoContainer = AppColors.Blue90, // #BBDEFB
        onInfoContainer = AppColors.Blue30, // #01579B
        // ── Semantic: Warning (#FF9800) ───────────────────────────────────────────
        warning = AppColors.Amber40, // #FF9800  — exact Figma
        onWarning = AppColors.Amber10, // #4A2800  (was Color(black) — fixed)
        warningContainer = AppColors.Amber90, // #FFE0B2
        onWarningContainer = AppColors.Amber30, // #E65100
        // ── Semantic: Error (#F44336) ─────────────────────────────────────────────
        error = AppColors.Red40, // #BA1A1A  (M3 light = tonal 40)
        onError = AppColors.Red100, // #FFFFFF
        errorContainer = AppColors.Red90, // #FFDA D6
        onErrorContainer = AppColors.Red10, // #410002
        // ── Overlays & Scrims ─────────────────────────────────────────────────────
        scrim = AppColors.Neutral0.copy(alpha = 0.60f), // #000000 @ 60%
        overlay = AppColors.Neutral0.copy(alpha = 0.32f), // #000000 @ 32%
        // ── Interactive States ────────────────────────────────────────────────────
        disabled = AppColors.Neutral0.copy(alpha = 0.38f), // black @ 38%
        disabledContainer = AppColors.Neutral0.copy(alpha = 0.12f), // black @ 12%
        // ── Shadow ────────────────────────────────────────────────────────────────
        shadow = AppColors.Neutral0, // #000000
    )
