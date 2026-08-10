package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuelokello.mwenyeji.presentation.designsystem.animation.SlideAnimatedVisibility
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.GreenLight
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.TextSecondary
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.TextTertiary
import com.samuelokello.mwenyeji.presentation.ui.theme.typography.JetBrainsFamily

@Composable
fun LeaderBoard(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "you_row")
    val youAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                tween(1800, easing = EaseInOutSine),
                RepeatMode.Reverse,
            ),
        label = "you_breathe",
    )
    val colors = MwenyejiTheme.colorScheme

    SlideAnimatedVisibility(
        visible = visible,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceContainerLow)
                    .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("🏆", fontSize = 14.sp)
                Text(
                    "Top contributors this week",
                    style = MwenyejiTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Row 1
            LeaderRow(rank = "1", name = "WanjikuNairobi", pts = "340 pts", badge = "🥇")
            Spacer(Modifier.height(2.dp))
            // Row 2
            LeaderRow(rank = "2", name = "MajikoMtaa", pts = "280 pts", badge = "🥈")
            Spacer(Modifier.height(2.dp))

            // You row — breathing animation
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .alpha(youAlpha)
                        .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "3",
                    fontFamily = JetBrainsFamily,
                    fontSize = 13.sp,
                    color = TextTertiary,
                    modifier = Modifier.width(16.dp),
                )
                Text(
                    "You?",
                    style = MwenyejiTheme.typography.bodySmall,
                    color = GreenLight,
                    modifier = Modifier.weight(1f),
                )
                // Blurred points
                Text(
                    "000 pts",
                    fontFamily = JetBrainsFamily,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.blur(4.dp),
                )
                Text(
                    "←",
                    style = MwenyejiTheme.typography.bodySmall,
                    color = GreenLight,
                )
            }
        }
    }
}

@Composable
private fun LeaderRow(
    rank: String,
    name: String,
    pts: String,
    badge: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            rank,
            fontFamily = JetBrainsFamily,
            fontSize = 13.sp,
            color = TextTertiary,
            modifier = Modifier.width(16.dp),
        )
        Text(
            name,
            style = MwenyejiTheme.typography.bodySmall,
            color = TextTertiary,
            modifier = Modifier.weight(1f),
        )
        Text(pts, fontFamily = JetBrainsFamily, fontSize = 13.sp, color = TextSecondary)
        Text(badge, fontSize = 16.sp)
    }
}
