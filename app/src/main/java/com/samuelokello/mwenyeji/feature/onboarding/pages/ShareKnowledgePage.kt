package com.samuelokello.mwenyeji.feature.onboarding.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.feature.onboarding.animation.OnboardingPage
import com.samuelokello.mwenyeji.feature.onboarding.animation.RememberTimelineRunner
import com.samuelokello.mwenyeji.feature.onboarding.animation.timeline
import com.samuelokello.mwenyeji.feature.onboarding.componenets.ConfidenceSection
import com.samuelokello.mwenyeji.feature.onboarding.componenets.ContribCard
import com.samuelokello.mwenyeji.feature.onboarding.componenets.PointsToast
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme

sealed interface ShareKnowledgeEvent {
    data object ShowCard : ShareKnowledgeEvent

    data object AnimateBar : ShareKnowledgeEvent

    data object ShowToast : ShareKnowledgeEvent

    data object HideToast : ShareKnowledgeEvent
}

@Composable
fun ShareKnowledgePage(isActive: Boolean) {
    var showCard by remember { mutableStateOf(false) }
    var animateBar by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }

    val duration = MwenyejiTheme.duration

    val shareKnowledgeTimeline =
        remember {
            timeline {
                step(duration.QUICK.toLong()) { emit -> emit(ShareKnowledgeEvent.ShowCard) }
                step(duration.LONG.toLong()) { emit -> emit(ShareKnowledgeEvent.AnimateBar) }
                step(duration.SLOW.toLong()) { emit -> emit(ShareKnowledgeEvent.ShowToast) }
                step(2500) { emit -> emit(ShareKnowledgeEvent.HideToast) }
            }
        }

    RememberTimelineRunner(
        isActive = isActive,
        timeline = shareKnowledgeTimeline,
        onReset = {
            showCard = false
            animateBar = false
            showToast = false
        },
        onEvent = { event ->
            when (event) {
                ShareKnowledgeEvent.ShowCard -> showCard = true
                ShareKnowledgeEvent.AnimateBar -> animateBar = true
                ShareKnowledgeEvent.ShowToast -> showToast = true
                ShareKnowledgeEvent.HideToast -> showToast = false
            }
        },
    )

    Box(modifier = Modifier.fillMaxSize()) {
        OnboardingPage(
            isActive = isActive,
            label = stringResource(R.string.contribute),
            title = stringResource(R.string.share_what_you_know),
            subtitle = stringResource(R.string.add_a_route_in_under_2_minutes_help_the_next_commuter),
        ) {
            Column {
                ContribCard(visible = showCard)
                Spacer(Modifier.height(16.dp))
                ConfidenceSection(animate = animateBar)
            }
        }

        // Toast — top right overlay
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 20.dp, end = 20.dp),
            contentAlignment = Alignment.TopEnd,
        ) {
            PointsToast(visible = showToast)
        }
    }
}
