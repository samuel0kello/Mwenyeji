package com.samuelokello.mwenyeji.presentation.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiAppTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@Composable
fun MwenyejiSearchBar(state: TextFieldState, placeholder: String, modifier: Modifier = Modifier, onSearchAction: () -> Unit = {}) {
    BasicTextField(
        state = state,
        textStyle =
            MwenyejiTheme.typography.bodyMedium.copy(
                color = MwenyejiTheme.colorScheme.primary,
            ),
        cursorBrush = SolidColor(MwenyejiTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        onKeyboardAction = {
            onSearchAction()
        },
        modifier =
            modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MwenyejiTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.1f))
                .border(0.4.dp, MwenyejiTheme.colorScheme.border, RoundedCornerShape(12.dp)),
        decorator = { innerTextField ->
            Row(
                modifier =
                    Modifier
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_outline_search),
                    contentDescription = null,
                    tint = MwenyejiTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MwenyejiTheme.typography.bodyMedium,
                            color = MwenyejiTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }

                if (state.text.isNotEmpty()) {
                    IconButton(
                        onClick = { state.clearText() },
                        modifier = Modifier.size(20.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_outline_close),
                            contentDescription = "Clear search",
                            tint = MwenyejiTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
    )
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun MwenyejiSearchPreviewLight() {
    MwenyejiAppTheme {
        MwenyejiSearchBar(
            state = TextFieldState(""),
            placeholder = "Search",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MwenyejiSearchPreviewDark() {
    MwenyejiAppTheme {
        MwenyejiSearchBar(
            state = TextFieldState(""),
            placeholder = "Search",
            modifier = Modifier.padding(16.dp),
        )
    }
}
