package com.samuelokello.mwenyeji.ui.designsystem.components.button

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

/**
 * Outlined button for secondary actions
 */
@Composable
fun MwenyejiOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = MwenyejiTheme.sizes.buttonMedium),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MwenyejiTheme.colorScheme.primary,
            disabledContentColor = MwenyejiTheme.colorScheme.disabled,
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = SolidColor(MwenyejiTheme.colorScheme.outline)
        ),
        shape = MwenyejiTheme.shapes.small,
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
 * Convenience function for outlined button with text only
 */
@Composable
fun MwenyejiOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    MwenyejiOutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        icon = icon
    ) {
        Text(
            text = text,
            style = MwenyejiTheme.typography.labelLarge
        )
    }
}