package com.samuelokello.mwenyeji.feature.contribute

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.core.DragIndication
import com.composables.core.ModalBottomSheet
import com.composables.core.Scrim
import com.composables.core.Sheet
import com.composables.core.SheetDetent.Companion.FullyExpanded
import com.composables.core.SheetDetent.Companion.Hidden
import com.composables.core.rememberModalBottomSheetState
import com.samuelokello.mwenyeji.feature.contribute.step.InstructionsStepScreen
import com.samuelokello.mwenyeji.feature.contribute.step.RouteStepScreen
import com.samuelokello.mwenyeji.feature.contribute.step.TimingStepScreen
import com.samuelokello.mwenyeji.feature.contribute.step.WarningsStepScreen
import com.samuelokello.mwenyeji.ui.designsystem.components.button.MwenyejiButton
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ContributeSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onNavigateToSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContributeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MwenyejiTheme.colorScheme

    // Always start Hidden — we animate to FullyExpanded reactively
    val sheetState =
        rememberModalBottomSheetState(
            initialDetent = Hidden,
        )

    // Drive the sheet purely from `visible` — no LaunchedEffect needed
    sheetState.currentDetent = if (visible) FullyExpanded else Hidden

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ContributeEffect.NavigateBack -> {
                    onDismiss()
                }

                is ContributeEffect.NavigateToSuccess -> {
                    onNavigateToSuccess()
                }

                else -> {}
            }
        }
    }

    ModalBottomSheet(
        state = sheetState,
        onDismiss = onDismiss,
    ) {
        Scrim()

        Box(
            Modifier.padding(
                WindowInsets.navigationBars
                    .only(WindowInsetsSides.Horizontal)
                    .asPaddingValues(),
            ),
        ) {
            Sheet(
                modifier =
                    modifier
                        .shadow(4.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(colors.surface)
                        .fillMaxWidth(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding(),
                ) {
                    // drag handle
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        DragIndication(
                            modifier =
                                Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(colors.outlineVariant),
                        )
                    }

                    SheetStepHeader(state = state)

                    AnimatedContent(
                        targetState = state.currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                            } else {
                                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                            }
                        },
                        label = "stepAnimation",
                        modifier =
                            Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                    ) { step ->
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            when (step) {
                                ContributeStep.ROUTE -> {
                                    RouteStepScreen(
                                        state = state,
                                        onAction = viewModel::onAction,
                                    )
                                }

                                ContributeStep.TIMING -> {
                                    TimingStepScreen(
                                        state = state,
                                        onAction = viewModel::onAction,
                                    )
                                }

                                ContributeStep.INSTRUCTIONS -> {
                                    InstructionsStepScreen(
                                        state = state,
                                        onAction = viewModel::onAction,
                                    )
                                }

                                ContributeStep.WARNINGS -> {
                                    WarningsStepScreen(
                                        state = state,
                                        onAction = viewModel::onAction,
                                    )
                                }
                            }
                        }
                    }

                    MwenyejiButton(
                        text = if (state.isLastStep) "Submit guide →" else "Continue",
                        onClick = { viewModel.onAction(ContributeActions.NextStep) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetStepHeader(state: ContributeState) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = state.stepLabel,
            style = typography.labelSmall,
            color = colors.primary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(ContributeStep.TOTAL) { index ->
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (index <= state.currentStep) {
                                    colors.primary
                                } else {
                                    colors.outlineVariant
                                },
                            ),
                )
            }
        }
        Text(
            text = state.stepTitle,
            style = typography.headlineSmall,
            color = colors.onSurface,
        )
        Text(
            text = state.stepSubtitle,
            style = typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
    }
}
