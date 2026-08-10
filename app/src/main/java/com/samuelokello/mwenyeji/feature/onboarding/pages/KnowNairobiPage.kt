package com.samuelokello.mwenyeji.feature.onboarding.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.feature.onboarding.animation.OnboardingPage
import com.samuelokello.mwenyeji.feature.onboarding.animation.rememberTimelineRunner
import com.samuelokello.mwenyeji.feature.onboarding.animation.timeline
import com.samuelokello.mwenyeji.feature.onboarding.componenets.NotificationCard
import com.samuelokello.mwenyeji.feature.onboarding.componenets.RouteCard
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme

sealed interface KnowNairobiEvent {
    data object ShowNotif : KnowNairobiEvent

    data object ShowCard : KnowNairobiEvent
}

val timeline =
    timeline<KnowNairobiEvent> {
        step(200) { emit -> emit(KnowNairobiEvent.ShowNotif) }
        step(400) { emit -> emit(KnowNairobiEvent.ShowCard) }
    }

@Composable
fun KnowNairobiPage(isActive: Boolean) {
    var showNotif by remember { mutableStateOf(false) }
    var showCard by remember { mutableStateOf(false) }

    val duration = MwenyejiTheme.duration

    val knowNairobiTimeline =
        remember(duration) {
            timeline {
                step(duration.SHORT.toLong()) { emit -> emit(KnowNairobiEvent.ShowNotif) }
                step(duration.NORMAL.toLong()) { emit -> emit(KnowNairobiEvent.ShowCard) }
            }
        }

    rememberTimelineRunner(
        isActive = isActive,
        timeline = knowNairobiTimeline,
        onReset = {
            showNotif = false
            showCard = false
        },
        onEvent = { event ->
            when (event) {
                KnowNairobiEvent.ShowNotif -> showNotif = true
                KnowNairobiEvent.ShowCard -> showCard = true
            }
        },
    )

    OnboardingPage(
        isActive = isActive,
        label = "Mwenyeji",
        title = stringResource(R.string.know_nairobi_like_a_local),
        subtitle = stringResource(R.string.real_routes_fares),
    ) {
        Column {
            NotificationCard(showNotif)
            Spacer(Modifier.height(12.dp))
            RouteCard(showCard)
        }
    }
}
