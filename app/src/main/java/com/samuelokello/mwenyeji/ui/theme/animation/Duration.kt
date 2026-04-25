package com.samuelokello.mwenyeji.ui.theme.animation

/**
 * Animation duration constants for Mwenyeji app
 * All durations in milliseconds
 */
object Duration {
    // Quick animations for small UI changes
    const val INSTANT = 0
    const val FAST = 100
    const val QUICK = 150

    // Standard animations for most UI transitions
    const val SHORT = 200
    const val MEDIUM = 300
    const val NORMAL = 400

    // Longer animations for complex transitions
    const val LONG = 600
    const val SLOW = 800
    const val VERY_SLOW = 1000

    // Specific use cases
    const val RIPPLE = 200
    const val BUTTONPRESS = 100
    const val PAGE_TRANSITION = 300
    const val DIALOG_ENTER = 300
    const val DIALOG_EXIT = 200
    const val BOTTOM_SHEET_ENTER = 300
    const val BOTTOM_SHEET_EXIT = 200
    const val FADE_IN = 150
    const val FADE_OUT = 75
}
