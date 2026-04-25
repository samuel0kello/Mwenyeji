package com.samuelokello.mwenyeji.ui.designsystem.components.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

/**
 * Primary filled button for Mwenyeji app
 */
@Composable
fun MwenyejiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = MwenyejiTheme.sizes.buttonMedium),
        enabled = enabled,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MwenyejiTheme.colorScheme.primary,
                contentColor = MwenyejiTheme.colorScheme.onPrimary,
                disabledContainerColor = MwenyejiTheme.colorScheme.disabledContainer,
                disabledContentColor = MwenyejiTheme.colorScheme.disabled,
            ),
        shape = MwenyejiTheme.shapes.small,
        elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation = MwenyejiTheme.elevation.level0,
                pressedElevation = MwenyejiTheme.elevation.level1,
            ),
        contentPadding =
            PaddingValues(
                horizontal = MwenyejiTheme.spacing.medium,
                vertical = MwenyejiTheme.spacing.small,
            ),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(MwenyejiTheme.sizes.iconSmall),
            )
            Spacer(modifier = Modifier.width(MwenyejiTheme.spacing.small))
        }
        content()
    }
}

/**
 * Convenience function for button with text only
 */
@Composable
fun MwenyejiButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: ImageVector? = null) {
    MwenyejiButton(
        onClick = onClick,
        enabled = enabled,
        icon = icon,
    ) {
        Text(
            text = text,
            style = MwenyejiTheme.typography.labelSmall,
        )
    }
}

@Preview
@Composable
private fun MwenyejiButtonPreview() {
    MwenyejiButton(
        text = "Button",
        onClick = { },
    )
}
