package com.samuelokello.mwenyeji.feature.contribute.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.feature.contribute.ContributeActions
import com.samuelokello.mwenyeji.feature.contribute.ContributeState
import com.samuelokello.mwenyeji.feature.contribute.ContributeStep
import com.samuelokello.mwenyeji.ui.designsystem.components.MwenyejiStepBar

/**
 * Shared scaffold layout used by all 4 contribute step screens.
 *
 * Provides:
 *  - [MwenyejiStepBar] header with progress + back navigation
 *  - Scrollable content area via [content] slot
 *  - Pinned Continue / Submit button at the bottom
 *  - Keyboard-aware padding via [imePadding]
 *
 * Each step screen only needs to fill the [content] slot with
 * its own fields — no need to repeat the header/button boilerplate.
 *
 * @param state         Shared contribute state for header info.
 * @param onIntent      Shared intent handler.
 * @param buttonLabel   Label on the primary CTA button.
 * @param content       Step-specific form fields.
 */
@Composable
fun StepScaffold(
    state: ContributeState,
    onIntent: (ContributeActions) -> Unit,
    modifier: Modifier = Modifier,
    buttonLabel: String = "Continue",
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .safeContentPadding()
                .imePadding(),
    ) {
        // Step header — progress bar + title
        MwenyejiStepBar(
            stepLabel = state.stepLabel,
            title = state.stepTitle,
            currentStep = state.currentStep + 1, // 1-based for display
            totalSteps = ContributeStep.TOTAL,
            onNavigateBack = { onIntent(ContributeActions.PreviousStep) },
        )

        // Scrollable form content
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content,
        )

        // Pinned CTA button
//        MwenyejiButton(
//            text = buttonLabel,
//            onClick = { onAction(ContributeActions.NextStep) },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//        )
    }
}

@Composable
fun StepContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}
