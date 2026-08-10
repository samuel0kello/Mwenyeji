package com.samuelokello.mwenyeji.feature.onboarding.componenets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.presentation.designsystem.animation.MwenyejiAnimatedVisibility
import com.samuelokello.mwenyeji.presentation.designsystem.animation.MwenyejiAnimations
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

@Composable
fun SearchCard(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MwenyejiTheme.colorScheme
    MwenyejiAnimatedVisibility(
        visible = visible,
        enter = MwenyejiAnimations.slideDownEnter,
        exit = MwenyejiAnimations.slideUpExit,
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
                painter = painterResource(R.drawable.ic_outline_search),
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
