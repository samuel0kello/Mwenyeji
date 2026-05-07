package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.typography.JetBrainsFamily

@Composable
fun RouteCard(visible: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec =
            infiniteRepeatable(
                tween(2500, easing = EaseInOutSine),
                RepeatMode.Reverse,
            ),
        label = "glow_alpha",
    )
    val colors = MwenyejiTheme.colorScheme

    AnimatedVisibility(
        visible = visible,
        enter =
            slideInVertically(
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                initialOffsetY = { it / 2 },
            ) +
                fadeIn(tween(400)),
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceContainerLow)
                    .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "CBD → Westlands",
                style = MwenyejiTheme.typography.titleMedium,
                color = colors.onSurface,
            )
            Text(
                "Stage: Kencom Bus Stop",
                style = MwenyejiTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                MetaItem(label = stringResource(R.string.fare).uppercase(), value = "Ksh 50")
                MetaItem(label = stringResource(R.string.time), value = "~12 min")
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primary.copy(alpha = 0.8f))
                        .border(
                            1.dp,
                            colors.surfaceContainerLowest.copy(alpha = glowAlpha),
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("✓", color = colors.primary, fontSize = 13.sp)
                Text(
                    stringResource(R.string.confirmed_2h_ago_3_locals),
                    style = MwenyejiTheme.typography.bodySmall,
                    color = colors.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun MetaItem(label: String, value: String) {
    val colors = MwenyejiTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            label,
            style = MwenyejiTheme.typography.labelSmall,
            color = colors.onPrimaryContainer,
        )
        Text(
            value,
            fontFamily = JetBrainsFamily,
            fontSize = 18.sp,
            color = colors.primary,
        )
    }
}
