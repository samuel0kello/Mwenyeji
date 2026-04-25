package com.samuelokello.mwenyeji.feature.feed

import com.samuelokello.mwenyeji.data.helpers.DomainError
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.TimeOfDay

data class FeedState(
    val selectedTimeOfDay: TimeOfDay = TimeOfDay.MORNING_RUSH,
    val searchQuery: String = "",
    val routes: List<Route> = emptyList(),
    val filteredRoutes: List<Route> = emptyList(),
    val userLat: Double? = null,
    val userLng: Double? = null,
    val locationPermissionGranted: Boolean = false,
    val isLoading: Boolean = false,
    val error: DomainError? = null,
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
        val route: Route,
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
