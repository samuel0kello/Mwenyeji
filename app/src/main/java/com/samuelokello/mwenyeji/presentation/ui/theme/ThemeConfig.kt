package com.samuelokello.mwenyeji.presentation.ui.theme

/**
 * Theme configuration constants
 */
object ThemeConfig {
    /**
     * Whether to enable dynamic color theming (Material You)
     * Only works on Android 12+
     */
    const val ENABLE_DYNAMIC_COLOR = false

    /**
     * Default theme mode
     */
    enum class ThemeMode {
        LIGHT,
        DARK,
        SYSTEM,
    }

    /**
     * App theme preferences
     */
    data class ThemePreferences(
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        val useDynamicColor: Boolean = ENABLE_DYNAMIC_COLOR,
    )
}
