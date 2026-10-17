package com.samuelokello.mwenyeji.feature.guidance.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import com.samuelokello.mwenyeji.feature.guidance.service.TripGuidanceService
import com.samuelokello.mwenyeji.feature.guidance.service.TripRequest
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Starting a guided trip from any screen: asks once about sharing ride traces, asks for precise
 * location (and notifications on Android 13+), then starts the service and calls [onStarted].
 *
 * @param onDenied Called when precise location was refused; guidance cannot run without it.
 * @return The function to call with the trip to start.
 */
@Composable
fun rememberStartGuidance(
    onStarted: () -> Unit,
    onDenied: () -> Unit,
): (TripRequest) -> Unit {
    val context = LocalContext.current
    val prefs: MwenyejiPrefs = koinInject()
    val scope = rememberCoroutineScope()
    val started by rememberUpdatedState(onStarted)
    val denied by rememberUpdatedState(onDenied)
    var waitingForConsent by remember { mutableStateOf<TripRequest?>(null) }
    var waitingForPermission by remember { mutableStateOf<TripRequest?>(null) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val request = waitingForPermission
            waitingForPermission = null
            // Notifications are optional: guidance still speaks and vibrates without them.
            if (results[Manifest.permission.ACCESS_FINE_LOCATION] == true && request != null) {
                TripGuidanceService.start(context, request)
                started()
            } else {
                denied()
            }
        }

    fun startWithPermission(request: TripRequest) {
        val granted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            TripGuidanceService.start(context, request)
            started()
        } else {
            waitingForPermission = request
            permissionLauncher.launch(guidancePermissions())
        }
    }

    waitingForConsent?.let { request ->
        TraceConsentDialog(
            onAnswer = { share ->
                waitingForConsent = null
                scope.launch {
                    prefs.setShareRideTraces(share)
                    startWithPermission(request)
                }
            },
            onDismiss = { waitingForConsent = null },
        )
    }

    return { request ->
        scope.launch {
            // Asked once, the first time any trip is started; the answer is kept from then on.
            if (prefs.getShareRideTraces().first() == null) waitingForConsent = request else startWithPermission(request)
        }
    }
}

/** What guidance asks for: precise location, and on Android 13+ notifications for the next step. */
fun guidancePermissions(): Array<String> =
    buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

/**
 * Asked once, before the first guided trip: may Mwenyeji learn from the path the matatu takes?
 * Closing it without answering starts nothing, and asks again next time.
 */
@Composable
fun TraceConsentDialog(
    onAnswer: (share: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MwenyejiTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(R.string.trace_consent_title),
                style = MwenyejiTheme.typography.titleLarge,
                color = colors.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.trace_consent_body),
                style = MwenyejiTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = { onAnswer(true) }) {
                Text(stringResource(R.string.trace_consent_allow), color = colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = { onAnswer(false) }) {
                Text(stringResource(R.string.trace_consent_decline), color = colors.primary)
            }
        },
    )
}
