package com.samuelokello.mwenyeji.feature.planner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.presentation.designsystem.components.AppBarAction
import com.samuelokello.mwenyeji.presentation.designsystem.components.MwenyejiHeaderBar
import com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.SnackBarManager
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * "Where to?": type or say a destination, get the matatus that go there, start one.
 *
 * @param listenOnOpen Open the speech recogniser straight away (the feed's mic button).
 */
@Composable
fun PlanTripScreen(
    listenOnOpen: Boolean,
    onNavigateBack: () -> Unit,
    onOpenChat: () -> Unit,
    onNavigateToGuidance: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlanTripViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar: SnackBarManager = koinInject()
    val voiceUnavailable = stringResource(R.string.plan_voice_unavailable)
    val support =
        rememberPlannerScreenSupport(
            onGuidanceStarted = onNavigateToGuidance,
            onLocationGranted = { viewModel.onAction(PlanTripAction.LocationGranted) },
        )
    val voice =
        rememberVoiceInput(
            prompt = stringResource(R.string.plan_listen_prompt),
            onText = { viewModel.onAction(PlanTripAction.Spoken(it)) },
            onUnavailable = { snackbar.showError(voiceUnavailable) },
        )

    LaunchedEffect(Unit) {
        if (listenOnOpen && voice.available) voice.listen()
    }

    val colors = MwenyejiTheme.colorScheme
    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = {
            MwenyejiHeaderBar(
                title = stringResource(R.string.plan_where_to),
                subtitle = stringResource(R.string.plan_listen_prompt),
                onNavigateBack = onNavigateBack,
                actions =
                    listOf(
                        AppBarAction(
                            icon = ImageVector.vectorResource(R.drawable.ic_outline_chat),
                            contentDescription = stringResource(R.string.plan_ask),
                            onClick = onOpenChat,
                        ),
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TripInputRow(
                text = state.text,
                onTextChange = { viewModel.onAction(PlanTripAction.TextChanged(it)) },
                placeholder = stringResource(R.string.plan_placeholder),
                onSubmit = { viewModel.onAction(PlanTripAction.Submit) },
                voice = voice,
            )
            when {
                state.isPlanning -> Working()

                else -> state.outcome?.let { outcome ->
                    PlanOutcomeContent(outcome = outcome, onStart = support.start, onAllowLocation = support.askForLocation)
                }
            }
        }
    }
}

@Composable
internal fun Working(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MwenyejiTheme.colorScheme.primary)
        Text(
            text = stringResource(R.string.plan_working),
            style = MwenyejiTheme.typography.bodyMedium,
            color = MwenyejiTheme.colorScheme.onSurfaceVariant,
        )
    }
}
