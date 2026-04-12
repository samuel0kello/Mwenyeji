package com.samuelokello.mwenyeji.feature.contribute.step

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.feature.contribute.ContributeActions
import com.samuelokello.mwenyeji.feature.contribute.ContributeState
import com.samuelokello.mwenyeji.ui.designsystem.components.inputFields.MwenyejiInputField
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun RouteStepScreen(
    state: ContributeState,
    onAction: (ContributeActions) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        StepInputField(
            label = "FROM",
            value = state.from,
            placeholder = "e.g., CBD, Kencom",
            onValueChange = { onAction(ContributeActions.FromChanged(it)) },
            errorMessage = state.errors["from"],
        )

        Spacer(Modifier.height(20.dp))

        StepInputField(
            label = "TO",
            value = state.to,
            placeholder = "e.g., Westlands, Sarit",
            onValueChange = { onAction(ContributeActions.ToChanged(it)) },
            errorMessage = state.errors["to"],
        )

        Spacer(Modifier.height(20.dp))

        StepInputField(
            label = "FARE (KSH)",
            value = state.fareKsh,
            placeholder = "e.g., 50",
            onValueChange = { onAction(ContributeActions.FareChanged(it)) },
            errorMessage = state.errors["fare"],
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@Composable
fun StepInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = MwenyejiTheme.colorScheme
    val typography = MwenyejiTheme.typography

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        MwenyejiInputField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder) },
            isError = errorMessage != null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth(),
        )
        // Validation error
        if (errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = typography.labelSmall,
                color = colors.error,
            )
        }
    }
}
