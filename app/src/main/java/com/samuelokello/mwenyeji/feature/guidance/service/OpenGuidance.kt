package com.samuelokello.mwenyeji.feature.guidance.service

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The guidance notification's tap. It reopens the app with [EXTRA] set; the activity calls [request],
 * and the navigation host shows the guidance screen once it is ready, then calls [consume].
 */
object OpenGuidance {
    internal const val EXTRA = "com.samuelokello.mwenyeji.guidance.OPEN"

    private val _requested = MutableStateFlow(false)
    val requested: StateFlow<Boolean> = _requested.asStateFlow()

    /** Call from the activity with every intent it receives. */
    fun handle(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA, false) == true) {
            intent.removeExtra(EXTRA)
            _requested.value = true
        }
    }

    fun consume() {
        _requested.value = false
    }
}
