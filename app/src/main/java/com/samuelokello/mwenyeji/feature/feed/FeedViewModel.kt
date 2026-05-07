package com.samuelokello.mwenyeji.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.DomainError
import com.samuelokello.mwenyeji.data.helpers.toUserMessage
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import com.samuelokello.mwenyeji.presentation.designsystem.components.toolTip.TooltipKey
import com.samuelokello.mwenyeji.presentation.designsystem.components.toolTip.TooltipManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class FeedViewModel(
    private val routeRepository: RoutesRepository,
    private val prefs: MwenyejiPrefs,
    private val tooltipManager: TooltipManager,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    private val _effects = Channel<FeedEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        initializeFeed()
    }

    private fun initializeFeed() {
        viewModelScope.launch {
            // Read saved time-of-day preference from onboarding personalization
            val savedTimeName = prefs.getDefaultTimeOfDay().first()
            val defaultTime =
                TimeOfDay.entries.firstOrNull { it.name == savedTimeName }
                    ?: TimeOfDay.MORNING_RUSH

            _state.update { it.copy(selectedTimeOfDay = defaultTime) }

            // Request location and load routes concurrently
            launch { requestLocation() }
            launch { loadRoutes() }

            observeTooltips()
        }
    }

    fun onAction(action: FeedAction) {
        when (action) {
            is FeedAction.SelectTimeOfDay -> {
                onTimeOfDaySelected(action.timeOfDay)
            }

            is FeedAction.SearchQueryChanged -> {
                onSearchQueryChanged(action.query)
            }

            is FeedAction.RouteClicked -> {
                onRouteClicked(action.route)
            }

            is FeedAction.SeeAllClicked -> {
                onSeeAllClicked()
            }

            is FeedAction.RetryClicked -> {
                viewModelScope.launch { loadRoutes() }
            }

            is FeedAction.RequestLocationPermission -> {
                requestLocation()
            }

            is FeedAction.LocationPermissionResult -> {
                onPermissionResult(action.granted)
            }

            is FeedAction.LocationReceived -> {
                onLocationReceived(action.lat, action.lng)
            }

            is FeedAction.DismissFabTooltip -> {
                viewModelScope.launch {
                    tooltipManager.markShown(TooltipKey.FAB_CONTRIBUTE)
                }
            }

            is FeedAction.DismissTimeFilterTooltip -> {
                viewModelScope.launch {
                    tooltipManager.markShown(TooltipKey.TIME_OF_DAY_CHIPS)
                }
            }
        }
    }

    private suspend fun loadRoutes() {
        routeRepository
            .observeRoutes()
            .onStart { _state.update { it.copy(isLoading = true, error = null) } }
            .catch { e ->
                val error = DomainError.Unknown(e.message ?: "Unknown error")
                _state.update { it.copy(isLoading = false, error = error) }
                _effects.send(FeedEffect.ShowError(e.message ?: "Failed to load routes"))
            }.collect { result ->
                when (result) {
                    is DataResult.Success -> {
                        applyFiltersAndSort(result.data)
                    }

                    is DataResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.error) }
                        _effects.send(FeedEffect.ShowError(result.error.toUserMessage()))
                    }
                }
            }
    }

    /**
     * Single function that owns all filtering and sorting logic.
     * Called whenever routes, location, search query, or time-of-day changes.
     *
     * Order of operations:
     *   1. Sort by proximity to user (haversine to nearest terminus)
     *   2. Filter by selected time of day
     *   3. Filter by search query
     */
    private fun applyFiltersAndSort(routes: List<Route>) {
        val current = _state.value
        val userLat = current.userLat
        val userLng = current.userLng

        // Step 1 — proximity sort
        val sorted =
            if (userLat != null && userLng != null) {
                routes.sortedBy { route ->
                    // Use whichever terminus is closer to the user
                    val dist1 =
                        if (route.fromLat != null && route.fromLng != null) {
                            haversineKm(userLat, userLng, route.fromLat, route.fromLng)
                        } else {
                            Double.MAX_VALUE
                        }

                    val dist2 =
                        if (route.toLat != null && route.toLng != null) {
                            haversineKm(userLat, userLng, route.toLat, route.toLng)
                        } else {
                            Double.MAX_VALUE
                        }

                    minOf(dist1, dist2)
                }
            } else {
                // No location yet — sort by confirmedCount (already ordered by Firestore)
                routes
            }

        // Step 2 — time of day filter
        val timeFiltered =
            sorted.filter { route ->
                current.selectedTimeOfDay == TimeOfDay.ANYTIME ||
                    route.bestTimeOfDay == TimeOfDay.ANYTIME ||
                    route.bestTimeOfDay == current.selectedTimeOfDay
            }

        // Step 3 — search filter
        val searchFiltered = timeFiltered.filterBySearch(current.searchQuery)

        _state.update {
            it.copy(
                isLoading = false,
                routes = sorted, // full sorted list (no search/time filter)
                filteredRoutes = searchFiltered, // what the feed actually shows
            )
        }
    }

    private fun onTimeOfDaySelected(timeOfDay: TimeOfDay) {
        _state.update { it.copy(selectedTimeOfDay = timeOfDay) }
        // Re-apply filters on the already-loaded routes — no Firestore call needed
        applyFiltersAndSort(_state.value.routes)
    }

    private fun onSearchQueryChanged(query: String) {
        _state.update { current ->
            current.copy(
                searchQuery = query,
                filteredRoutes =
                    current.routes
                        .filter { route ->
                            current.selectedTimeOfDay == TimeOfDay.ANYTIME ||
                                route.bestTimeOfDay == TimeOfDay.ANYTIME ||
                                route.bestTimeOfDay == current.selectedTimeOfDay
                        }.filterBySearch(query),
            )
        }
    }

    private fun requestLocation() {
        viewModelScope.launch {
            _effects.send(FeedEffect.RequestLocationPermission)
        }
    }

    private fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(locationPermissionGranted = granted) }
        if (granted) viewModelScope.launch { _effects.send(FeedEffect.GetLocation) }
    }

    private fun onLocationReceived(lat: Double, lng: Double) {
        _state.update { it.copy(userLat = lat, userLng = lng) }
        // Re-sort existing routes now that we have location
        applyFiltersAndSort(_state.value.routes)
    }

    // Navigation
    private fun onRouteClicked(route: Route) {
        viewModelScope.launch { _effects.send(FeedEffect.NavigateToRouteDetail(route)) }
    }

    private fun onSeeAllClicked() {
        viewModelScope.launch { _effects.send(FeedEffect.NavigateToSeeAll) }
    }

    // Tooltips

    private fun observeTooltips() {
        viewModelScope.launch {
            tooltipManager.shouldShow(TooltipKey.FAB_CONTRIBUTE).collect { show ->
                _state.update { it.copy(showFabTooltip = show) }
            }
        }
        viewModelScope.launch {
            tooltipManager.shouldShow(TooltipKey.TIME_OF_DAY_CHIPS).collect { show ->
                _state.update { it.copy(showTimeFilterTooltip = show) }
            }
        }
    }

    private fun List<Route>.filterBySearch(query: String): List<Route> {
        if (query.isBlank()) return this
        val q = query.trim().lowercase()
        return filter { route ->
            // Check searchTerms first (GTFS routes) O(1) lookup
            route.searchTerms.any { it.contains(q) } ||
                // Fall back to field-level search (community routes)
                route.from.contains(q, ignoreCase = true) ||
                route.to.contains(q, ignoreCase = true) ||
                route.via.contains(q, ignoreCase = true) ||
                route.routeNumber?.contains(q, ignoreCase = true) == true
        }
    }

    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a =
            sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        return r * 2 * asin(sqrt(a))
    }
}
