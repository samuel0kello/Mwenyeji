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

    fun onIntent(intent: FeedIntent) {
        when (intent) {
            is FeedIntent.SelectTimeOfDay    -> onTimeOfDaySelected(intent.timeOfDay)
            is FeedIntent.SearchQueryChanged -> onSearchQueryChanged(intent.query)
            is FeedIntent.RouteClicked       -> onRouteClicked(intent.route)
            is FeedIntent.SeeAllClicked      -> onSeeAllClicked()
            is FeedIntent.RetryClicked       -> loadRoutes()
        }
    }

    private fun loadRoutes() {
        viewModelScope.launch {
            routeRepository
                .getRoutes(_state.value.selectedTimeOfDay)
                .onStart {
                    _state.update { it.copy(isLoading = true, error = null) }
                }
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                    _effects.send(FeedEffect.ShowError(e.message ?: "Failed to load routes"))
                }
                .collect { routes ->
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
        if (query.isBlank()) this
        else filter { route ->
            route.from.contains(query, ignoreCase = true) ||
                    route.to.contains(query, ignoreCase = true) ||
                    route.via.contains(query, ignoreCase = true)
        }
}