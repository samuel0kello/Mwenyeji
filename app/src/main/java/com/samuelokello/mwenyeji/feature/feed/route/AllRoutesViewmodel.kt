package com.samuelokello.mwenyeji.feature.feed.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.toUserMessage
import com.samuelokello.mwenyeji.data.models.BoardableRoute
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.TripDirection
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AllRoutesViewModel(
    private val routeRepository: RoutesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AllRoutesState())
    val state: StateFlow<AllRoutesState> = _state.asStateFlow()

    private val _effects = Channel<AllRoutesEffects>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadRoutes()
    }

    fun onAction(action: AllRoutesActions) {
        when (action) {
            is AllRoutesActions.RouteClicked -> onRouteClicked(action.boardableRoute)
            is AllRoutesActions.RetryClicked -> loadRoutes()
        }
    }

    private fun onRouteClicked(boardableRoute: BoardableRoute) {
        viewModelScope.launch {
            _effects.send(AllRoutesEffects.NavigateToRouteDetail(boardableRoute.route))
        }
    }

    fun loadRoutes() {
        viewModelScope.launch {
            routeRepository
                .observeRoutes()
                .onStart { _state.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                    _effects.send(AllRoutesEffects.ShowError(e.message ?: "Failed to load routes"))
                }.collect { result ->
                    when (result) {
                        is DataResult.Error -> {
                            val message = result.error.toUserMessage()
                            _state.update { it.copy(isLoading = false, error = message) }
                            _effects.send(AllRoutesEffects.ShowError(message))
                        }

                        is DataResult.Success -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = null,
                                    routes = result.data.toAllRoutesBoardable(),
                                )
                            }
                        }
                    }
                }
        }
    }
}

/**
 * Wraps raw routes as BoardableRoute without location context.
 * The "See all" screen has no user location — shows routes ordered
 * by confirmedCount (Firestore order) using terminus1 as the display
 * boarding point.
 */
private fun List<Route>.toAllRoutesBoardable(): List<BoardableRoute> =
    map { route ->
        BoardableRoute(
            route = route,
            boardingStop =
                RouteStop(
                    stopId = route.firstStopId ?: "",
                    name = route.from,
                    lat = route.terminus1Lat ?: 0.0,
                    lng = route.terminus1Lng ?: 0.0,
                    sequence = 1,
                ),
            walkingDistanceKm = Double.MAX_VALUE,
            onwardTerminus = route.to,
            stopsRemaining = route.stopCount,
            tripDirection = TripDirection.OUTBOUND,
        )
    }

data class AllRoutesState(
    val isLoading: Boolean = false,
    val routes: List<BoardableRoute> = emptyList(),
    val error: String? = null,
)

sealed interface AllRoutesActions {
    data class RouteClicked(
        val boardableRoute: BoardableRoute,
    ) : AllRoutesActions

    data object RetryClicked : AllRoutesActions
}

sealed interface AllRoutesEffects {
    // Carries Route (not BoardableRoute) — navigation only needs the ID
    data class NavigateToRouteDetail(
        val route: Route,
    ) : AllRoutesEffects

    data class ShowError(
        val message: String,
    ) : AllRoutesEffects
}
