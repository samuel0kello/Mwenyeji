package com.samuelokello.mwenyeji.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStep
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    // Channel for one-time effects (navigation, toasts)
    private val _effects = Channel<FeedEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadRoutes()
    }

    // Intent handler

    fun onIntent(intent: FeedIntent) {
        when (intent) {
            is FeedIntent.SelectTimeOfDay    -> onTimeOfDaySelected(intent.timeOfDay)
            is FeedIntent.SearchQueryChanged -> onSearchQueryChanged(intent.query)
            is FeedIntent.RouteClicked       -> onRouteClicked(intent.route)
            is FeedIntent.SeeAllClicked      -> onSeeAllClicked()
            is FeedIntent.RetryClicked       -> loadRoutes()
        }
    }

    // Private handlers

    private fun loadRoutes() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // TODO: replace with real repository call
                // val routes = routeRepository.getRoutes()
                val routes = mockRoutes()
                _state.update {
                    it.copy(
                        isLoading = false,
                        routes = routes,
                        filteredRoutes = routes.filterByTimeOfDay(it.selectedTimeOfDay),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Something went wrong")
                }
                _effects.send(FeedEffect.ShowError(e.message ?: "Failed to load routes"))
            }
        }
    }

    private fun onTimeOfDaySelected(timeOfDay: TimeOfDay) {
        _state.update { current ->
            current.copy(
                selectedTimeOfDay = timeOfDay,
                filteredRoutes = current.routes.filterByTimeOfDay(timeOfDay),
            )
        }
    }

    private fun onSearchQueryChanged(query: String) {
        _state.update { current ->
            current.copy(
                searchQuery = query,
                filteredRoutes = if (query.isBlank()) {
                    current.routes.filterByTimeOfDay(current.selectedTimeOfDay)
                } else {
                    current.routes.filter { route ->
                        route.from.contains(query, ignoreCase = true) ||
                        route.to.contains(query, ignoreCase = true) ||
                        route.via.contains(query, ignoreCase = true)
                    }
                },
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

    //  Helpers

    private fun List<Route>.filterByTimeOfDay(timeOfDay: TimeOfDay): List<Route> =
        if (timeOfDay == TimeOfDay.ANYTIME) this
        else filter { it.bestTimeOfDay == timeOfDay || it.bestTimeOfDay == TimeOfDay.ANYTIME }
}

// Mock data — replace with repository
fun mockRoutes() = listOf(
    Route(
        id = "1",
        from = "CBD",
        to = "Westlands",
        via = "via Uhuru Highway",
        fareKsh = 50.0,
        bestTimeOfDay = TimeOfDay.MORNING_RUSH,
        steps = listOf(
            RouteStep(
                1,
                "Board at Kencom, avoid Archives matatus during rush. Quick connection at Westlands roundabout."
            ),
            RouteStep(2, "Tell conductor 'Westlands roundabout' so you don't miss the stop."),
        ),
        tags = setOf(RouteTag.FAST),
        confirmedCount = 47,
        lastConfirmedAt = System.currentTimeMillis() - 7_200_000L,
    ),
    Route(
        id = "2",
        from = "CBD",
        to = "Westlands",
        via = "via Ngara shortcut",
        fareKsh = 40.0,
        bestTimeOfDay = TimeOfDay.MIDDAY,
        steps = listOf(
            RouteStep(1, "Less known route through Ngara. Cheaper but needs one connection. Works great midday."),
        ),
        tags = setOf(RouteTag.CHEAP, RouteTag.LESS_CROWDED),
        confirmedCount = 12,
        lastConfirmedAt = System.currentTimeMillis() - 18_000_000L,
    ),
    Route(
        id = "3",
        from = "CBD",
        to = "Eastleigh",
        via = "along River Road",
        fareKsh = 50.0,
        bestTimeOfDay = TimeOfDay.MORNING_RUSH,
        steps = listOf(
            RouteStep(1, "Walk to University Way first, catch from there. Skips worst of CBD chaos."),
        ),
        tags = setOf(RouteTag.RELIABLE),
        confirmedCount = 8,
        lastConfirmedAt = System.currentTimeMillis() - 86_400_000L,
    ),
)