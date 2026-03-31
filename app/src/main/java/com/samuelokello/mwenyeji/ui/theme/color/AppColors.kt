package com.samuelokello.mwenyeji.ui.theme.color

import androidx.compose.ui.graphics.Color

/**
 * Raw color palette — extracted directly from Mwenyeji Design System v1.0 (Figma).
 *
 * These are the seed/palette values. Do NOT use these directly in composables.
 * Use [LightColorScheme], [DarkColorScheme], or MwenyejiTheme.colorScheme instead.
 *
 * Naming convention: <Hue><Tonal step>
 *   - 0   = black
 *   - 10  = darkest useful tone
 *   - 100 = white
 *
 * Green tonal palette (brand)
 */
object AppColors {

    // ── Green (brand / primary) ─────────────────────────────────────────────
    val Green0   = Color(0xFF000000)
    val Green10  = Color(0xFF002111)   // deepest — primaryDark / primaryContainer dark
    val Green20  = Color(0xFF003920)   // onPrimary dark
    val Green25  = Color(0xFF00452A)
    val Green30  = Color(0xFF1D4B35)   // primaryDark from Figma
    val Green40  = Color(0xFF2D6B4D)   // Primary — #2D6B4D (Figma brand primary)
    val Green50  = Color(0xFF3D8B5D)   // Primary Light — hover states
    val Green60  = Color(0xFF4CAF78)   // bright accent — used on dark surfaces
    val Green70  = Color(0xFF6DC496)
    val Green80  = Color(0xFF9BD4B1)   // primary dark-theme role
    val Green90  = Color(0xFFB7F0CC)   // primaryContainer dark / onPrimaryContainer light
    val Green95  = Color(0xFFDDF7EA)
    val Green98  = Color(0xFFF0FBF5)
    val Green99  = Color(0xFFF8FDF9)
    val Green100 = Color(0xFFFFFFFF)

    // ── Green-Gray (secondary / neutral-variant) ────────────────────────────
    val GreenGray10  = Color(0xFF0D1F13)
    val GreenGray20  = Color(0xFF223527)
    val GreenGray30  = Color(0xFF384B3C)
    val GreenGray40  = Color(0xFF4F6354)
    val GreenGray50  = Color(0xFF667A6B)
    val GreenGray60  = Color(0xFF8C9B8F)   // outline light-theme
    val GreenGray70  = Color(0xFFB6CCB9)   // secondary dark-theme
    val GreenGray80  = Color(0xFFD2E8D5)   // secondaryContainer dark / onSecondaryContainer light
    val GreenGray90  = Color(0xFFE8F5EB)
    val GreenGray95  = Color(0xFFF4FAF5)
    val GreenGray99  = Color(0xFFF6FAF7)   // background light-theme
    val GreenGray100 = Color(0xFFFFFFFF)

    // ── Blue (tertiary / info / matatu tag) ─────────────────────────────────
    val Blue10  = Color(0xFF003258)
    val Blue20  = Color(0xFF00497D)
    val Blue30  = Color(0xFF01579B)
    val Blue40  = Color(0xFF2196F3)   // Info — exact from Figma
    val Blue60  = Color(0xFF42A5F5)
    val Blue80  = Color(0xFF90CAF9)   // info dark-theme
    val Blue90  = Color(0xFFBBDEFB)
    val Blue100 = Color(0xFFFFFFFF)

    // ── Neutral (surface / background tones) ────────────────────────────────
    val Neutral0   = Color(0xFF000000)
    val Neutral4   = Color(0xFF0A0F0C)   // surfaceContainerLowest dark
    val Neutral6   = Color(0xFF0E1210)   // background dark — Figma #121212 green-tinted
    val Neutral10  = Color(0xFF121212)   // pure Figma dark background
    val Neutral12  = Color(0xFF161A17)   // surface dark
    val Neutral17  = Color(0xFF1C2119)   // surfaceContainer dark
    val Neutral20  = Color(0xFF1E1E1E)   // Figma Surface #1E1E1E
    val Neutral22  = Color(0xFF252B22)   // surfaceContainerHigh dark
    val Neutral24  = Color(0xFF2A2A2A)   // Figma Surface Raised #2A2A2A
    val Neutral30  = Color(0xFF303530)   // surfaceContainerHighest dark
    val Neutral35  = Color(0xFF333333)   // Figma Border #333333
    val Neutral40  = Color(0xFF383A35)   // surfaceBright dark
    val Neutral50  = Color(0xFF424940)   // surfaceVariant dark / outlineVariant
    val Neutral60  = Color(0xFF666666)   // Figma text muted
    val Neutral70  = Color(0xFF8C9388)   // outline dark
    val Neutral80  = Color(0xFF9E9E9E)   // Figma text secondary
    val Neutral87  = Color(0xFFDEE5DA)   // surfaceVariant light
    val Neutral90  = Color(0xFFE5E6E0)   // onBackground / onSurface dark
    val Neutral92  = Color(0xFFECEDE7)
    val Neutral94  = Color(0xFFF0F5F1)   // surfaceContainerLow light
    val Neutral95  = Color(0xFFF6FAF7)   // background light (green-tinted white)
    val Neutral96  = Color(0xFFF8F9F4)
    val Neutral98  = Color(0xFFFCFDF7)
    val Neutral99  = Color(0xFFFEFEF9)
    val Neutral100 = Color(0xFFFFFFFF)   // surface light

    // ── Neutral-Variant (for secondary surface roles) ───────────────────────
    val NeutralVariant30 = Color(0xFF3F4942)
    val NeutralVariant40 = Color(0xFF565E59)
    val NeutralVariant50 = Color(0xFF6E7770)
    val NeutralVariant60 = Color(0xFF8C9B8F)   // outlineVariant light
    val NeutralVariant70 = Color(0xFFA8B5AA)
    val NeutralVariant80 = Color(0xFFC2C9BE)   // outlineVariant / onSurfaceVariant dark
    val NeutralVariant90 = Color(0xFFDEE5DA)   // surfaceVariant light
    val NeutralVariant95 = Color(0xFFECF3ED)
    val NeutralVariant99 = Color(0xFFF6FAF7)

    // ── Amber (warning / boda tag) ───────────────────────────────────────────
    // Figma semantic: Warning / Boda = #FF9800
    val Amber10  = Color(0xFF4A2800)
    val Amber20  = Color(0xFF6A3C00)
    val Amber30  = Color(0xFFE65100)
    val Amber40  = Color(0xFFFF9800)   // Warning — exact from Figma
    val Amber60  = Color(0xFFFFB74D)   // warning dark-theme
    val Amber80  = Color(0xFFFFCC80)
    val Amber90  = Color(0xFFFFE0B2)
    val Amber100 = Color(0xFFFFFFFF)

    // ── Red (error) ───────────────────────────────────────────────────────────
    // Figma semantic: Error = #F44336
    val Red10  = Color(0xFF410002)
    val Red20  = Color(0xFF690005)
    val Red30  = Color(0xFF93000A)
    val Red40  = Color(0xFFBA1A1A)   // error light-theme
    val Red60  = Color(0xFFEF5350)
    val Red80  = Color(0xFFFFB4AB)   // error dark-theme
    val Red90  = Color(0xFFFFDAD6)
    val Red100 = Color(0xFFFFFFFF)

    // ── Success
    val SuccessGreen10  = Color(0xFF1B5E20)
    val SuccessGreen20  = Color(0xFF003909)
    val SuccessGreen30  = Color(0xFF00530F)
    val SuccessGreen40  = Color(0xFF4CAF50)
    val SuccessGreen60  = Color(0xFF66BB6A)
    val SuccessGreen80  = Color(0xFF81C784)   // success dark-theme
    val SuccessGreen90  = Color(0xFFC8E6C9)
    val SuccessGreen95  = Color(0xFFE8F5E9)
    val SuccessGreen100 = Color(0xFFFFFFFF)
}