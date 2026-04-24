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
import com.samuelokello.mwenyeji.ui.designsystem.components.button.MwenyejiButton

@Composable
fun StepScaffold(
    state: ContributeState,
    onAction: (ContributeActions) -> Unit,
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
            currentStep = state.currentStep + 1,
            totalSteps = ContributeStep.TOTAL,
            onNavigateBack = { onAction(ContributeActions.PreviousStep) },
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

        MwenyejiButton(
            text = buttonLabel,
            onClick = { onAction(ContributeActions.NextStep) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}
