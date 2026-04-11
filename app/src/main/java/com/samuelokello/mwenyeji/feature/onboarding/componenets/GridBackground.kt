package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun GridBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "grid")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val colors = MwenyejiTheme.colorScheme

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val color = colors.onSurface.copy(alpha = pulse * 0.6f)
        val dotColor = colors.primaryDark.copy(alpha = pulse)

        val cols = listOf(w * .2f, w * .46f, w * .72f, w * .9f)
        val rows = listOf(h * .14f, h * .28f, h * .43f, h * .57f, h * .71f)

        // horizontal lines
        rows.forEach { y ->
            drawLine(color, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }
        // vertical lines
        cols.forEach { x ->
            drawLine(color, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
        }
        // diagonal connectors
        drawLine(color, Offset(cols[0], rows[0]), Offset(cols[1], rows[1]), strokeWidth = 1f)
        drawLine(color, Offset(cols[1], rows[0]), Offset(cols[2], rows[1]), strokeWidth = 1f)
        drawLine(color, Offset(cols[0], rows[2]), Offset(cols[1], rows[3]), strokeWidth = 1f)
        drawLine(color, Offset(cols[2], rows[2]), Offset(cols[3], rows[3]), strokeWidth = 1f)

        // intersection dots
        listOf(
            Offset(cols[0], rows[0]), Offset(cols[1], rows[1]),
            Offset(cols[2], rows[1]), Offset(cols[0], rows[2]),
            Offset(cols[3], rows[3]),
        ).forEach { offset ->
            drawCircle(dotColor, radius = 4f, center = offset)
        }
    }
}