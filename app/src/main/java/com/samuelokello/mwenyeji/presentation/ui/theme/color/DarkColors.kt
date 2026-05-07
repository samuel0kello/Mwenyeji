package com.samuelokello.mwenyeji.presentation.ui.theme.color

/**
 * Dark theme color scheme for Mwenyeji.
 *
 * Source of truth: Mwenyeji Design System v1.0 —  "ColorScheme" frame.
 * Figma exact values used as anchors:
 *   Background  #121212   Surface     #1E1E1E
 *   SurfaceRaised #2A2A2A Border      #333333
 *   Primary     #2D6B4D   PrimaryLight #3D88B0  PrimaryDark #1D4B35
 *   Success     #4CAF50   Info        #2196F3
 *   Warning     #FF9800   Error       #F44336
 *
 * M3 dark-theme tonal mapping:
 *   primary role  → Green80  (#9BD4B1)  — light enough on dark bg, AA contrast
 *   on-primary    → Green20  (#003920)  — text/icon ON the primary-colored button
 *   surface roles → Neutral6–Neutral30 tonal ramp (green-tinted near-blacks)
 */
val DarkColorScheme =
    MwenyejiColorScheme(
        // ── Primary ───────────────────────────────────────────────────────────────
        // M3 rule: primary in dark = ~tonal 80. Green80 = #9BD4B1.
        primary = AppColors.Green80, // #9BD4B1
        primaryLight = AppColors.Green90, // #B7F0CC  — hover
        primaryDark = AppColors.Green70, // #6DC496  — pressed
        onPrimary = AppColors.Green20, // #003920
        primaryContainer = AppColors.Green30, // #1D4B35  — icon circle bg
        onPrimaryContainer = AppColors.Green90, // #B7F0CC
        // Secondary
        secondary = AppColors.GreenGray70, // #B6CCB9
        onSecondary = AppColors.GreenGray20, // #223527
        secondaryContainer = AppColors.GreenGray30, // #384B3C
        onSecondaryContainer = AppColors.GreenGray80, // #D2E8D5
        // ── Tertiary (info / matatu tag) ─────────────────────────────────────────
        // Figma Info/Matatu = #2196F3 → blue tonal ramp
        tertiary = AppColors.Blue80, // #90CAF9
        onTertiary = AppColors.Blue10, // #003258
        tertiaryContainer = AppColors.Blue20, // #00497D
        onTertiaryContainer = AppColors.Blue90, // #BBDEFB
        // ── Background & Surface ──────────────────────────────────────────────────
        // Figma: Background #121212 — we use green-tinted 0E1210 to honour brand warmth
        background = AppColors.Neutral6, // #0E1210  (≈ Figma #121212, green tint)
        onBackground = AppColors.Neutral90, // #E5E6E0
        surface = AppColors.Neutral12, // #161A17  (Figma Surface #1E1E1E → green tint)
        onSurface = AppColors.Neutral90, // #E5E6E0
        surfaceVariant = AppColors.Neutral50, // #424940
        onSurfaceVariant = AppColors.NeutralVariant80, // #C2C9BE
        surfaceTint = AppColors.Green80, // = primary
        surfaceDim = AppColors.Neutral6, // dimmest = background
        surfaceBright = AppColors.Neutral40, // #383A35  — most elevated
        surfaceContainerLowest = AppColors.Neutral4, // #0A0F0C
        surfaceContainerLow = AppColors.Neutral12, // #161A17
        surfaceContainer = AppColors.Neutral17, // #1C2119  — card / illustration bg
        surfaceContainerHigh = AppColors.Neutral22, // #252B22
        surfaceContainerHighest = AppColors.Neutral30, // #303530
        //  Inverse
        inverseSurface = AppColors.Neutral90, // #E5E6E0
        inverseOnSurface = AppColors.Neutral17, // #1C2119
        inversePrimary = AppColors.Green40, // #2D6B4D  — Figma brand primary
        // Outline & Border
        outline = AppColors.Neutral70, // #8C9388
        outlineVariant = AppColors.Neutral50, // #424940
        border = AppColors.Neutral35, // #333333  — exact Figma Border
        divider = AppColors.Neutral22, // #252B22  — subtle row separators
        // Semantic: Success (#4CAF50)
        success = AppColors.SuccessGreen80, // #81C784  (M3 dark = tonal 80)
        onSuccess = AppColors.SuccessGreen20, // #003909
        successContainer = AppColors.SuccessGreen30, // #00530F
        onSuccessContainer = AppColors.SuccessGreen80, // #81C784
        //  Semantic: Info (#2196F3)
        info = AppColors.Blue80, // #90CAF9
        onInfo = AppColors.Blue10, // #003258
        infoContainer = AppColors.Blue20, // #00497D
        onInfoContainer = AppColors.Blue90, // #BBDEFB
        //  Semantic: Warning (#FF9800)
        warning = AppColors.Amber60, // #FFB74D  (M3 dark = lighter tonal)
        onWarning = AppColors.Amber10, // #4A2800
        warningContainer = AppColors.Amber20, // #6A3C00
        onWarningContainer = AppColors.Amber80, // #FFCC80
        // Semantic: Error (#F44336)
        error = AppColors.Red80, // #FFB4AB  (M3 dark = tonal 80)
        onError = AppColors.Red20, // #690005
        errorContainer = AppColors.Red30, // #93000A
        onErrorContainer = AppColors.Red90, // #FFDA D6
        // Overlays & Scrims
        scrim = AppColors.Neutral0.copy(alpha = 0.70f), // #000000 @ 70%
        overlay = AppColors.Neutral0.copy(alpha = 0.50f), // #000000 @ 50%
        //  Interactive States
        disabled = AppColors.Neutral100.copy(alpha = 0.38f), // white @ 38%
        disabledContainer = AppColors.Neutral100.copy(alpha = 0.12f), // white @ 12%
        // Shadow
        shadow = AppColors.Neutral0, // #000000
    )
