package com.samuelokello.mwenyeji.feature.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.feature.onboarding.componenets.ConfidenceSection
import com.samuelokello.mwenyeji.feature.onboarding.componenets.GridBackground
import com.samuelokello.mwenyeji.feature.onboarding.componenets.PointsToast
import com.samuelokello.mwenyeji.feature.onboarding.componenets.ScreenBottom
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun Screen3ShareKnowledge(
    currentPage: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    var showCard by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var animateBar by remember { mutableStateOf(false) }

    LaunchedEffect(currentPage) {
        if (currentPage == 2) {
            showCard = false
            showToast = false
            animateBar = false
            delay(150)
            showCard = true
            delay(600)
            animateBar = true
            delay(800)
            showToast = true
            delay(2500)
            showToast = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
                    .padding(top = 60.dp, bottom = 44.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                androidx.compose.material3.TextButton(onClick = onSkip) {
                    androidx.compose.material3.Text(
                        "Skip",
                        color = TextSecondary,
                        style = MwenyejiTheme.typography.bodySmall,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                ContribCard(visible = showCard)
                Spacer(Modifier.height(16.dp))
                ConfidenceSection(animate = animateBar)
            }

            ScreenBottom(
                label = stringResource(R.string.contribute),
                title = stringResource(R.string.share_what_you_know),
                subtitle = stringResource(R.string.add_a_route_in_under_2_minutes_help_the_next_commuter),
                page = 2,
                btnText = stringResource(R.string.i_want_to_contribute),
                onNext = onNext,
            )
        }

        // Toast — top right overlay
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp, end = 20.dp),
            contentAlignment = Alignment.TopEnd,
        ) {
            PointsToast(visible = showToast)
        }
    }
}
