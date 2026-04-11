package com.samuelokello.mwenyeji.ui.theme.typography

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.samuelokello.mwenyeji.R

val poppins: FontFamily = FontFamily(
    Font(R.font.poppins_thin, weight = FontWeight.Thin),
    Font(R.font.poppins_extralight, weight = FontWeight.ExtraLight),
    Font(R.font.poppins_extralight_italic, weight = FontWeight.ExtraLight, style = FontStyle.Italic),
    Font(R.font.poppins_light, weight = FontWeight.Light),
    Font(R.font.poppins_light_italic, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(R.font.poppins_regular, weight = FontWeight.Normal),
    Font(R.font.poppins_medium, weight = FontWeight.Medium),
    Font(R.font.poppins_medium_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.poppins_semibold, weight = FontWeight.SemiBold),
    Font(R.font.poppins_semibold_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    Font(R.font.poppins_bold, weight = FontWeight.Bold),
    Font(R.font.poppins_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(R.font.poppins_extrabold, weight = FontWeight.ExtraBold),
    Font(R.font.poppins_extrabold_italic, weight = FontWeight.ExtraBold, style = FontStyle.Italic),
    Font(R.font.poppins_black, weight = FontWeight.Black),
    Font(R.font.poppins_black_italic, weight = FontWeight.Black, style = FontStyle.Italic),
)
val SyneFamily = FontFamily(
    Font(R.font.syne_extrabold, FontWeight.Normal),
    Font(R.font.syne_bold, FontWeight.Bold),
    Font(R.font.syne_extrabold, FontWeight.ExtraBold),
)

val DmSansFamily = FontFamily(
    Font(R.font.dm_sans, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
)

val JetBrainsFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
)


/**
 * Default font family - uses system default
 * Replace with your custom font family when ready
 */
val AppFontFamily: FontFamily = poppins
