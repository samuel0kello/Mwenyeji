package com.samuelokello.mwenyeji.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.data.repository.RouteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
) : ViewModel() {
    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    private val _effects = Channel<FeedEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadRoutes()
    }

    fun onAction(action: FeedAction) {
        when (action) {
            is FeedAction.SelectTimeOfDay -> onTimeOfDaySelected(action.timeOfDay)
            is FeedAction.SearchQueryChanged -> onSearchQueryChanged(action.query)
            is FeedAction.RouteClicked -> onRouteClicked(action.route)
            is FeedAction.SeeAllClicked -> onSeeAllClicked()
            is FeedAction.RetryClicked -> loadRoutes()
            is FeedAction.RequestLocationPermission -> requestLocation()
            is FeedAction.LocationPermissionResult -> onPermissionResult(action.granted)
            is FeedAction.LocationReceived -> onLocationReceived(action.lat, action.lng)
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
        // permission granted — FeedScreen will now trigger location fetch
        viewModelScope.launch {
            _effects.send(FeedEffect.GetLocation)
        }
    }

    private fun onLocationReceived(lat: Double, lng: Double) {
        _state.update { it.copy(userLat = lat, userLng = lng) }
        // re-sort the feed now that we have location
        sortRoutesByDistance()
    }

    private fun loadRoutes() {
        viewModelScope.launch {
            routeRepository
                .getRoutes(_state.value.selectedTimeOfDay)
                .onStart {
                    _state.update { it.copy(isLoading = true, error = null) }
                }.catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                    _effects.send(FeedEffect.ShowError(e.message ?: "Failed to load routes"))
                }.collect { routes ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            routes = routes,
                            filteredRoutes = routes.filterBySearchQuery(it.searchQuery),
                        )
                    }
                }
        }
    }

    private fun onTimeOfDaySelected(timeOfDay: TimeOfDay) {
        _state.update { it.copy(selectedTimeOfDay = timeOfDay) }
        // Reload from Firestore with new filter
        loadRoutes()
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

    // Search is client-side — Firestore handles time-of-day filtering
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

    private fun sortRoutesByDistance() {
        val lat = _state.value.userLat ?: return
        val lng = _state.value.userLng ?: return

        _state.update { current ->
            val sorted = current.routes.sortedBy { route ->
                haversineDistance(lat, lng, route.fromLat ?: 0.0, route.fromLng ?: 0.0)
            }
            current.copy(
                routes = sorted,
                filteredRoutes = sorted.filterBySearchQuery(current.searchQuery)
            )
        }
    }

    // Haversine formula — straight-line distance between two coordinates in km
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
