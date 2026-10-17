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
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import com.samuelokello.mwenyeji.feature.guidance.service.TripRequest
import com.samuelokello.mwenyeji.feature.guidance.service.matches
import com.samuelokello.mwenyeji.presentation.designsystem.components.snackbar.SnackBarManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
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
    private val prefs: MwenyejiPrefs,
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

            is RouteDetailsAction.StartGuide -> startGuide(action)

            is RouteDetailsAction.TraceConsentAnswered -> answerTraceConsent(action.share)

            RouteDetailsAction.TraceConsentDismissed -> _state.update { it.copy(traceConsentFor = null) }

            is RouteDetailsAction.GuidancePermissionDenied -> {
                snackbarManager.showError(action.message)
            }

            is RouteDetailsAction.GenerateAiSuggestion -> {
                snackbarManager.showInfo("Local guide suggestion is a coming soon feature")
                generateAiSuggestion(_state.value.route, _state.value.routeStops)
            }
        }
    }

    /**
     * A guide does not store its stops, so the trip follows the direction the user searched:
     * it ends at the destination and starts at the searched origin, or at the stop nearest the user.
     */
    private fun startGuide(action: RouteDetailsAction.StartGuide) {
        val current = _state.value
        val route = current.route ?: return
        val guide =
            current.guides.firstOrNull { it.id == action.guideId }
                ?: current.suggestedGuide?.takeIf { it.id == action.guideId }
                ?: return
        val request =
            TripRequest.forDirection(
                routeId = route.id,
                routeStops = current.routeStops,
                from = current.overrideFrom ?: route.from,
                to = current.overrideTo ?: route.to,
                labelSaysReverse = route.isReverseFor(current.overrideFrom, current.overrideTo),
                vehicleName = guide.sacco.ifBlank { action.defaultVehicleName },
                fare = guide.fareKsh?.toInt(),
            )
        if (request == null) {
            snackbarManager.showInfo("Stages are still loading. Try again in a moment.")
            return
        }
        viewModelScope.launch {
            // Asked once, the first time a guide is started; the answer is kept from then on.
            if (prefs.getShareRideTraces().first() == null) {
                _state.update { it.copy(traceConsentFor = request) }
            } else {
                _effects.send(RouteDetailsEffect.StartGuidance(request))
            }
        }
    }

    private fun answerTraceConsent(share: Boolean) {
        val request = _state.value.traceConsentFor ?: return
        _state.update { it.copy(traceConsentFor = null) }
        viewModelScope.launch {
            prefs.setShareRideTraces(share)
            _effects.send(RouteDetailsEffect.StartGuidance(request))
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

        // Which way along the stored stops the trip runs: from the stops themselves when the names match,
        // because many routes store their stops the other way round from their from/to label.
        val isReverse =
            TripRequest.runsReversed(stops, effectiveFrom, effectiveTo)
                ?: route.isReverseFor(currentState.overrideFrom, currentState.overrideTo)
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

        // Only the stops between boarding and alighting, in the direction of travel.
        val ride = ridePortion(if (isReverse) stops.reversed() else stops, effectiveFrom, effectiveTo)

        if (ride.size > 2) {
            // Up to 3 landmarks spread along the ride, so a long ride is not a list of every stop.
            landmarks(ride.drop(1).dropLast(1), count = 3).forEach { stop ->
                steps.add(RouteStep(steps.size + 1, "Pass through ${stop.name}"))
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
    // The trip waiting on the ride-sharing question, shown as a dialog while not null.
    val traceConsentFor: TripRequest? = null,
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

    /** @param defaultVehicleName Spoken when the guide names no sacco. */
    data class StartGuide(
        val guideId: String,
        val defaultVehicleName: String,
    ) : RouteDetailsAction

    /** @param share Whether the rider lets Mwenyeji learn from the path of their rides. */
    data class TraceConsentAnswered(
        val share: Boolean,
    ) : RouteDetailsAction

    /** Closed without answering: nothing starts, and the question comes back next time. */
    data object TraceConsentDismissed : RouteDetailsAction

    data class GuidancePermissionDenied(
        val message: String,
    ) : RouteDetailsAction

    data object NavigateBack : RouteDetailsAction
}

sealed interface RouteDetailsEffect {
    data object NavigateBack : RouteDetailsEffect

    data class StartGuidance(
        val request: TripRequest,
    ) : RouteDetailsEffect
}

/** The stops from [from] to [to] in [stops] (already in travel order), or all of them when the names do not match. */
private fun ridePortion(
    stops: List<RouteStop>,
    from: String,
    to: String,
): List<RouteStop> {
    val start = stops.indexOfFirst { it.matches(from) }.takeIf { it >= 0 } ?: 0
    val end = stops.indexOfLast { it.matches(to) }.takeIf { it > start } ?: stops.lastIndex
    return stops.subList(start, end + 1)
}

/** [count] stops spread evenly through [stops]. */
private fun landmarks(
    stops: List<RouteStop>,
    count: Int,
): List<RouteStop> {
    if (stops.size <= count) return stops
    return (1..count).map { stops[it * stops.size / (count + 1)] }
}

/**
 * The route's from/to label read as a direction: true when the trip runs from the label's "to" towards
 * its "from". Only a fallback: many routes store their stops the other way round from their label.
 */
private fun Route.isReverseFor(
    overrideFrom: String?,
    overrideTo: String?,
): Boolean {
    val from = overrideFrom ?: this.from
    val to = overrideTo ?: this.to
    return from == this.to || to == this.from
}
