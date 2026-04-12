package com.samuelokello.mwenyeji.ui.theme.elevation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevation system for Mwenyeji app
 * Provides consistent shadow/elevation values
 */
@Immutable
data class AppElevation(
    val level0: Dp,
    val level1: Dp,
    val level2: Dp,
    val level3: Dp,
    val level4: Dp,
    val level5: Dp,
)

val Elevation =
    AppElevation(
        level0 = 0.dp,
        level1 = 1.dp,
        level2 = 3.dp,
        level3 = 6.dp,
        level4 = 8.dp,
        level5 = 12.dp,
    )

/**
 * Semantic elevation helpers
 */
object ElevationTokens {
    val none: Dp = 0.dp
    val card: Dp = 1.dp
    val button: Dp = 0.dp
    val buttonPressed: Dp = 3.dp
    val fab: Dp = 6.dp
    val fabPressed: Dp = 8.dp
    val navigationBar: Dp = 3.dp
    val dialog: Dp = 6.dp
    val menu: Dp = 3.dp
    val modalBottomSheet: Dp = 1.dp
}
