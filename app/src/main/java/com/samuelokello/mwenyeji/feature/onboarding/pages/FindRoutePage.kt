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
import com.samuelokello.mwenyeji.feature.onboarding.animation.RememberTimelineRunner
import com.samuelokello.mwenyeji.feature.onboarding.animation.timeline
import com.samuelokello.mwenyeji.feature.onboarding.componenets.ResultRouteCard
import com.samuelokello.mwenyeji.feature.onboarding.componenets.RouteResult
import com.samuelokello.mwenyeji.feature.onboarding.componenets.SearchCard
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.AmberWarm
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.GreenLight

sealed interface FindRouteEvent {
    data object ShowSearch : FindRouteEvent

    data object ShowCard1 : FindRouteEvent

    data object ShowCard2 : FindRouteEvent
}

val findRouteTimeline =
    timeline<FindRouteEvent> {
        step(150) { emit -> emit(FindRouteEvent.ShowSearch) }
        step(400) { emit -> emit(FindRouteEvent.ShowCard1) }
        step(200) { emit -> emit(FindRouteEvent.ShowCard2) }
    }

@Composable
fun FindRoutePage(isActive: Boolean) {
    var showSearch by remember { mutableStateOf(false) }
    var showCard1 by remember { mutableStateOf(false) }
    var showCard2 by remember { mutableStateOf(false) }

    val duration = MwenyejiTheme.duration

    val findRouteTimeline =
        remember(duration) {
            timeline {
                step(duration.QUICK.toLong()) { emit -> emit(FindRouteEvent.ShowSearch) }
                step(duration.NORMAL.toLong()) { emit -> emit(FindRouteEvent.ShowCard1) }
                step(duration.SHORT.toLong()) { emit -> emit(FindRouteEvent.ShowCard2) }
            }
        }

    val results =
        remember {
            listOf(
                RouteResult(
                    from = "CBD",
                    to = "Karen",
                    fare = "Ksh 150",
                    confidence = "Reliable",
                    duration = "45 min",
                    dotColor = GreenLight,
                    isActive = true,
                    steps =
                        listOf(
                            "Board at Kencom, ask for Karen route",
                            "Alight at Galleria roundabout",
                        ),
                ),
                RouteResult(
                    from = "Ngong Rd",
                    to = "Karen",
                    fare = "Ksh 80",
                    confidence = "Moderate",
                    duration = "38 min",
                    dotColor = AmberWarm,
                    isActive = false,
                ),
            )
        }

    RememberTimelineRunner(
        isActive = isActive,
        timeline = findRouteTimeline,
        onReset = {
            showSearch = false
            showCard1 = false
            showCard2 = false
        },
        onEvent = { event ->
            when (event) {
                FindRouteEvent.ShowSearch -> showSearch = true
                FindRouteEvent.ShowCard1 -> showCard1 = true
                FindRouteEvent.ShowCard2 -> showCard2 = true
            }
        },
    )

    OnboardingPage(
        isActive = isActive,
        label = stringResource(R.string.discover),
        title = stringResource(R.string.find_your_route_instantly),
        subtitle = stringResource(R.string.search_any_destination_step_by_step_matatu_directions_with_fares),
    ) {
        Column {
            SearchCard(visible = showSearch)
            Spacer(Modifier.height(12.dp))
            ResultRouteCard(result = results[0], visible = showCard1)
            Spacer(Modifier.height(10.dp))
            ResultRouteCard(result = results[1], visible = showCard2)
        }
    }
}
