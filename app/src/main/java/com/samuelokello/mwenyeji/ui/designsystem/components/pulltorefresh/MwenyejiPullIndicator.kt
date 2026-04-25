package com.samuelokello.mwenyeji.ui.designsystem.components.pulltorefresh

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.ui.theme.animation.AppEasing

private val INDICATOR_HEIGHT = 120.dp
private const val OFFSCREEN_X = -200f

@Composable
internal fun MwenyejiPullIndicator(
    pullProgress: Float,
    isRefreshing: Boolean,
    statusText: String,
    modifier: Modifier = Modifier
) {
    val targetHeight =
        when {
            isRefreshing -> INDICATOR_HEIGHT
            pullProgress > 0f -> INDICATOR_HEIGHT * pullProgress.coerceAtMost(1f)
            else -> 0.dp
        }
    val height by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(300, easing = AppEasing.standard),
        label = "indicatorHeight",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        if (height > 0.dp) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                PinIllustration(progress = pullProgress, isRefreshing = isRefreshing)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = statusText,
                    style = MwenyejiTheme.typography.labelMedium,
                    color = MwenyejiTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PinIllustration(progress: Float, isRefreshing: Boolean) {
    val isEngaged = isRefreshing || progress >= 1f
    val targetOffsetX = if (isEngaged) 0.dp else (OFFSCREEN_X * (1f - progress)).dp

    val offsetX by animateDpAsState(
        targetValue = targetOffsetX,
        animationSpec = if (isRefreshing) tween(300) else tween(150),
        label = "pinX",
    )

    val pinScale by rememberInfiniteTransition(label = "pinPulse").animateFloat(
        initialValue = 1f,
        targetValue = if (isRefreshing) 1.1f else 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(800, easing = AppEasing.standard),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pinScale",
    )

    Image(
        painter = painterResource(R.drawable.ic_navigation),
        contentDescription = null,
        modifier =
            Modifier
                .size(width = 96.dp, height = 48.dp)
                .offset(x = offsetX)
                .graphicsLayer {
                    scaleX = pinScale
                    scaleY = pinScale
                },
    )
}
