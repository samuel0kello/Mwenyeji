package com.samuelokello.mwenyeji.ui.designsystem.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

/**
 * Standard card component for Mwenyeji app
 */
@Composable
fun MwenyejiCard(
    modifier: Modifier = Modifier,
    shape: Shape = MwenyejiTheme.shapes.medium,
    elevation: Dp = MwenyejiTheme.elevation.level1,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MwenyejiTheme.colorScheme.surface,
            contentColor = MwenyejiTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        border = border,
        content = content
    )
}

/**
 * Outlined card variant
 */
@Composable
fun MwenyejiOutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = MwenyejiTheme.shapes.medium,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MwenyejiTheme.colorScheme.surface,
            contentColor = MwenyejiTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MwenyejiTheme.elevation.level0
        ),
        border = BorderStroke(
            width = MwenyejiTheme.sizes.borderWidth,
            color = MwenyejiTheme.colorScheme.outline
        ),
        content = content
    )
}

/**
 * Elevated card variant with more prominent shadow
 */
@Composable
fun MwenyejiElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = MwenyejiTheme.shapes.medium,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MwenyejiTheme.colorScheme.surfaceContainerLow,
            contentColor = MwenyejiTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MwenyejiTheme.elevation.level3
        ),
        content = content
    )
}

@Preview(showBackground = true)
@Composable
private fun MwenyejiCardPreview() {
    MwenyejiCard(
    ) {
        Column() {
            Text("Card")
        }
    }
}