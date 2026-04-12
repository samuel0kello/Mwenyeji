package com.samuelokello.mwenyeji.ui.designsystem.components.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

/**
 * Text button for tertiary/minimal actions
 */
@Composable
fun MwenyejiTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = MwenyejiTheme.sizes.buttonMedium),
        enabled = enabled,
        colors =
            ButtonDefaults.textButtonColors(
                contentColor = MwenyejiTheme.colorScheme.primary,
                disabledContentColor = MwenyejiTheme.colorScheme.disabled,
            ),
        shape = MwenyejiTheme.shapes.small,
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
 * Convenience function for text button with text only
 */
@Composable
fun MwenyejiTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    MwenyejiTextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
    ) {
        Text(
            text = text,
            style = MwenyejiTheme.typography.labelLarge,
        )
    }
}
