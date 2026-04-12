package com.samuelokello.mwenyeji.feature.contribute.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.feature.contribute.ContributeActions
import com.samuelokello.mwenyeji.feature.contribute.ContributeState
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun InstructionsStepScreen(
    state: ContributeState,
    onAction: (ContributeActions) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        state.steps.forEachIndexed { index, stepValue ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Field takes all remaining width
                StepInputField(
                    label = "STEP ${index + 1}",
                    value = stepValue,
                    placeholder =
                        if (index == 0) {
                            "e.g., Go to Kencom stage near Hilton..."
                        } else {
                            "Continue..."
                        },
                    onValueChange = { onAction(ContributeActions.StepChanged(index, it)) },
                    errorMessage = if (index == 0) state.errors["steps"] else null,
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction =
                                if (index == state.steps.lastIndex) {
                                    ImeAction.Done
                                } else {
                                    ImeAction.Next
                                },
                        ),
                    modifier = Modifier.weight(1f),
                )

                if (state.steps.size > 1) {
                    IconButton(
                        onClick = { onAction(ContributeActions.RemoveStep(index)) },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Remove step ${index + 1}",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        // Add another step button
        TextButton(
            onClick = { onAction(ContributeActions.AddStep) },
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier
                .height(0.dp)
                .size(6.dp))
            Text(
                text = "Add another step",
                style = typography.labelMedium,
                color = colors.primary,
            )
        }
    }
}
