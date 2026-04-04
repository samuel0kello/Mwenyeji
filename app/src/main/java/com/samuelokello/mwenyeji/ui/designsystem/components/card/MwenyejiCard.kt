package com.samuelokello.mwenyeji.ui.designsystem.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.ui.theme.MwenyejiAppTheme
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

/**
 * Standard card component for Mwenyeji.
 *
 * Changes from original:
 *  - Added [onClick] — when provided, the card is clickable with a ripple.
 *    When null, it renders as a static surface (original behaviour).
 *  - Added [containerColor] — lets callers like [TimeOfDayChip] animate the
 *    background without needing a wrapper Box.
 *  - Added [contentColor] — overridable content color.
 *  - Cleaned up empty column wrappers in previews.
 */
@Composable
fun MwenyejiCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MwenyejiTheme.shapes.medium,
    elevation: Dp = MwenyejiTheme.elevation.level1,
    border: BorderStroke? = null,
    containerColor: Color = MwenyejiTheme.colorScheme.surface,
    contentColor: Color = MwenyejiTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        // Clickable variant — M3 Card(onClick) handles ripple + semantics
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor   = contentColor,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = border,
            content = content,
        )
    } else {
        // Static variant — no click handling, same as original
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor   = contentColor,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = border,
            content = content,
        )
    }
}

/**
 * Outlined card — zero elevation, outline border, no click.
 * Add [onClick] if you need an interactable outlined card.
 */
@Composable
fun MwenyejiOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MwenyejiTheme.shapes.medium,
    containerColor: Color = MwenyejiTheme.colorScheme.surface,
    contentColor: Color = MwenyejiTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    MwenyejiCard(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        elevation = MwenyejiTheme.elevation.level0,
        border = BorderStroke(
            width = MwenyejiTheme.sizes.borderWidth,
            color = MwenyejiTheme.colorScheme.outline,
        ),
        containerColor = containerColor,
        contentColor = contentColor,
        content = content,
    )
}

/**
 * Elevated card — higher shadow, surfaceContainerLow background.
 */
@Composable
fun MwenyejiElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MwenyejiTheme.shapes.medium,
    content: @Composable ColumnScope.() -> Unit,
) {
    MwenyejiCard(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        elevation = MwenyejiTheme.elevation.level3,
        containerColor = MwenyejiTheme.colorScheme.surfaceContainerLow,
        contentColor = MwenyejiTheme.colorScheme.onSurface,
        content = content,
    )
}



@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun MwenyejiCardPreview() {
    MwenyejiAppTheme {
        MwenyejiCard(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Standard card",
                modifier = Modifier.padding(16.dp),
                color = MwenyejiTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun MwenyejiCardClickablePreview() {
    MwenyejiAppTheme {
        MwenyejiCard(
            modifier = Modifier.padding(16.dp),
            onClick = {},
        ) {
            Text(
                text = "Clickable card",
                modifier = Modifier.padding(16.dp),
                color = MwenyejiTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun MwenyejiOutlinedCardPreview() {
    MwenyejiAppTheme {
        MwenyejiOutlinedCard(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Outlined card",
                modifier = Modifier.padding(16.dp),
                color = MwenyejiTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1210)
@Composable
private fun MwenyejiElevatedCardPreview() {
    MwenyejiAppTheme {
        MwenyejiElevatedCard(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Elevated card",
                modifier = Modifier.padding(16.dp),
                color = MwenyejiTheme.colorScheme.onSurface,
            )
        }
    }
}