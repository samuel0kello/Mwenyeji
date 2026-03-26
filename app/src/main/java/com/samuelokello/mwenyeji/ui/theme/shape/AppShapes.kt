package com.samuelokello.mwenyeji.ui.theme.shape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Shape system for Mwenyeji app
 * Provides consistent corner radius values
 */
@Immutable
data class AppShapes(
    val none: Shape,
    val extraSmall: Shape,
    val small: Shape,
    val medium: Shape,
    val large: Shape,
    val extraLarge: Shape,
    val full: Shape,
)

/**
 * Default shape configuration
 */
val Shapes = AppShapes(
    none = RoundedCornerShape(0.dp),
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
    full = RoundedCornerShape(50), // Percentage-based for pills
)