package com.samuelokello.mwenyeji.presentation.designsystem.components.inputFields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@Composable
fun MwenyejiInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        enabled = enabled,
        readOnly = readOnly,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        colors =
            OutlinedTextFieldDefaults.colors(
                // 1. Container Colors (The dark grey box)
                focusedContainerColor = MwenyejiTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MwenyejiTheme.colorScheme.surfaceContainer,
                // 2. Text Colors
                focusedTextColor = MwenyejiTheme.colorScheme.onSurfaceVariant,
                unfocusedTextColor = MwenyejiTheme.colorScheme.onSurface,
                // 3. Border Colors (Making them transparent to look like "Filled" cards)
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                // 4. Cursor Color (Matching your blue/primary accent in the image)
                cursorColor = MwenyejiTheme.colorScheme.primary,
                // 5. Placeholder/Label Colors
                unfocusedLabelColor = MwenyejiTheme.colorScheme.primary,
                focusedLabelColor = MwenyejiTheme.colorScheme.primary,
            ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview
@Composable
fun MwenyejiInputFieldPrev(modifier: Modifier = Modifier) {
    MwenyejiInputField(
        value = "Test value",
        onValueChange = {},
    )
}
