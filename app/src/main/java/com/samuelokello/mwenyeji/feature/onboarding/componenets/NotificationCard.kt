package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun NotificationCard(visible: Boolean, modifier: Modifier = Modifier) {
    val color = MwenyejiTheme.colorScheme
    AnimatedVisibility(
        visible = visible,
        enter =
            slideInVertically(
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                initialOffsetY = { -it },
            ) +
                fadeIn(),
        modifier = modifier,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.surfaceContainerLow)
                    .border(
                        width = 1.dp,
                        color = color.border,
                        shape = RoundedCornerShape(14.dp),
                    ).border(
                        width = 2.dp,
                        color = color.border,
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp),
                    ).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("🚌", fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.mwenyeji_7_04_am),
                    style = MwenyejiTheme.typography.labelSmall,
                    color = color.tertiaryContainer,
                )
                Text(
                    stringResource(R.string.need_to_get_to_westlands),
                    style = MwenyejiTheme.typography.bodySmall,
                    color = color.primary,
                )
            }
        }
    }
}
