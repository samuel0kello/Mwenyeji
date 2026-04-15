package com.samuelokello.mwenyeji.ui.designsystem.components.snackbar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SnackbarMessageType { SUCCESS, ERROR, INFO }

data class SnackbarMessage(
    val message: String,
    val type: SnackbarMessageType = SnackbarMessageType.INFO,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)

class SnackbarManager {
    private val _currentMessage = MutableStateFlow<SnackbarMessage?>(null)
    val currentMessage: StateFlow<SnackbarMessage?> = _currentMessage.asStateFlow()

    fun showSuccess(message: String) {
        _currentMessage.value =
            SnackbarMessage(
                message = message,
                type = SnackbarMessageType.SUCCESS,
            )
    }

    fun showError(message: String, actionLabel: String? = "Dismiss", onAction: (() -> Unit)? = null) {
        _currentMessage.value =
            SnackbarMessage(
                message = message,
                type = SnackbarMessageType.ERROR,
                actionLabel = actionLabel,
                onAction = onAction,
            )
    }

    fun showInfo(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _currentMessage.value =
            SnackbarMessage(
                message = message,
                type = SnackbarMessageType.INFO,
                actionLabel = actionLabel,
                onAction = onAction,
            )
    }

    fun dismiss() {
        _currentMessage.value = null
    }
}
