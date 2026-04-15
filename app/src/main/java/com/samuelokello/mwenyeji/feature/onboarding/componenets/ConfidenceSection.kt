package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.DangerRed
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.TextSecondary
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.TextTertiary

@Composable
fun ConfidenceSection(animate: Boolean, modifier: Modifier = Modifier) {
    val barProgress by animateFloatAsState(
        targetValue = if (animate) 0.8f else 0f,
        animationSpec =
            tween(
                durationMillis = 1200,
                delayMillis = 200,
                easing = FastOutSlowInEasing,
            ),
        label = "confidence_bar",
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Avatars + label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Stacked avatars
            Box(
                modifier =
                    Modifier
                        .width(72.dp)
                        .height(32.dp),
            ) {
                listOf(
                    Triple(
                        "JM",
                        MwenyejiTheme.colorScheme.primary,
                        MwenyejiTheme.colorScheme.primaryLight,
                    ),
                    Triple("AW", Color(0xFF2E1A3D), Color(0xFFB78852)),
                    Triple("BK", Color(0xFF3D1A1A), DangerRed),
                ).forEachIndexed { i, (initials, bg, fg) ->
                    Box(
                        modifier =
                            Modifier
                                .offset(x = (i * 20).dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(bg)
                                .then(
                                    if (i > 0) {
                                        Modifier.padding(start = 0.dp)
                                    } else {
                                        Modifier
                                    },
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(initials, style = MwenyejiTheme.typography.bodySmall, color = fg)
                    }
                }
            }
            Text(
                stringResource(R.string._3_people_confirmed_this_route),
                style = MwenyejiTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }

        // Bar
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.confidence),
                    style = MwenyejiTheme.typography.labelSmall,
                    color = TextTertiary,
                )
                Text(
                    "${(barProgress * 100).toInt()}%",
                    style = MwenyejiTheme.typography.labelSmall,
                    color = MwenyejiTheme.colorScheme.secondary,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MwenyejiTheme.colorScheme.secondary),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(barProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(MwenyejiTheme.colorScheme.primary),
                )
            }
        }
    }
}
