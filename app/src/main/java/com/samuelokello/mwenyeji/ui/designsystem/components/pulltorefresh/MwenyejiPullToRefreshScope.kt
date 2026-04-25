package com.samuelokello.mwenyeji.ui.designsystem.components.pulltorefresh

import androidx.compose.foundation.lazy.LazyListScope

/**
 * Scope exposed to the content lambda of [MwenyejiPullToRefresh].
 * Provides access to indicator placement helpers.
 */
interface MwenyejiPullToRefreshScope {
    /** Current pull progress, 0f at rest, 1f at threshold/refreshing. */
    val pullProgress: Float

    /** Whether a refresh is currently in progress. */
    val isRefreshing: Boolean

    /** Place the pull indicator as the first item in a [LazyListScope]. */
    fun LazyListScope.indicatorItem()
}

internal class MwenyejiPullToRefreshScopeImpl(
    override val pullProgress: Float,
    override val isRefreshing: Boolean,
    private val statusText: String,
) : MwenyejiPullToRefreshScope {
    override fun LazyListScope.indicatorItem() {
        item(key = "mwenyeji_pull_indicator") {
            MwenyejiPullIndicator(
                pullProgress = pullProgress,
                isRefreshing = isRefreshing,
                statusText = statusText,
            )
        }
    }
}
