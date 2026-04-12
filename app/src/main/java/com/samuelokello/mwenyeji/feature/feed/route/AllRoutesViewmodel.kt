package com.samuelokello.mwenyeji.feature.feed.route

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

class AllRoutesViewModel(
    private val routeRepository: RouteRepository,
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
            is AllRoutesActions.RouteClicked -> onRouteClicked(action.route)
            is AllRoutesActions.RetryClicked -> loadRoutes()
        }
    }

    private fun onRouteClicked(route: Route) {
        viewModelScope.launch {
            _effects.send(AllRoutesEffects.NavigateToRouteDetail(route))
        }
    }

    private fun loadRoutes() {
        viewModelScope.launch {
            routeRepository
                .getRoutes(TimeOfDay.ANYTIME) // all routes, no time filter
                .onStart {
                    _state.update { it.copy(isLoading = true, error = null) }
                }.catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                    _effects.send(AllRoutesEffects.ShowError(e.message ?: "Failed to load routes"))
                }.collect { routes ->
                    _state.update {
                        it.copy(isLoading = false, routes = routes)
                    }
                }
        }
    }
}

data class AllRoutesState(
    val isLoading: Boolean = false,
    val routes: List<Route> = emptyList(),
    val error: String? = null,
)

sealed interface AllRoutesActions {
    data class RouteClicked(
        val route: Route,
    ) : AllRoutesActions

    data object RetryClicked : AllRoutesActions
}

sealed interface AllRoutesEffects {
    data class NavigateToRouteDetail(
        val route: Route,
    ) : AllRoutesEffects

    data class ShowError(
        val message: String,
    ) : AllRoutesEffects
}
