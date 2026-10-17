package com.samuelokello.mwenyeji.feature.guidance.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.core.map.MwenyejiMapStyle
import com.samuelokello.mwenyeji.feature.guidance.service.GuidanceSnapshot
import com.samuelokello.mwenyeji.feature.guidance.service.TripGuidanceService
import com.samuelokello.mwenyeji.feature.guidance.service.guidanceStatus
import com.samuelokello.mwenyeji.feature.guidance.session.TripPhase
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripPlan
import com.samuelokello.mwenyeji.presentation.designsystem.components.button.MwenyejiButton
import com.samuelokello.mwenyeji.presentation.designsystem.components.button.MwenyejiOutlinedButton
import com.samuelokello.mwenyeji.presentation.designsystem.components.card.MwenyejiCard
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import kotlinx.coroutines.delay

/**
 * The map view of a running trip: the route, the stage and the stop, the rider's position, the next
 * step, and the taps the trip needs. Guidance lives in the service, so leaving this screen does not
 * stop it. The screen closes itself when the trip ends.
 */
@Composable
fun GuidanceScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val trip by TripGuidanceService.trip.collectAsStateWithLifecycle()
    var sawTrip by remember { mutableStateOf(false) }

    LaunchedEffect(trip) {
        when {
            trip != null -> sawTrip = true

            sawTrip -> onNavigateBack()

            else -> {
                // The service never started, for example because permission was revoked.
                delay(START_TIMEOUT_MS)
                if (!sawTrip) onNavigateBack()
            }
        }
    }

    GuidanceContent(
        trip = trip,
        onBack = onNavigateBack,
        onBoard = { TripGuidanceService.onBoard(context) },
        onAlighted = { TripGuidanceService.alighted(context) },
        onStop = { TripGuidanceService.stop(context) },
        modifier = modifier,
    )
}

@Composable
private fun GuidanceContent(
    trip: GuidanceSnapshot?,
    onBack: () -> Unit,
    onBoard: () -> Unit,
    onAlighted: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val context = LocalContext.current
    val phase = trip?.phase ?: TripPhase.PLANNED
    val plan = trip?.plan

    Box(modifier = modifier.fillMaxSize()) {
        GuidanceMap(plan = plan, modifier = Modifier.fillMaxSize())

        // Next step, at the top where a glance finds it.
        MwenyejiCard(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            border = BorderStroke(1.dp, colors.border),
            elevation = MwenyejiTheme.elevation.level2,
            containerColor = colors.surfaceContainerHigh,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_outline_arrow_back),
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = guidanceStatus(context, plan, phase),
                        style = MwenyejiTheme.typography.titleLarge,
                        color = if (phase == TripPhase.ALIGHT_NOW || phase == TripPhase.MISSED_STOP) colors.primary else colors.onSurface,
                    )
                    if (plan != null && (phase.isRiding || phase == TripPhase.AT_STAGE)) {
                        Text(
                            text = stringResource(R.string.guidance_stops_left, plan.stopsToRide, plan.alightStop.name),
                            style = MwenyejiTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // The taps the trip needs right now, within thumb reach.
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = MwenyejiMapStyle.ATTRIBUTION,
                style = MwenyejiTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier
                    .background(colors.surface.copy(alpha = 0.8f), MwenyejiTheme.shapes.small)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
            when {
                phase == TripPhase.AT_STAGE ->
                    MwenyejiButton(
                        text = stringResource(R.string.guidance_action_on_board),
                        onClick = onBoard,
                        modifier = Modifier.fillMaxWidth(),
                    )

                phase.isRiding || phase == TripPhase.MISSED_STOP ->
                    MwenyejiButton(
                        text = stringResource(R.string.guidance_action_alighted),
                        onClick = onAlighted,
                        modifier = Modifier.fillMaxWidth(),
                    )
            }
            if (!phase.isFinal) {
                MwenyejiOutlinedButton(
                    text = stringResource(R.string.guidance_action_stop),
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private const val START_TIMEOUT_MS = 15_000L
