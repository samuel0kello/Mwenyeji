package com.samuelokello.mwenyeji.feature.feed.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.feature.feed.FeedEffect
import com.samuelokello.mwenyeji.feature.feed.mockRoutes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AllRoutesViewmodel: ViewModel() {
    private val _state = MutableStateFlow(AllRoutesState())
    val state: StateFlow<AllRoutesState> = _state.asStateFlow()

    private val _effects = Channel<AllRoutesEffects>()
    val effects = _effects.receiveAsFlow()


    init {
        loadRoutes()
    }
    fun onAction(action: AllRoutesActions) {
        when(action){
            is AllRoutesActions.RouteClicked -> onRouteClicked(route = action.route)
        }
    }

    private fun onRouteClicked(route: Route) {
        viewModelScope.launch {
            _effects.send((AllRoutesEffects.NavigateToRouteDetail(route)))
        }
    }

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
//                        filteredRoutes = routes.filterByTimeOfDay(it.selectedTimeOfDay),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Something went wrong")
                }
                _effects.send(AllRoutesEffects.ShowError(e.message ?: "Failed to load routes"))
            }
        }
    }
}

data class AllRoutesState(
    val isLoading: Boolean = false,
    val routes: List<Route> = emptyList(),
    val error: String? = null
)

sealed interface AllRoutesActions {
    data class RouteClicked(val route: Route): AllRoutesActions
}
sealed interface AllRoutesEffects {
    data class NavigateToRouteDetail(val route: Route): AllRoutesEffects
    data class ShowError(val message: String): AllRoutesEffects
}