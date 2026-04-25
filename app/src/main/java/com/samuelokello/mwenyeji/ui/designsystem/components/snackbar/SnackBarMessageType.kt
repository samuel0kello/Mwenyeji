package com.samuelokello.mwenyeji.ui.designsystem.components.snackbar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SnackBarMessageType { SUCCESS, ERROR, INFO }

data class SnackBarMessage(
    val message: String,
    val type: SnackBarMessageType = SnackBarMessageType.INFO,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)

class SnackBarManager {
    private val _currentMessage = MutableStateFlow<SnackBarMessage?>(null)
    val currentMessage: StateFlow<SnackBarMessage?> = _currentMessage.asStateFlow()

    fun showSuccess(message: String) {
        _currentMessage.value =
            SnackBarMessage(
                message = message,
                type = SnackBarMessageType.SUCCESS,
            )
    }

    fun showError(message: String, actionLabel: String? = "Dismiss", onAction: (() -> Unit)? = null) {
        _currentMessage.value =
            SnackBarMessage(
                message = message,
                type = SnackBarMessageType.ERROR,
                actionLabel = actionLabel,
                onAction = onAction,
            )
    }

    fun showInfo(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _currentMessage.value =
            SnackBarMessage(
                message = message,
                type = SnackBarMessageType.INFO,
                actionLabel = actionLabel,
                onAction = onAction,
            )
    }

    fun dismiss() {
        _currentMessage.value = null
    }
}
