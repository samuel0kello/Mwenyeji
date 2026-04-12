package com.samuelokello.mwenyeji.feature.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.feature.onboarding.componenets.FeedEntry
import com.samuelokello.mwenyeji.feature.onboarding.componenets.GridBackground
import com.samuelokello.mwenyeji.feature.onboarding.componenets.LeaderBoard
import com.samuelokello.mwenyeji.feature.onboarding.componenets.LiveFeedCard
import com.samuelokello.mwenyeji.feature.onboarding.componenets.ScreenBottom
import com.samuelokello.mwenyeji.ui.theme.color.AppColors.DangerRed
import kotlinx.coroutines.delay

@Composable
fun Screen4Community(
    currentPage: Int,
    onFinish: () -> Unit,
) {
    var visibleRows by remember { mutableIntStateOf(0) }
    var showLeader by remember { mutableStateOf(false) }

    val feedEntries =
        listOf(
            FeedEntry("JM", "James", "CBD → Rongai", "✅", "2s ago"),
            FeedEntry(
                "AW",
                "Aisha",
                "Ngong → Westlands",
                "✅",
                "5s ago",
                avatarBg = Color(0xFF2E1A3D),
                avatarFg = Color(0xFFB78852),
            ),
            FeedEntry(
                "BK",
                "Brian",
                "⚠ Tom Mboya stage",
                "🚩",
                "8s ago",
                avatarBg = Color(0xFF3D1A1A),
                avatarFg = DangerRed,
                isWarning = true,
            ),
            FeedEntry(
                "WN",
                "Wanjiru",
                "Karen → CBD",
                "✅",
                "12s ago",
                avatarBg = Color(0xFF1A2E3D),
                avatarFg = Color(0xFF52A8B7),
            ),
        )

    LaunchedEffect(currentPage) {
        if (currentPage == 3) {
            visibleRows = 0
            showLeader = false
            delay(300)
            feedEntries.indices.forEach { _ ->
                delay(180L)
                visibleRows++
            }
            delay(300)
            showLeader = true
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                LiveFeedCard(entries = feedEntries, visibleCount = visibleRows)
                Spacer(Modifier.height(12.dp))
                LeaderBoard(visible = showLeader)
            }
            Spacer(Modifier.height(12.dp))

            ScreenBottom(
                label = stringResource(R.string.verify),
                title = stringResource(R.string.community_confirms_it),
                subtitle = stringResource(R.string.every_route_verified_by_fellow_commuters_trust_the_collective),
                page = 3,
                btnText = stringResource(R.string.explore_routes),
                onNext = onFinish,
                isFinal = true,
            )
        }
    }
}
