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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.feature.onboarding.componenets.GridBackground
import com.samuelokello.mwenyeji.feature.onboarding.componenets.ScreenBottom
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.AmberWarm
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.GreenLight
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun Screen2FindRoute(
    currentPage: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    var showSearch by remember { mutableStateOf(false) }
    var showCard1 by remember { mutableStateOf(false) }
    var showCard2 by remember { mutableStateOf(false) }

    LaunchedEffect(currentPage) {
        if (currentPage == 1) {
            showSearch = false
            showCard1 = false
            showCard2 = false
            delay(150)
            showSearch = true
            delay(400)
            showCard1 = true
            delay(200)
            showCard2 = true
        }
    }

    val results = listOf(
        RouteResult(
            from = "CBD",
            to = "Karen",
            fare = "Ksh 150",
            confidence = "Reliable",
            duration = "45 min",
            dotColor = GreenLight,
            isActive = true,
            steps = listOf(
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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        GridBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 60.dp, bottom = 44.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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
                SearchCard(visible = showSearch)
                Spacer(Modifier.height(12.dp))
                ResultRouteCard(result = results[0], visible = showCard1)
                Spacer(Modifier.height(10.dp))
                ResultRouteCard(result = results[1], visible = showCard2, delayMillis = 150)
            }

            ScreenBottom(
                label = stringResource(R.string.discover),
                title = stringResource(R.string.find_your_route_instantly),
                subtitle = stringResource(R.string.search_any_destination_step_by_step_matatu_directions_with_fares),
                page = 1,
                btnText = stringResource(R.string.see_how_it_works),
                onNext = onNext,
            )
        }
    }
}