package com.samuelokello.mwenyeji.feature.contribute

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelokello.mwenyeji.feature.contribute.components.StepScaffold
import com.samuelokello.mwenyeji.feature.contribute.step.InstructionsStepScreen
import com.samuelokello.mwenyeji.feature.contribute.step.RouteStepScreen
import com.samuelokello.mwenyeji.feature.contribute.step.TimingStepScreen
import com.samuelokello.mwenyeji.feature.contribute.step.WarningsStepScreen
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.SnackbarManager
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Parent screen for the 4-step guide contribution flow.
 *
 * Owns the [ContributeViewModel] and distributes state + intent handler
 * down to each step screen. Navigation between steps is handled internally
 * via [ContributeIntent.NextStep] / [ContributeIntent.PreviousStep] —
 * no NavController involvement needed between steps.
 *
 * External navigation (back to feed, success screen) is handled via
 * [ContributeEffect] collected here and forwarded to the caller.
 */
@Composable
fun ContributeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContributeViewModel = koinViewModel(),
    snackBarManager: SnackbarManager = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Collect one-time effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ContributeEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is ContributeEffect.NavigateToSuccess -> {
                    onNavigateToSuccess()
                }

                is ContributeEffect.ShowError -> {
                    snackBarManager.showError(
                        message = effect.message,
                        actionLabel = "Dismiss",
                        onAction = { snackBarManager.dismiss() },
                    )
                }

                is ContributeEffect.ShowFieldError -> { // handled per-step via state.errors
                }
            }
        }
    }

    ContributeScreenContent(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
fun ContributeScreenContent(state: ContributeState, onAction: (ContributeActions) -> Unit, modifier: Modifier = Modifier) {
    StepScaffold(
        state = state,
        onAction = onAction,
        buttonLabel = if (state.isLastStep) "Share guide" else "Continue",
    ) {
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            },
            label = "contributeStepAnimation",
            modifier = modifier.fillMaxSize(),
        ) { step ->
            when (step) {
                ContributeStep.ROUTE -> {
                    RouteStepScreen(
                        state = state,
                        onAction = onAction,
                    )
                }

                ContributeStep.TIMING -> {
                    TimingStepScreen(
                        state = state,
                        onAction = onAction,
                    )
                }

                ContributeStep.INSTRUCTIONS -> {
                    InstructionsStepScreen(
                        state = state,
                        onAction = onAction,
                    )
                }

                ContributeStep.WARNINGS -> {
                    WarningsStepScreen(
                        state = state,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}
