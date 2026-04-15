package com.samuelokello.mwenyeji.feature.onboarding.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.feature.onboarding.componenets.TypewriterText
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

@Composable
fun SearchCard(visible: Boolean, modifier: Modifier = Modifier) {
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
                initialOffsetY = { -it / 2 },
            ) +
                fadeIn(),
        modifier = modifier,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceContainerLow)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = colors.tertiary,
                modifier = Modifier.size(18.dp),
            )
            TypewriterText(
                text = stringResource(R.string.karen),
                style = MwenyejiTheme.typography.bodyLarge,
                color = colors.primary,
                delayPerChar = 80L,
            )
        }
    }
}
