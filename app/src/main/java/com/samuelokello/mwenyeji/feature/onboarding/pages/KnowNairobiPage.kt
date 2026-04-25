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
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.feature.onboarding.animation.OnboardingPage
import com.samuelokello.mwenyeji.feature.onboarding.animation.RememberTimelineRunner
import com.samuelokello.mwenyeji.feature.onboarding.animation.timeline
import com.samuelokello.mwenyeji.feature.onboarding.componenets.NotificationCard
import com.samuelokello.mwenyeji.feature.onboarding.componenets.RouteCard

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

    val t = remember { timeline }

    RememberTimelineRunner(
        isActive = isActive,
        timeline = t,
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
        title = "Know Nairobi like a local",
        subtitle = "Real routes & fares",
    ) {
        Column {
            NotificationCard(showNotif)
            Spacer(Modifier.height(12.dp))
            RouteCard(showCard)
        }
    }
}
