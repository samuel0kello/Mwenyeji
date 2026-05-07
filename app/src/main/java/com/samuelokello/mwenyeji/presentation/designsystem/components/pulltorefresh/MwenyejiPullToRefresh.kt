package com.samuelokello.mwenyeji.presentation.designsystem.components.pulltorefresh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay

/**
 * Mwenyeji-branded pull-to-refresh container.
 *
 * Drop this around any scrollable content (LazyColumn, Column with verticalScroll, etc).
 * Handles the gesture, the indicator visuals, haptics, and post-refresh confirmation
 * automatically.
 *
 * Example:
 * ```
 * MwenyejiPullToRefresh(
 *     isRefreshing = state.isRefreshing,
 *     onRefresh = viewModel::refresh,
 * ) {
 *     LazyColumn { ... }
 * }
 * ```
 *
 * @param isRefreshing whether a refresh is currently in progress
 * @param onRefresh called when the user pulls past the threshold and releases
 * @param modifier modifier for the outer container
 * @param pullingText copy shown while user is actively pulling (default: "Pull to refresh")
 * @param releaseText copy shown when pull threshold is crossed (default: "Release to refresh")
 * @param refreshingText copy shown while refresh is in progress (default: "Asking the streets...")
 * @param confirmationText optional copy shown briefly after refresh completes (default: null = nothing)
 * @param content the scrollable content. The indicator renders ABOVE this in the gap.
 *               Important: include the indicator inside your scrolling list as the first item.
 *               See [MwenyejiPullToRefreshScope.indicatorItem].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MwenyejiPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    pullingText: String = "Pull to refresh",
    releaseText: String = "Release to refresh",
    refreshingText: String = "Asking the streets...",
    confirmationText: String? = null,
    content: @Composable MwenyejiPullToRefreshScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    val haptics = LocalHapticFeedback.current

    // Track whether we just finished a refresh, for showing confirmation text briefly.
    var showConfirmation by remember { mutableStateOf(false) }
    var previousRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (previousRefreshing && !isRefreshing && confirmationText != null) {
            showConfirmation = true
            delay(1500)
            showConfirmation = false
        }
        previousRefreshing = isRefreshing
    }

    // Haptic when threshold is first crossed.
    val willRefresh by remember { derivedStateOf { state.distanceFraction > 1f } }
    LaunchedEffect(willRefresh) {
        if (willRefresh) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Compute the unified pull progress for the indicator.
    val pullProgress = if (isRefreshing) 1f else state.distanceFraction.coerceIn(0f, 1f)

    val statusText =
        when {
            showConfirmation && confirmationText != null -> confirmationText
            isRefreshing -> refreshingText
            state.distanceFraction >= 1f -> releaseText
            state.distanceFraction > 0.3f -> pullingText
            else -> ""
        }

    val scope =
        remember(pullProgress, isRefreshing, statusText) {
            MwenyejiPullToRefreshScopeImpl(
                pullProgress = pullProgress,
                isRefreshing = isRefreshing,
                statusText = statusText,
            )
        }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = state,
        indicator = {}, // suppress overlay; indicator lives inside content
        modifier = modifier,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            scope.content()
        }
    }
}
