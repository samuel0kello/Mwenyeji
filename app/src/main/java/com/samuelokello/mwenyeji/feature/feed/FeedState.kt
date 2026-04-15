package com.samuelokello.mwenyeji.feature.feed

import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.TimeOfDay

data class FeedState(
    val isLoading: Boolean = false,
    val routes: List<Route> = emptyList(),
    val filteredRoutes: List<Route> = emptyList(),
    val selectedTimeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val searchQuery: String = "",
    val error: String? = null,
    val showContributeSheet: Boolean = false,
    val locationPermissionGranted: Boolean = false,
    val userLat: Double? = null,
    val userLng: Double? = null,
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
