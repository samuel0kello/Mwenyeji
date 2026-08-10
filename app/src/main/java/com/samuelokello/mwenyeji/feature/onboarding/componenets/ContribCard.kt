package com.samuelokello.mwenyeji.feature.onboarding.componenets

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
import androidx.compose.foundation.layout.width
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
import com.samuelokello.mwenyeji.presentation.designsystem.animation.SlideAnimatedVisibility
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.typography.JetBrainsFamily

@Composable
fun ContribCard(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
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
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "+ NEW ROUTE",
                style = MwenyejiTheme.typography.labelSmall,
                color = colors.primaryLight,
            )
            FieldRow(key = stringResource(R.string.from), value = "Githurai 45")
            FieldRow(key = stringResource(R.string.to), value = "CBD, Kencom")
            FieldRow(key = stringResource(R.string.fare), value = "Ksh 70")

            Spacer(Modifier.height(2.dp))
            Text(
                "BEST TIME",
                style = MwenyejiTheme.typography.labelSmall,
                color = colors.primary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(label = stringResource(R.string.morning_rush), selected = true)
                Chip(label = stringResource(R.string.midday), selected = false)
                Chip(label = stringResource(R.string.evening), selected = false)
            }
        }
    }
}

@Composable
private fun FieldRow(
    key: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            key,
            style = MwenyejiTheme.typography.bodySmall,
            color = MwenyejiTheme.colorScheme.primaryLight,
            modifier = Modifier.width(36.dp),
        )
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        MwenyejiTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f),
                    )
                    .border(1.dp, MwenyejiTheme.colorScheme.border, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                value,
                fontFamily = JetBrainsFamily,
                fontSize = 14.sp,
                color = MwenyejiTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun Chip(
    label: String,
    selected: Boolean,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (selected) MwenyejiTheme.colorScheme.surfaceContainer else MwenyejiTheme.colorScheme.surfaceContainerLow)
                .border(
                    1.dp,
                    if (selected) MwenyejiTheme.colorScheme.primary else MwenyejiTheme.colorScheme.border,
                    RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            label,
            style = MwenyejiTheme.typography.bodySmall,
            color = if (selected) MwenyejiTheme.colorScheme.primary else MwenyejiTheme.colorScheme.secondary,
        )
    }
}
