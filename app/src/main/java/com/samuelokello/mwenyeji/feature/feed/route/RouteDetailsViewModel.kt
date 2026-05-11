package com.samuelokello.mwenyeji.feature.feed.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.toUserMessage
import com.samuelokello.mwenyeji.data.models.Guide
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.Verdict
import com.samuelokello.mwenyeji.data.repository.AuthRepository
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.SnackBarManager
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
    private val routeRepository: RoutesRepository,
    private val authRepository: AuthRepository,
    private val snackbarManager: SnackBarManager,
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
                onVerdictSelected(action.guideId, action.verdict)
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
                .observeRouteById(routeId)
                .onStart { _state.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                    snackbarManager.showError(e.message ?: "Failed to load route")
                }.collect { result ->
                    when (result) {
                        is DataResult.Success -> {
                            _state.update { it.copy(isLoading = false, route = result.data) }
                            // Load guides once route is confirmed to exist
                            loadGuides(routeId)
                        }

                        is DataResult.Error -> {
                            val message = result.error.toUserMessage()
                            _state.update { it.copy(isLoading = false, error = message) }
                            snackbarManager.showError(message)
                        }
                    }
                }
        }
    }

    private fun loadGuides(routeId: String) {
        viewModelScope.launch {
            routeRepository
                .observeGuides(routeId)
                .onStart { _state.update { it.copy(isLoadingGuides = true) } }
                .catch { e ->
                    _state.update { it.copy(isLoadingGuides = false) }
                    snackbarManager.showError(e.message ?: "Failed to load guides")
                }.collect { result ->
                    when (result) {
                        is DataResult.Success -> {
                            _state.update {
                                it.copy(isLoadingGuides = false, guides = result.data)
                            }
                        }

                        is DataResult.Error -> {
                            _state.update { it.copy(isLoadingGuides = false) }
                            snackbarManager.showError(result.error.toUserMessage())
                        }
                    }
                }
        }
    }

    /**
     * Submits a verdict for a specific guide.
     * Tapping the already-selected verdict toggles it off.
     * Uses optimistic update — reverts on failure.
     *
     * Verdicts are currently stored under the parent route's confirmations
     * subcollection. The guideId is embedded in the verdict to distinguish
     * per-guide feedback until guide-level confirmations are implemented.
     */
    private fun onVerdictSelected(guideId: String, verdict: RouteVerdict) {
        val routeId = _state.value.route?.id ?: return
        val userId = authRepository.currentUserId ?: return

        // Toggle off if same verdict tapped again
        val isSameGuide = _state.value.selectedGuideId == guideId
        val isSameVerdict = _state.value.selectedVerdict == verdict
        val newVerdict = if (isSameGuide && isSameVerdict) null else verdict
        val newGuideId = if (newVerdict == null) null else guideId

        // Optimistic update
        _state.update { it.copy(selectedGuideId = newGuideId, selectedVerdict = newVerdict) }

        val firestoreVerdict = newVerdict?.firestoreValue ?: verdict.firestoreValue

        viewModelScope.launch {
            when (
                val result =
                    routeRepository.submitVerdict(
                        routeId = routeId,
                        userId = userId,
                        verdict = Verdict.valueOf(firestoreVerdict),
                    )
            ) {
                is DataResult.Success -> {
                    val message =
                        when (newVerdict) {
                            RouteVerdict.WORKS -> "Thanks for confirming this guide works!"
                            RouteVerdict.DIDNT -> "Thanks — we'll note this guide had issues."
                            RouteVerdict.OUTDATED -> "Thanks! We'll flag this guide as potentially outdated."
                            null -> "Feedback removed."
                        }
                    snackbarManager.showSuccess(message)
                }

                is DataResult.Error -> {
                    // Revert optimistic update
                    _state.update { it.copy(selectedGuideId = null, selectedVerdict = null) }
                    snackbarManager.showError(
                        message = result.error.toUserMessage(),
                        actionLabel = "Retry",
                        onAction = { onVerdictSelected(guideId, verdict) },
                    )
                }
            }
        }
    }
}

data class RouteDetailsState(
    // Route
    val isLoading: Boolean = false,
    val route: Route? = null,
    val error: String? = null,
    // Guides — loaded after route, real-time
    val isLoadingGuides: Boolean = false,
    val guides: List<Guide> = emptyList(),
    // Verdict — tracks which guide the user has voted on this session
    val selectedGuideId: String? = null,
    val selectedVerdict: RouteVerdict? = null,
)

sealed interface RouteDetailsAction {
    data class LoadRoute(
        val routeId: String,
    ) : RouteDetailsAction

    data class VerdictSelected(
        val guideId: String,
        val verdict: RouteVerdict,
    ) : RouteDetailsAction

    data object NavigateBack : RouteDetailsAction
}

sealed interface RouteDetailsEffect {
    data object NavigateBack : RouteDetailsEffect
}
