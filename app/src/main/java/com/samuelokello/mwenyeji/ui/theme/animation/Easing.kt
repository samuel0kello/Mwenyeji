package com.samuelokello.mwenyeji.ui.theme.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * Material Design easing curves for animations
 */
object Easing {
    /**
     * Standard easing - most common, for normal motion
     */
    val standard: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    
    /**
     * Emphasized easing - for more pronounced motion
     */
    val emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    
    /**
     * Decelerated easing - for elements entering the screen
     */
    val decelerated: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    
    /**
     * Accelerated easing - for elements leaving the screen
     */
    val accelerated: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    
    /**
     * Linear easing - constant speed
     */
    val linear: Easing = CubicBezierEasing(0.0f, 0.0f, 1.0f, 1.0f)
    
    /**
     * Legacy easings for backwards compatibility
     */
    val legacyEaseInOut: Easing = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)
    val legacyEaseIn: Easing = CubicBezierEasing(0.42f, 0.0f, 1.0f, 1.0f)
    val legacyEaseOut: Easing = CubicBezierEasing(0.0f, 0.0f, 0.58f, 1.0f)
}