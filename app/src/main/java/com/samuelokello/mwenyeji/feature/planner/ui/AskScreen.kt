package com.samuelokello.mwenyeji.feature.planner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.presentation.designsystem.components.MwenyejiHeaderBar
import com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.SnackBarManager
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** Chat with Mwenyeji: ask by typing or speaking, get trips back as messages with Start guide. */
@Composable
fun AskScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGuidance: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AskViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar: SnackBarManager = koinInject()
    val voiceUnavailable = stringResource(R.string.plan_voice_unavailable)
    val support =
        rememberPlannerScreenSupport(
            onGuidanceStarted = onNavigateToGuidance,
            onLocationGranted = { viewModel.onAction(AskAction.LocationGranted) },
        )
    val voice =
        rememberVoiceInput(
            prompt = stringResource(R.string.plan_listen_prompt),
            onText = { viewModel.onAction(AskAction.Spoken(it)) },
            onUnavailable = { snackbar.showError(voiceUnavailable) },
        )
    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size, state.isThinking) {
        listState.animateScrollToItem(state.messages.size)
    }

    val colors = MwenyejiTheme.colorScheme
    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = {
            MwenyejiHeaderBar(
                title = stringResource(R.string.plan_ask),
                subtitle = stringResource(R.string.plan_listen_prompt),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).imePadding()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(state.messages) { _, message ->
                    when (message) {
                        is ChatMessage.FromRider -> Bubble(fromRider = true) {
                            Text(text = message.text, style = MwenyejiTheme.typography.bodyLarge, color = colors.onPrimaryContainer)
                        }

                        is ChatMessage.FromMwenyeji -> Bubble(fromRider = false) {
                            val outcome = message.outcome
                            if (outcome == null) {
                                Text(
                                    text = stringResource(R.string.chat_welcome),
                                    style = MwenyejiTheme.typography.bodyLarge,
                                    color = colors.onSurface,
                                )
                            } else {
                                PlanOutcomeContent(outcome = outcome, onStart = support.start, onAllowLocation = support.askForLocation)
                            }
                        }
                    }
                }
                if (state.isThinking) item { Working() }
            }
            TripInputRow(
                text = state.text,
                onTextChange = { viewModel.onAction(AskAction.TextChanged(it)) },
                placeholder = stringResource(R.string.chat_placeholder),
                onSubmit = { viewModel.onAction(AskAction.Send) },
                voice = voice,
                modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

/** The rider's messages on the right, Mwenyeji's on the left and wider, since they carry trip cards. */
@Composable
private fun Bubble(
    fromRider: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = MwenyejiTheme.colorScheme
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (fromRider) Alignment.CenterEnd else Alignment.CenterStart) {
        Box(
            modifier =
                Modifier
                    .widthIn(max = if (fromRider) 300.dp else 360.dp)
                    .background(
                        if (fromRider) colors.primaryContainer else colors.surfaceContainer,
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (fromRider) 16.dp else 4.dp,
                            bottomEnd = if (fromRider) 4.dp else 16.dp,
                        ),
                    ).padding(12.dp),
        ) { content() }
    }
}
