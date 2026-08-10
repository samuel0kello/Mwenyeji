package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@Composable
fun ProgressDots(
    total: Int,
    current: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            val isActive = index == current
            val width by animateDpAsState(
                targetValue = if (isActive) 16.dp else 8.dp,
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                label = "dot_width_$index",
            )
            Box(
                modifier =
                    Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isActive) MwenyejiTheme.colorScheme.primary else MwenyejiTheme.colorScheme.primaryContainer),
            )
        }
    }
}
