package com.samuelokello.mwenyeji.feature.feed.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.repository.AuthRepository
import com.samuelokello.mwenyeji.data.repository.RouteRepository
import com.samuelokello.mwenyeji.ui.designsystem.components.snackbar.SnackbarManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteDetailsViewModel(
    private val routeRepository: RouteRepository,
    private val authRepository: AuthRepository,
    private val snackbarManager: SnackbarManager,
) : ViewModel() {

    private val _state = MutableStateFlow(RouteDetailsState())
    val state: StateFlow<RouteDetailsState> = _state.asStateFlow()

    private val _effects = Channel<RouteDetailsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onAction(action: RouteDetailsAction) {
        when (action) {
            is RouteDetailsAction.LoadRoute -> loadRoute(action.routeId)
            is RouteDetailsAction.VerdictSelected -> onVerdictSelected(action.verdict)
            is RouteDetailsAction.NavigateBack -> viewModelScope.launch {
                _effects.send(RouteDetailsEffect.NavigateBack)
            }
        }
    }

    private fun loadRoute(routeId: String) {
        viewModelScope.launch {
            routeRepository
                .getRouteById(routeId)
                .onStart { _state.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                    snackbarManager.showError(e.message ?: "Failed to load route")
                }
                .collect { route ->
                    _state.update { it.copy(isLoading = false, route = route) }
                }
        }
    }

    private fun onVerdictSelected(verdict: RouteVerdict) {
        val routeId = _state.value.route?.id ?: return
        val userId = authRepository.currentUserId ?: return

        // Optimistic update
        _state.update { it.copy(selectedVerdict = verdict) }

        viewModelScope.launch {
            routeRepository.confirmRoute(
                routeId = routeId,
                userId = userId,
                verdict = verdict.name,
            ).onSuccess {
                // Show thank you message via snackbar
                val message = when (verdict) {
                    RouteVerdict.WORKS -> "Thanks for confirming this route works! 👍"
                    RouteVerdict.DIDNT -> "Thanks for the feedback — we'll note this route had issues."
                    RouteVerdict.OUTDATED -> "Thanks! We'll flag this route as potentially outdated."
                }
                snackbarManager.showSuccess(message)
            }.onFailure { e ->
                // Revert on failure
                _state.update { it.copy(selectedVerdict = null) }
                snackbarManager.showError(
                    message = e.message ?: "Failed to submit feedback",
                    actionLabel = "Retry",
                    onAction = { onVerdictSelected(verdict) },
                )
            }
        }
    }
}

data class RouteDetailsState(
    val isLoading: Boolean = false,
    val route: Route? = null,
    val selectedVerdict: RouteVerdict? = null,
    val error: String? = null,
)

sealed interface RouteDetailsAction {
    data class LoadRoute(val routeId: String) : RouteDetailsAction
    data class VerdictSelected(val verdict: RouteVerdict) : RouteDetailsAction
    data object NavigateBack : RouteDetailsAction
}

sealed interface RouteDetailsEffect {
    data object NavigateBack : RouteDetailsEffect
}