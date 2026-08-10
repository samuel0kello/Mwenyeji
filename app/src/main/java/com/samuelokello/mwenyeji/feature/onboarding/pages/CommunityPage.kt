package com.samuelokello.mwenyeji.feature.onboarding.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
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
import com.samuelokello.mwenyeji.feature.onboarding.animation.OnboardingPage
import com.samuelokello.mwenyeji.feature.onboarding.animation.rememberTimelineRunner
import com.samuelokello.mwenyeji.feature.onboarding.animation.timeline
import com.samuelokello.mwenyeji.feature.onboarding.componenets.FeedEntry
import com.samuelokello.mwenyeji.feature.onboarding.componenets.LeaderBoard
import com.samuelokello.mwenyeji.feature.onboarding.componenets.LiveFeedCard
import com.samuelokello.mwenyeji.presentation.ui.theme.MwenyejiTheme
import com.samuelokello.mwenyeji.presentation.ui.theme.color.AppColors.DangerRed

sealed interface CommunityEvent {
    data object ShowFeedRow : CommunityEvent

    data object ShowLeader : CommunityEvent
}

@Composable
fun CommunityPage(isActive: Boolean) {
    var visibleRows by remember { mutableIntStateOf(0) }
    var showLeader by remember { mutableStateOf(false) }
    val duration = MwenyejiTheme.duration

    val communityTimeline =
        remember(duration) {
            timeline {
                step(duration.MEDIUM.toLong()) { emit -> emit(CommunityEvent.ShowFeedRow) }
                step(180) { emit -> emit(CommunityEvent.ShowFeedRow) }
                step(180) { emit -> emit(CommunityEvent.ShowFeedRow) }
                step(180) { emit -> emit(CommunityEvent.ShowFeedRow) }
                step(duration.MEDIUM.toLong()) { emit -> emit(CommunityEvent.ShowLeader) }
            }
        }

    val feedEntries =
        remember {
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
        }

    rememberTimelineRunner(
        isActive = isActive,
        timeline = communityTimeline,
        onReset = {
            visibleRows = 0
            showLeader = false
        },
        onEvent = { event ->
            when (event) {
                CommunityEvent.ShowFeedRow -> if (visibleRows < feedEntries.size) visibleRows++
                CommunityEvent.ShowLeader -> showLeader = true
            }
        },
    )

    OnboardingPage(
        isActive = isActive,
        label = stringResource(R.string.verify),
        title = stringResource(R.string.community_confirms_it),
        subtitle = stringResource(R.string.every_route_verified_by_fellow_commuters_trust_the_collective),
    ) {
        Column {
            LiveFeedCard(entries = feedEntries, visibleCount = visibleRows)
            Spacer(Modifier.height(12.dp))
            LeaderBoard(visible = showLeader)
        }
    }
}
