package com.samuelokello.mwenyeji.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.filterBy
import com.samuelokello.mwenyeji.data.helpers.sortedByProximity
import com.samuelokello.mwenyeji.data.helpers.toUserMessage
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import com.samuelokello.mwenyeji.ui.designsystem.components.toolTip.TooltipKey
import com.samuelokello.mwenyeji.ui.designsystem.components.toolTip.TooltipManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(
    private val routesRepository: RoutesRepository,
    private val prefs: MwenyejiPrefs,
    private val tooltipManager: TooltipManager,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    private val _effects = Channel<FeedEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var routesJob: Job? = null

    init {
        viewModelScope.launch {
            val savedTime = prefs.getDefaultTimeOfDay().first()
            val initial =
                runCatching { TimeOfDay.valueOf(savedTime ?: "") }
                    .getOrDefault(TimeOfDay.MORNING_RUSH)
            _state.update { it.copy(selectedTimeOfDay = initial) }

            observeRoutes(initial)
            observeTooltips()
            requestLocation()
        }
    }

    fun onAction(action: FeedAction) {
        when (action) {
            is FeedAction.SelectTimeOfDay -> selectTimeOfDay(action.timeOfDay)
            is FeedAction.SearchQueryChanged -> updateSearch(action.query)
            is FeedAction.RouteClicked -> emitEffect(FeedEffect.NavigateToRouteDetail(action.route))
            is FeedAction.SeeAllClicked -> emitEffect(FeedEffect.NavigateToSeeAll)
            is FeedAction.RetryClicked -> observeRoutes(_state.value.selectedTimeOfDay)
            is FeedAction.RequestLocationPermission -> requestLocation()
            is FeedAction.LocationPermissionResult -> onPermissionResult(action.granted)
            is FeedAction.LocationReceived -> onLocationReceived(action.lat, action.lng)
            FeedAction.DismissFabTooltip -> dismissTooltip(TooltipKey.FAB_CONTRIBUTE)
            FeedAction.DismissTimeFilterTooltip -> dismissTooltip(TooltipKey.TIME_OF_DAY_CHIPS)
        }
    }

    // ---- routes ----

    private fun observeRoutes(timeOfDay: TimeOfDay) {
        routesJob?.cancel() // <-- the key fix: cancel the previous collector
        routesJob =
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, error = null) }
                routesRepository.observeRoutes(timeOfDay).collect { result ->
                    when (result) {
                        is DataResult.Success -> {
                            _state.update { current ->
                                val sorted = result.data.sortedByProximity(current.userLat, current.userLng)
                                current.copy(
                                    isLoading = false,
                                    error = null,
                                    routes = sorted,
                                    filteredRoutes = sorted.filterBy(current.searchQuery),
                                )
                            }
                        }

                        is DataResult.Error -> {
                            _state.update { it.copy(isLoading = false, error = result.error) }
                            _effects.send(FeedEffect.ShowError(result.error.toUserMessage()))
                        }
                    }
                }
            }
    }

    private fun selectTimeOfDay(timeOfDay: TimeOfDay) {
        if (_state.value.selectedTimeOfDay == timeOfDay) return
        _state.update { it.copy(selectedTimeOfDay = timeOfDay) }
        observeRoutes(timeOfDay)
    }

    private fun updateSearch(query: String) {
        _state.update { current ->
            current.copy(
                searchQuery = query,
                filteredRoutes = current.routes.filterBy(query),
            )
        }
    }

    // ---- location ----

    private fun requestLocation() {
        viewModelScope.launch { _effects.send(FeedEffect.RequestLocationPermission) }
    }

    private fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(locationPermissionGranted = granted) }
        if (granted) {
            viewModelScope.launch { _effects.send(FeedEffect.GetLocation) }
        }
    }

    private fun onLocationReceived(lat: Double, lng: Double) {
        _state.update { current ->
            val sorted = current.routes.sortedByProximity(lat, lng)
            current.copy(
                userLat = lat,
                userLng = lng,
                routes = sorted,
                filteredRoutes = sorted.filterBy(current.searchQuery),
            )
        }
    }

    private fun observeTooltips() {
        viewModelScope.launch {
            tooltipManager.shouldShow(TooltipKey.FAB_CONTRIBUTE).collect { show ->
                _state.update { it.copy(showFabTooltip = show) }
            }
        }
        viewModelScope.launch {
            tooltipManager.shouldShow(TooltipKey.TIME_OF_DAY_CHIPS).collect { show ->
                _state.update { it.copy(showTimeFilterTooltip = show) }
            }
        }
    }

    private fun dismissTooltip(key: TooltipKey) {
        viewModelScope.launch { tooltipManager.markShown(key) }
    }

    // ---- utilities ----

    private fun emitEffect(effect: FeedEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
