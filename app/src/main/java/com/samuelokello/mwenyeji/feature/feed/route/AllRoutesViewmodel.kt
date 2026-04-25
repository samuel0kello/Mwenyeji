package com.samuelokello.mwenyeji.feature.feed.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.toUserMessage
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.TimeOfDay
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
            is AllRoutesActions.RouteClicked -> onRouteClicked(action.route)
            is AllRoutesActions.RetryClicked -> loadRoutes()
        }
    }

    private fun onRouteClicked(route: Route) {
        viewModelScope.launch {
            _effects.send(AllRoutesEffects.NavigateToRouteDetail(route))
        }
    }

    fun loadRoutes() {
        viewModelScope.launch {
            routeRepository
                .observeRoutes(TimeOfDay.ANYTIME) // all routes, no time filter
                .onStart {
                    _state.update { it.copy(isLoading = true, error = null) }
                }.catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                    _effects.send(AllRoutesEffects.ShowError(e.message ?: "Failed to load routes"))
                }.collect { routes ->
                    when (routes) {
                        is DataResult.Error -> {
                            val userMessage = routes.error.toUserMessage()
                            _state.update { it.copy(isLoading = false, error = userMessage) }
                            _effects.send(AllRoutesEffects.ShowError(userMessage))
                        }

                        is DataResult.Success -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = null,
                                    routes = routes.data,
                                )
                            }
                        }
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
