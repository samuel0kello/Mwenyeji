package com.samuelokello.mwenyeji.feature.onboarding.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.TextPrimary
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.TextSecondary
import com.samuelokello.mwenyeji.ui.theme.typography.JetBrainsFamily

data class RouteResult(
    val from: String,
    val to: String,
    val fare: String,
    val confidence: String,
    val duration: String,
    val dotColor: Color,
    val isActive: Boolean = false,
    val steps: List<String> = emptyList(),
)

@Composable
fun ResultRouteCard(result: RouteResult, visible: Boolean, delayMillis: Int = 0, modifier: Modifier = Modifier) {
    val colors = MwenyejiTheme.colorScheme
    AnimatedVisibility(
        visible = visible,
        enter =
            slideInHorizontally(
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                initialOffsetX = { it },
            ) +
                fadeIn(tween(400, delayMillis)),
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceContainerLow)
                    .border(
                        width = 1.dp,
                        color = if (result.isActive) colors.primary else colors.background,
                        shape = RoundedCornerShape(16.dp),
                    ).padding(16.dp),
        ) {
            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${result.from} → ${result.to}",
                    style = MwenyejiTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontSize = 16.sp,
                )
                Text(
                    result.fare,
                    fontFamily = JetBrainsFamily,
                    fontSize = 16.sp,
                    color = colors.primary,
                )
            }

            Spacer(Modifier.height(6.dp))

            // Confidence tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(result.dotColor),
                )
                Text(
                    "${result.confidence} · ${result.duration}",
                    style = MwenyejiTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            // Steps (only for active card)
            if (result.isActive && result.steps.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.border),
                )
                Spacer(Modifier.height(12.dp))
                result.steps.forEachIndexed { index, step ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = if (index < result.steps.size - 1) 8.dp else 0.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "${index + 1}",
                                fontFamily = JetBrainsFamily,
                                fontSize = 11.sp,
                                color = colors.background,
                            )
                        }
                        Text(
                            step,
                            style = MwenyejiTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}
