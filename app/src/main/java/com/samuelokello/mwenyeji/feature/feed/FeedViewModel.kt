package com.samuelokello.mwenyeji.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.data.repository.RouteRepository
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import com.samuelokello.mwenyeji.ui.designsystem.components.toolTip.TooltipKey
import com.samuelokello.mwenyeji.ui.designsystem.components.toolTip.TooltipManager
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
    private val routeRepository: RouteRepository,
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
            val savedTimeName = prefs.getDefaultTimeOfDay().first()
            val defaultTime =
                TimeOfDay.entries.find { it.name == savedTimeName } ?: TimeOfDay.MORNING_RUSH

            _state.update { it.copy(selectedTimeOfDay = defaultTime) }

            val locationJob = launch { requestLocation() }
            val routesJob = launch { loadRoutes() }

            locationJob.join()
            routesJob.join()

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
                viewModelScope.launch {
                    loadRoutes()
                }
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

    private fun requestLocation() {
        viewModelScope.launch {
            _effects.send(FeedEffect.RequestLocationPermission)
        }
    }

    private fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(locationPermissionGranted = granted) }
        if (!granted) return
        viewModelScope.launch {
            _effects.send(FeedEffect.GetLocation)
        }
    }

    private fun onLocationReceived(lat: Double, lng: Double) {
        _state.update { it.copy(userLat = lat, userLng = lng) }
        // Force a re-sort of existing routes when location is first acquired
        applySortingAndFiltering(_state.value.routes)
    }

    private suspend fun loadRoutes() {
        val selectedTime = _state.value.selectedTimeOfDay

        viewModelScope.launch {
            routeRepository
                .getRoutes(selectedTime)
                .onStart {
                    _state.update { it.copy(isLoading = true, error = null) }
                }.catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                    _effects.send(FeedEffect.ShowError(e.message ?: "Failed to load routes"))
                }.collect { routes ->
                    applySortingAndFiltering(routes)
                }
        }

        // Wait until the first batch of routes is actually loaded into the state
        // This ensures the screen isn't empty when we join() in initializeFeed
        state.first { !it.isLoading && (it.routes.isNotEmpty() || it.error != null) }
    }

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

    /**
     * Core logic: Takes raw routes, sorts them by distance to user,
     * then applies search filters.
     */
    private fun applySortingAndFiltering(routes: List<Route>) {
        val userLat = _state.value.userLat
        val userLng = _state.value.userLng

        // 1. Sort by distance if location is available
        val processedRoutes =
            if (userLat != null && userLng != null) {
                routes.sortedBy { route ->
                    haversineDistance(userLat, userLng, route.fromLat ?: 0.0, route.fromLng ?: 0.0)
                }
            } else {
                routes
            }

        // 2. Update state with both sorted and filtered lists
        _state.update {
            it.copy(
                isLoading = false,
                routes = processedRoutes,
                filteredRoutes = processedRoutes.filterBySearchQuery(it.searchQuery),
            )
        }
    }

    private fun onTimeOfDaySelected(timeOfDay: TimeOfDay) {
        _state.update { it.copy(selectedTimeOfDay = timeOfDay) }
        // Reload from Firestore with new filter
        viewModelScope.launch {
            loadRoutes()
        }
    }

    private fun onSearchQueryChanged(query: String) {
        _state.update { current ->
            current.copy(
                searchQuery = query,
                filteredRoutes = current.routes.filterBySearchQuery(query),
            )
        }
    }

    private fun onRouteClicked(route: Route) {
        viewModelScope.launch {
            _effects.send(FeedEffect.NavigateToRouteDetail(route))
        }
    }

    private fun onSeeAllClicked() {
        viewModelScope.launch {
            _effects.send(FeedEffect.NavigateToSeeAll)
        }
    }

    private fun List<Route>.filterBySearchQuery(query: String): List<Route> =
        if (query.isBlank()) {
            this
        } else {
            val trimmedQuery = query.trim()
            filter { route ->
                route.from.contains(trimmedQuery, ignoreCase = true) ||
                    route.to.contains(trimmedQuery, ignoreCase = true) ||
                    route.via.contains(trimmedQuery, ignoreCase = true)
            }
        }

    private fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
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
