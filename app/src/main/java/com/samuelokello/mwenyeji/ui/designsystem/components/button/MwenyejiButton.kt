package com.samuelokello.mwenyeji.ui.designsystem.components.button

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import androidx.compose.material3.Icon
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Primary filled button for Mwenyeji app
 */
@Composable
fun MwenyejiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = MwenyejiTheme.sizes.buttonMedium),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MwenyejiTheme.colorScheme.primary,
            contentColor = MwenyejiTheme.colorScheme.onPrimary,
            disabledContainerColor = MwenyejiTheme.colorScheme.disabledContainer,
            disabledContentColor = MwenyejiTheme.colorScheme.disabled,
        ),
        shape = MwenyejiTheme.shapes.small,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = MwenyejiTheme.elevation.level0,
            pressedElevation = MwenyejiTheme.elevation.level1,
        ),
        contentPadding = PaddingValues(
            horizontal = MwenyejiTheme.spacing.medium,
            vertical = MwenyejiTheme.spacing.small
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(MwenyejiTheme.sizes.iconSmall)
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
fun MwenyejiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    MwenyejiButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(54.dp),
        enabled = enabled,
        icon = icon
    ) {
        Text(
            text = text,
            style = MwenyejiTheme.typography.labelLarge
        )
    }
}

@Preview
@Composable
private fun MwenyejiButtonPreview() {
    MwenyejiButton(
        text = "Button",
        onClick = { }
    )
}