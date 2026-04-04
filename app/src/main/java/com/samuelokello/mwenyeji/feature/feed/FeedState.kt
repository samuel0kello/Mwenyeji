package com.samuelokello.mwenyeji.feature.feed

import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.TimeOfDay


// State — what the UI render
data class FeedState(
    val isLoading: Boolean = false,
    val routes: List<Route> = emptyList(),
    val filteredRoutes: List<Route> = emptyList(),
    val selectedTimeOfDay: TimeOfDay = TimeOfDay.MORNING_RUSH,
    val searchQuery: String = "",
    val error: String? = null,
)

// Intent — what the user can do
sealed interface FeedIntent {
    data class SelectTimeOfDay(val timeOfDay: TimeOfDay) : FeedIntent
    data class SearchQueryChanged(val query: String) : FeedIntent
    data class RouteClicked(val route: Route) : FeedIntent
    data object SeeAllClicked : FeedIntent
    data object RetryClicked : FeedIntent
}


// Effect — one-time events (navigation, toasts)
sealed interface FeedEffect {
    data class NavigateToRouteDetail(val route: Route) : FeedEffect
    data object NavigateToSeeAll : FeedEffect
    data class ShowError(val message: String) : FeedEffect
}