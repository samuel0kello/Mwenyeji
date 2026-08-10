package com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar

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

interface SnackBarManager {
    val currentMessage: StateFlow<SnackBarMessage?>

    fun showSuccess(message: String)

    fun showError(
        message: String,
        actionLabel: String? = "Dismiss",
        onAction: (() -> Unit)? = null,
    )

    fun showInfo(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
    )

    fun dismiss()
}

class SnackBarManagerImpl : SnackBarManager {
    private val _currentMessage = MutableStateFlow<SnackBarMessage?>(null)
    override val currentMessage: StateFlow<SnackBarMessage?> = _currentMessage.asStateFlow()

    override fun showSuccess(message: String) {
        _currentMessage.value =
            SnackBarMessage(
                message = message,
                type = SnackBarMessageType.SUCCESS,
            )
    }

    override fun showError(
        message: String,
        actionLabel: String?,
        onAction: (() -> Unit)?,
    ) {
        _currentMessage.value =
            SnackBarMessage(
                message = message,
                type = SnackBarMessageType.ERROR,
                actionLabel = actionLabel,
                onAction = onAction,
            )
    }

    override fun showInfo(
        message: String,
        actionLabel: String?,
        onAction: (() -> Unit)?,
    ) {
        _currentMessage.value =
            SnackBarMessage(
                message = message,
                type = SnackBarMessageType.INFO,
                actionLabel = actionLabel,
                onAction = onAction,
            )
    }

    override fun dismiss() {
        _currentMessage.value = null
    }
}
