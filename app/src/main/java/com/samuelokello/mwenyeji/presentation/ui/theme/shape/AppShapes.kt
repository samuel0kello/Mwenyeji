package com.samuelokello.mwenyeji.presentation.ui.theme.shape

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
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
    val extraSmall: CornerBasedShape,
    val small: CornerBasedShape,
    val medium: CornerBasedShape,
    val large: CornerBasedShape,
    val extraLarge: CornerBasedShape,
    val full: Shape,
)

/**
 * Default shape configuration
 */
val Shapes =
    AppShapes(
        none = RoundedCornerShape(0.dp),
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
        full = RoundedCornerShape(50),
    )

fun AppShapes.toMaterialShapes(): Shapes =
    Shapes(
        extraSmall = extraSmall,
        small = small,
        medium = medium,
        large = large,
        extraLarge = extraLarge,
    )
