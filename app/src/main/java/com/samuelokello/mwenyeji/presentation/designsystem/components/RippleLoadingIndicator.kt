package com.samuelokello.mwenyeji.presentation.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@Composable
fun RippleLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MwenyejiTheme.colorScheme.primary,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
            ),
        label = "ripple_time",
    )

    Canvas(modifier = modifier.size(60.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Draw 3 rippling circles
        repeat(3) { i ->
            val radius = (time + i * 20) % 100
            val opacity = (1 - radius / 100).coerceAtLeast(0f)

            drawCircle(
                color = color,
                center = Offset(centerX, centerY),
                radius = (radius / 100) * 25.dp.toPx(),
                style = Stroke(width = 2.dp.toPx()),
                alpha = opacity * 0.7f,
            )
        }

        // Center dot
        drawCircle(
            color = color,
            center = Offset(centerX, centerY),
            radius = 4.dp.toPx(),
        )
    }
}
