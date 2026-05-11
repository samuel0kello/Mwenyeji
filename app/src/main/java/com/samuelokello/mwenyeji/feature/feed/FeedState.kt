package com.samuelokello.mwenyeji.feature.feed

import com.samuelokello.mwenyeji.data.helpers.DomainError
import com.samuelokello.mwenyeji.data.models.BoardableRoute
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.TimeOfDay

data class FeedState(
    // Raw GTFS routes from Firestore (full list, unfiltered)
    val routes: List<Route> = emptyList(),
    // Boardable routes derived from user location + stop cache
    // This is what the feed actually displays
    val boardableRoutes: List<BoardableRoute> = emptyList(),
    // Filtered view of boardableRoutes after search + time-of-day
    val filteredRoutes: List<BoardableRoute> = emptyList(),
    // Location state
    val userLat: Double? = null,
    val userLng: Double? = null,
    val locationPermissionGranted: Boolean = false,
    // True while the stop cache is being prefetched after location arrives.
    // Feed shows terminus-based sort during this window, then updates.
    val isRefiningProximity: Boolean = false,
    // Search and filter
    val searchQuery: String = "",
    val selectedTimeOfDay: TimeOfDay = TimeOfDay.MORNING_RUSH,
    // Loading and error
    val isLoading: Boolean = false,
    val error: DomainError? = null,
    // Tooltips
    val showFabTooltip: Boolean = false,
    val showTimeFilterTooltip: Boolean = false,
)

sealed interface FeedAction {
    data class SelectTimeOfDay(
        val timeOfDay: TimeOfDay,
    ) : FeedAction

    data class SearchQueryChanged(
        val query: String,
    ) : FeedAction

    data class RouteClicked(
        val route: BoardableRoute,
    ) : FeedAction

    data object SeeAllClicked : FeedAction

    data object RetryClicked : FeedAction

    data object RequestLocationPermission : FeedAction

    data class LocationPermissionResult(
        val granted: Boolean,
    ) : FeedAction

    data class LocationReceived(
        val lat: Double,
        val lng: Double,
    ) : FeedAction

    data object DismissFabTooltip : FeedAction

    data object DismissTimeFilterTooltip : FeedAction
}

// Effect — one-time events (navigation, toasts)
sealed interface FeedEffect {
    data class NavigateToRouteDetail(
        val route: Route,
    ) : FeedEffect

    data object NavigateToSeeAll : FeedEffect

    data object GetLocation : FeedEffect

    data class ShowError(
        val message: String,
    ) : FeedEffect

    data object RequestLocationPermission : FeedEffect
}
