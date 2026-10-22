package com.samuelokello.mwenyeji.feature.planner.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.feature.guidance.ui.rememberStartGuidance
import com.samuelokello.mwenyeji.feature.planner.domain.TripOption
import com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.SnackBarManager
import org.koin.compose.koinInject

/** What both planning screens need from the platform. */
class PlannerScreenSupport(
    /** Starts guidance for a trip option (asking for consent and permission as needed). */
    val start: (TripOption) -> Unit,
    /** Asks for location, for planning "from here". */
    val askForLocation: () -> Unit,
)

@Composable
fun rememberPlannerScreenSupport(
    onGuidanceStarted: () -> Unit,
    onLocationGranted: () -> Unit,
): PlannerScreenSupport {
    val snackbar: SnackBarManager = koinInject()
    val needsPreciseLocation = stringResource(R.string.guidance_needs_precise_location)
    val defaultVehicle = stringResource(R.string.guidance_vehicle_default)
    val routeVehicle = stringResource(R.string.guidance_vehicle_route, "%s")
    val granted by rememberUpdatedState(onLocationGranted)

    val startGuidance =
        rememberStartGuidance(
            onStarted = onGuidanceStarted,
            onDenied = { snackbar.showError(needsPreciseLocation) },
        )
    val locationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results.values.any { it }) granted()
        }

    return PlannerScreenSupport(
        start = { option ->
            // Spoken as "Look for … to <stop>" when no guide names a sacco.
            val vehicle = option.route.routeNumber?.let { routeVehicle.replace("%s", it) } ?: defaultVehicle
            startGuidance(option.toTripRequest(vehicle))
        },
        askForLocation = {
            locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        },
    )
}
