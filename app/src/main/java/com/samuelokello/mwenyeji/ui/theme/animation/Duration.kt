package com.samuelokello.mwenyeji.ui.theme.animation

/**
 * Animation duration constants for Mwenyeji app
 * All durations in milliseconds
 */
object Duration {
    // Quick animations for small UI changes
    const val instant = 0
    const val fast = 100
    const val quick = 150
    
    // Standard animations for most UI transitions
    const val short = 200
    const val medium = 300
    const val normal = 400
    
    // Longer animations for complex transitions
    const val long = 500
    const val slow = 700
    const val verySlow = 1000
    
    // Specific use cases
    const val ripple = 200
    const val buttonPress = 100
    const val pageTransition = 300
    const val dialogEnter = 300
    const val dialogExit = 200
    const val bottomSheetEnter = 300
    const val bottomSheetExit = 200
    const val fadeIn = 150
    const val fadeOut = 75
}