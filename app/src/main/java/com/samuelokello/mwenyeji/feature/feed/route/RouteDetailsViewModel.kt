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
            is RouteDetailsAction.LoadRoute -> {
                loadRoute(action.routeId)
            }

            is RouteDetailsAction.VerdictSelected -> {
                onVerdictSelected(action.verdict)
            }

            is RouteDetailsAction.NavigateBack -> {
                viewModelScope.launch {
                    _effects.send(RouteDetailsEffect.NavigateBack)
                }
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
                }.collect { route ->
                    _state.update { it.copy(isLoading = false, route = route) }
                    // Load user's existing verdict once route is loaded
                    loadUserVerdict(routeId)
                }
        }
    }

    private fun loadUserVerdict(routeId: String) {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            val firestoreVerdict = routeRepository.getUserVerdict(routeId, userId)
            // Map the Firestore string back to the enum
            val verdict =
                RouteVerdict.entries.find {
                    it.firestoreValue == firestoreVerdict
                }
            _state.update { it.copy(selectedVerdict = verdict) }
        }
    }

    private fun onVerdictSelected(verdict: RouteVerdict) {
        val routeId = _state.value.route?.id ?: return
        val userId = authRepository.currentUserId ?: return

        // If tapping the already-selected verdict, toggle it off
        val newVerdict = if (_state.value.selectedVerdict == verdict) null else verdict

        // Optimistic update
        _state.update { it.copy(selectedVerdict = newVerdict) }

        viewModelScope.launch {
            val firestoreVerdict =
                newVerdict?.firestoreValue
                    ?: _state.value.selectedVerdict?.firestoreValue
                    ?: return@launch

            routeRepository
                .confirmRoute(
                    routeId = routeId,
                    userId = userId,
                    verdict = firestoreVerdict, // ← now passes "CONFIRMED" not "WORKS"
                ).onSuccess {
                    val message =
                        when (newVerdict) {
                            RouteVerdict.WORKS -> "Thanks for confirming this route works! 👍"
                            RouteVerdict.DIDNT -> "Thanks for the feedback — we'll note this route had issues."
                            RouteVerdict.OUTDATED -> "Thanks! We'll flag this route as potentially outdated."
                            null -> "Feedback removed."
                        }
                    snackbarManager.showSuccess(message)
                }.onFailure { e ->
                    // Revert optimistic update on failure
                    _state.update { it.copy(selectedVerdict = _state.value.selectedVerdict) }
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
    data class LoadRoute(
        val routeId: String,
    ) : RouteDetailsAction

    data class VerdictSelected(
        val verdict: RouteVerdict,
    ) : RouteDetailsAction

    data object NavigateBack : RouteDetailsAction
}

sealed interface RouteDetailsEffect {
    data object NavigateBack : RouteDetailsEffect
}
