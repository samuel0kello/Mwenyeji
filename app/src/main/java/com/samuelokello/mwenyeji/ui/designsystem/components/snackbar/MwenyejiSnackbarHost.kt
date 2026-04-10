package com.samuelokello.mwenyeji.ui.designsystem.components.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import kotlinx.coroutines.delay

/**
 * Hosts the snackbar — place this at the bottom of your App Scaffold content.
 * Observes [SnackbarManager] and shows/hides automatically.
 * Auto-dismisses after [autoDismissMs] unless it has an action button.
 */
@Composable
fun MwenyejiSnackbarHost(
    manager: SnackbarManager,
    modifier: Modifier = Modifier,
    autoDismissMs: Long = 3000L,
) {
    val message by manager.currentMessage.collectAsStateWithLifecycle()

    LaunchedEffect(message) {
        if (message != null && message?.actionLabel == null) {
            delay(autoDismissMs)
            manager.dismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier,
    ) {
        message?.let { msg ->
            MwenyejiSnackbar(
                message = msg,
                onAction = {
                    msg.onAction?.invoke()
                    manager.dismiss()
                },
                onDismiss = { manager.dismiss() },
            )
        }
    }
}

@Composable
private fun MwenyejiSnackbar(
    message: SnackbarMessage,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    val (containerColor, contentColor, icon) = when (message.type) {
        SnackbarMessageType.SUCCESS -> Triple(
            colors.success,
            colors.onSuccess,
            Icons.Outlined.Check,
        )

        SnackbarMessageType.ERROR -> Triple(
            colors.error.copy(alpha = 0.15f),
            colors.error,
            Icons.Outlined.ErrorOutline,
        )

        SnackbarMessageType.INFO -> Triple(
            colors.info,
            colors.onInfoContainer,
            Icons.Outlined.Info,
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Type icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )

            // Message text
            Text(
                text = message.message,
                style = typography.bodySmall,
                color = contentColor,
                modifier = Modifier.weight(1f),
            )

            // Action button (Dismiss / Retry etc.)
            if (message.actionLabel != null) {
                TextButton(onClick = onAction) {
                    Text(
                        text = message.actionLabel,
                        style = typography.labelMedium,
                        color = contentColor,
                    )
                }
            }
        }
    }
}