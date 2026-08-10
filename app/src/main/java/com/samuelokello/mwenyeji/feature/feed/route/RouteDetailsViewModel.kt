package com.samuelokello.mwenyeji.feature.feed.route

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.core.ml.GuideSuggestionEngine
import com.samuelokello.mwenyeji.core.network.ConnectivityObserver
import com.samuelokello.mwenyeji.core.network.ConnectivityStatus
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.toUserMessage
import com.samuelokello.mwenyeji.data.models.Guide
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStep
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.data.models.TimeOfDay
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
import java.util.Calendar

class RouteDetailsViewModel(
    private val routeRepository: RoutesRepository,
    private val authRepository: AuthRepository,
    private val snackbarManager: SnackBarManager,
    private val suggestionEngine: GuideSuggestionEngine,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {
    private val _state = MutableStateFlow(RouteDetailsState())
    val state: StateFlow<RouteDetailsState> = _state.asStateFlow()

    private val _effects = Channel<RouteDetailsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                _state.update { it.copy(connectivityStatus = status) }
            }
        }
    }

    fun onAction(action: RouteDetailsAction) {
        when (action) {
            is RouteDetailsAction.LoadRoute -> {
                _state.update { it.copy(overrideFrom = action.from, overrideTo = action.to) }
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

            is RouteDetailsAction.GenerateAiSuggestion -> {
                snackbarManager.showInfo("Local guide suggestion is a coming soon feature")
                generateAiSuggestion(_state.value.route, _state.value.routeStops)
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
                            // Load guides and stops once route is confirmed to exist
                            loadGuides(routeId)
                            loadStops(routeId)
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

    private fun loadStops(routeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingStops = true) }
            when (val result = routeRepository.getRouteStops(routeId)) {
                is DataResult.Success -> {
                    _state.update { it.copy(isLoadingStops = false, routeStops = result.data) }
                }

                is DataResult.Error -> {
                    _state.update { it.copy(isLoadingStops = false) }
                }
            }
        }
    }

    private fun generateAiSuggestion(
        route: Route?,
        stops: List<RouteStop>,
    ) {
        if (route == null) {
            Log.e("RouteDetailsVM", "AI Generation: Route is null, aborting")
            return
        }

        val currentState = _state.value
        val effectiveFrom = currentState.overrideFrom ?: route.from
        val effectiveTo = currentState.overrideTo ?: route.to

        // Determine if we are traveling in reverse direction compared to GTFS default
        val isReverse = effectiveFrom == route.to || effectiveTo == route.from
        val startStopId = if (isReverse) route.lastStopId else route.firstStopId

        viewModelScope.launch {
            Log.d("RouteDetailsVM", "AI Generation started for route: ${route.id}, isReverse: $isReverse")
            _state.update { it.copy(isGeneratingAi = true, aiError = null) }

            // Artificial delay for UX visibility of the shimmer
            kotlinx.coroutines.delay(2000)

            try {
                val calendar = Calendar.getInstance()
                val arrivalMinutes = (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)).toFloat()

                Log.d("RouteDetailsVM", "AI Prediction Inputs - stopId: $startStopId, arrivalMinutes: $arrivalMinutes")

                val predictedLabel =
                    suggestionEngine.suggestGuide(
                        stopId = startStopId ?: "",
                        arrivalMinutes = arrivalMinutes,
                        stopSequence = if (isReverse) route.stopCount.toFloat() else 1f,
                    )

                if (predictedLabel != null) {
                    Log.d("RouteDetailsVM", "AI Prediction Success: $predictedLabel")
                    val suggestedGuide = parseAiLabel(predictedLabel, route, stops, effectiveFrom, effectiveTo, isReverse)
                    _state.update { it.copy(suggestedGuide = suggestedGuide, isGeneratingAi = false) }
                } else {
                    Log.w("RouteDetailsVM", "AI Prediction returned null label")
                    _state.update { it.copy(isGeneratingAi = false, aiError = "Unable to generate AI patterns for this route.") }
                }
            } catch (e: Exception) {
                Log.e("RouteDetailsVM", "AI Generation Error", e)
                _state.update { it.copy(isGeneratingAi = false, aiError = e.message ?: "AI Generation failed") }
            }
        }
    }

    private fun parseAiLabel(
        label: String,
        route: Route,
        stops: List<RouteStop>,
        effectiveFrom: String,
        effectiveTo: String,
        isReverse: Boolean,
    ): Guide {
        // Format: "Best at MIDDAY: No specific timing note. | fare≈30 KSh | tags: RELIABLE"
        val parts = label.split("|").map { it.trim() }

        val timingPart = parts.getOrNull(0) ?: ""
        val bestTime = TimeOfDay.entries.find { timingPart.contains(it.name, ignoreCase = true) } ?: TimeOfDay.ANYTIME
        val timingReason = timingPart.substringAfter(":", "").trim()

        val farePart = parts.getOrNull(1) ?: ""
        val fareValue = farePart.filter { it.isDigit() }.toDoubleOrNull()

        val tagsPart = parts.getOrNull(2) ?: ""
        val tags = RouteTag.entries.filter { tagsPart.contains(it.name, ignoreCase = true) }.toSet()

        val steps = mutableListOf<RouteStep>()
        steps.add(RouteStep(1, "Board at $effectiveFrom"))

        val displayStops = if (isReverse) stops.reversed() else stops

        if (displayStops.size > 2) {
            // Include up to 3 intermediate stops if they exist
            val intermediateStops = displayStops.drop(1).dropLast(1).take(3)
            intermediateStops.forEachIndexed { index, stop ->
                steps.add(RouteStep(index + 2, "Pass through ${stop.name}"))
            }
            steps.add(RouteStep(steps.size + 1, "Alight at $effectiveTo"))
        } else {
            steps.add(RouteStep(2, "Tell conductor your destination: $effectiveTo"))
            steps.add(RouteStep(3, "Alight at $effectiveTo"))
        }

        return Guide(
            id = "ai-suggestion",
            routeId = route.id,
            fareKsh = fareValue,
            bestTimeOfDay = bestTime,
            timingReason = timingReason,
            tags = tags,
            contributorId = "mwenyeji-ai",
            steps = steps,
        )
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
    private fun onVerdictSelected(
        guideId: String,
        verdict: RouteVerdict,
    ) {
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
    val overrideFrom: String? = null,
    val overrideTo: String? = null,
    // Guides — loaded after route, real-time
    val isLoadingGuides: Boolean = false,
    val guides: List<Guide> = emptyList(),
    val suggestedGuide: Guide? = null,
    val isGeneratingAi: Boolean = false,
    val aiError: String? = null,
    val isLoadingStops: Boolean = false,
    val routeStops: List<RouteStop> = emptyList(),
    // Verdict — tracks which guide the user has voted on this session
    val selectedGuideId: String? = null,
    val selectedVerdict: RouteVerdict? = null,
    // Connectivity
    val connectivityStatus: ConnectivityStatus = ConnectivityStatus.Available,
)

sealed interface RouteDetailsAction {
    data class LoadRoute(
        val routeId: String,
        val from: String? = null,
        val to: String? = null,
    ) : RouteDetailsAction

    data class VerdictSelected(
        val guideId: String,
        val verdict: RouteVerdict,
    ) : RouteDetailsAction

    data object GenerateAiSuggestion : RouteDetailsAction

    data object NavigateBack : RouteDetailsAction
}

sealed interface RouteDetailsEffect {
    data object NavigateBack : RouteDetailsEffect
}
