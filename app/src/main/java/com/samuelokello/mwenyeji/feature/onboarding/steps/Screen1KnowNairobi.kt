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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.samuelokello.mwenyeji.feature.onboarding.componenets.NotificationCard
import com.samuelokello.mwenyeji.feature.onboarding.componenets.RouteCard
import com.samuelokello.mwenyeji.feature.onboarding.componenets.ScreenBottom
import com.samuelokello.mwenyeji.ui.theme.MwenyejiTheme
import kotlinx.coroutines.delay

@Composable
fun Screen1KnowNairobi(
    currentPage: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    var showNotif by remember { mutableStateOf(false) }
    var showCard by remember { mutableStateOf(false) }

    // Trigger animations when this page is active
    LaunchedEffect(currentPage) {
        if (currentPage == 0) {
            showNotif = false
            showCard = false
            delay(200)
            showNotif = true
            delay(400)
            showCard = true
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        GridBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
                    .padding(top = 60.dp, bottom = 44.dp),
        ) {
            // Skip
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onSkip) {
                    Text(
                        "Skip",
                        color = MwenyejiTheme.colorScheme.onSecondaryContainer,
                        style = MwenyejiTheme.typography.bodySmall,
                    )
                }
            }

            // Hero
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                NotificationCard(visible = showNotif)
                Spacer(Modifier.height(12.dp))
                RouteCard(visible = showCard)
            }

            // Bottom
            ScreenBottom(
                label = stringResource(R.string.mwenyeji),
                title = stringResource(R.string.know_nairobi_like_a_local),
                subtitle = stringResource(R.string.real_routes_real_fares_from_people_who_ride_daily),
                page = 0,
                btnText = stringResource(R.string.let_s_go),
                onNext = onNext,
            )
        }
    }
}
