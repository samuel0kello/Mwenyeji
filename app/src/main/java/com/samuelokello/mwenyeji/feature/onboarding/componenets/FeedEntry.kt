package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.AmberWarm
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.DangerRed
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.GreenDim
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.GreenLight
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.TextPrimary
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.TextSecondary
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.TextTertiary

data class FeedEntry(
    val initials: String,
    val name: String,
    val route: String,
    val verdict: String,
    val time: String,
    val avatarBg: Color = GreenDim,
    val avatarFg: Color = GreenLight,
    val isWarning: Boolean = false,
)

@Composable
fun LiveFeedCard(entries: List<FeedEntry>, visibleCount: Int, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val liveDotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec =
            infiniteRepeatable(
                tween(800),
                RepeatMode.Reverse,
            ),
        label = "live_dot",
    )

    val colors = MwenyejiTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surfaceContainerLow)
                .border(1.dp, colors.border, RoundedCornerShape(20.dp)),
    ) {
        // Header
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(DangerRed.copy(alpha = liveDotAlpha)),
            )
            Text(
                "LIVE",
                style = MwenyejiTheme.typography.labelSmall,
                color = DangerRed,
                letterSpacing = 2.sp,
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.border),
        )

        // Feed rows
        entries.forEachIndexed { index, entry ->
            AnimatedVisibility(
                visible = index < visibleCount,
                enter =
                    slideInHorizontally(
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        initialOffsetX = { it / 2 },
                    ) +
                        fadeIn(tween(300)),
            ) {
                Column {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Avatar
                        Box(
                            modifier =
                                Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(entry.avatarBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                entry.initials,
                                style = MwenyejiTheme.typography.bodySmall,
                                color = entry.avatarFg,
                                fontSize = 12.sp,
                            )
                        }

                        // Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                entry.name,
                                style = MwenyejiTheme.typography.bodySmall,
                                color = TextPrimary,
                            )
                            Text(
                                entry.route,
                                style = MwenyejiTheme.typography.labelSmall,
                                color = if (entry.isWarning) AmberWarm else TextSecondary,
                            )
                        }

                        // Right
                        Column(horizontalAlignment = Alignment.End) {
                            Text(entry.verdict, fontSize = 16.sp)
                            Text(
                                entry.time,
                                style = MwenyejiTheme.typography.labelSmall,
                                color = TextTertiary,
                            )
                        }
                    }

                    if (index < entries.size - 1) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(colors.border),
                        )
                    }
                }
            }
        }
    }
}
