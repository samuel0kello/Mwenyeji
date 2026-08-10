package com.samuelokello.mwenyeji.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.core.ml.GuideSuggestionEngine
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.DomainError
import com.samuelokello.mwenyeji.data.helpers.ProximityEngine
import com.samuelokello.mwenyeji.data.helpers.toUserMessage
import com.samuelokello.mwenyeji.data.models.BoardableRoute
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStop
import com.samuelokello.mwenyeji.data.models.TimeOfDay
import com.samuelokello.mwenyeji.data.models.TripDirection
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import com.samuelokello.mwenyeji.presentation.designsystem.components.toolTip.TooltipKey
import com.samuelokello.mwenyeji.presentation.designsystem.components.toolTip.TooltipManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

class FeedViewModel(
    private val routeRepository: RoutesRepository,
    private val prefs: MwenyejiPrefs,
    private val tooltipManager: TooltipManager,
    private val suggestionEngine: GuideSuggestionEngine,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    private val _effects = Channel<FeedEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * Stop list cache: routeId → outbound ordered stops.
     * Populated once after location is received.
     * ProximityEngine uses all intermediate stops — not just termini —
     * so a route passing through the user's location ranks correctly.
     */
    private val routeStopsCache = mutableMapOf<String, List<RouteStop>>()

    init {
        initializeFeed()
    }

    private fun initializeFeed() {
        viewModelScope.launch {
            // Time-of-day preference drives the chip default.
            // The actual feed no longer filters by time at the route level —
            // bestTimeOfDay lives on Guide now, not Route.
            // The chip selection is preserved in state for when guide-level
            // filtering is implemented on the route detail screen.
            val savedTimeName = prefs.getDefaultTimeOfDay().first()
            val defaultTime =
                TimeOfDay.entries.firstOrNull { it.name == savedTimeName }
                    ?: TimeOfDay.MORNING_RUSH
            _state.update { it.copy(selectedTimeOfDay = defaultTime) }

            launch { requestLocation() }
            launch { loadRoutes() }
            observeTooltips()
        }
    }

    fun onAction(action: FeedAction) {
        when (action) {
            is FeedAction.SelectTimeOfDay -> {
                onTimeOfDaySelected(action.timeOfDay)
            }

            is FeedAction.SearchQueryChanged -> {
                onSearchQueryChanged(action.query)
            }

            is FeedAction.RouteClicked -> {
                onRouteClicked(action.route)
            }

            is FeedAction.SeeAllClicked -> {
                onSeeAllClicked()
            }

            is FeedAction.RetryClicked -> {
                viewModelScope.launch { loadRoutes() }
            }

            is FeedAction.RequestLocationPermission -> {
                requestLocation()
            }

            is FeedAction.LocationPermissionResult -> {
                onPermissionResult(action.granted)
            }

            is FeedAction.LocationReceived -> {
                onLocationReceived(action.lat, action.lng)
            }

            is FeedAction.DismissFabTooltip -> {
                viewModelScope.launch {
                    tooltipManager.markShown(TooltipKey.FAB_CONTRIBUTE)
                }
            }

            is FeedAction.DismissTimeFilterTooltip -> {
                viewModelScope.launch {
                    tooltipManager.markShown(TooltipKey.TIME_OF_DAY_CHIPS)
                }
            }
        }
    }

    // ── Route loading ─────────────────────────────────────────────────────────

    private fun loadRoutes() {
        viewModelScope.launch {
            routeRepository
                .observeRoutes()
                .onStart { _state.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    val error = DomainError.Unknown(e.message ?: "Unknown error")
                    _state.update { it.copy(isLoading = false, error = error) }
                    _effects.send(FeedEffect.ShowError(e.message ?: "Failed to load routes"))
                }.collect { result ->
                    when (result) {
                        is DataResult.Success -> {
                            onRoutesLoaded(result.data)
                        }

                        is DataResult.Error -> {
                            _state.update { it.copy(isLoading = false, error = result.error) }
                            _effects.send(FeedEffect.ShowError(result.error.toUserMessage()))
                        }
                    }
                }
        }
    }

    private fun onRoutesLoaded(routes: List<Route>) {
        // All routes from Firestore are GTFS routes — no source filter needed.
        _state.update { it.copy(routes = routes, isLoading = false) }
        recomputeBoardable()
    }

    // ── Stop cache prefetch ───────────────────────────────────────────────────

    private suspend fun prefetchRouteStops(routes: List<Route>) {
        val uncached = routes.filter { it.id !in routeStopsCache }
        if (uncached.isEmpty()) return

        _state.update { it.copy(isRefiningProximity = true) }

        uncached.chunked(20).forEach { batch ->
            batch
                .map { route ->
                    viewModelScope.async {
                        when (val result = routeRepository.getRouteStops(route.id)) {
                            is DataResult.Success -> {
                                if (result.data.isNotEmpty()) {
                                    routeStopsCache[route.id] = result.data
                                }
                            }

                            is DataResult.Error -> {
                                Unit
                            }
                        }
                    }
                }.awaitAll()
        }

        _state.update { it.copy(isRefiningProximity = false) }
    }

    // ── Boardable route computation ───────────────────────────────────────────

    private fun recomputeBoardable() {
        val current = _state.value
        val userLat = current.userLat
        val userLng = current.userLng
        val routes = current.routes

        val boardable =
            if (userLat != null && userLng != null) {
                ProximityEngine.computeBoardable(
                    routes = routes,
                    stopsCache = routeStopsCache,
                    userLat = userLat,
                    userLng = userLng,
                )
            } else {
                // No location yet — wrap all routes using terminus1 as the boarding point.
                // Sorted by confirmedCount (Firestore order).
                routes.map { route ->
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
            }

        val filtered = boardable.filterBySearch(current.searchQuery)

        _state.update {
            it.copy(
                boardableRoutes = boardable,
                filteredRoutes = filtered,
            )
        }

        generateSuggestion()
    }

    private fun generateSuggestion() {
        val current = _state.value
        val nearestRoute = current.boardableRoutes.firstOrNull { it.walkingDistanceKm < 0.5 } ?: return

        val calendar = Calendar.getInstance()
        val arrivalMinutes = (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)).toFloat()

        val suggestedRouteId =
            suggestionEngine.suggestGuide(
                stopId = nearestRoute.boardingStop.stopId,
                arrivalMinutes = arrivalMinutes,
                stopSequence = nearestRoute.boardingStop.sequence.toFloat(),
            )

        val suggestedBoardable = current.boardableRoutes.find { it.route.id == suggestedRouteId }
        _state.update { it.copy(suggestedRoute = suggestedBoardable) }
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

    /**
     * Feed filtering is search-only.
     *
     * Time-of-day is no longer a route-level field — it lives on Guide.
     * The chip selection is persisted in state and will be used when the
     * route detail screen filters guides by time of day.
     */
    private fun List<BoardableRoute>.filterBySearch(query: String): List<BoardableRoute> {
        if (query.isBlank()) return this
        val q = query.trim().lowercase()
        return filter { br ->
            val route = br.route
            route.searchTerms.any { it.contains(q) } ||
                route.from.contains(q, ignoreCase = true) ||
                route.to.contains(q, ignoreCase = true) ||
                route.via.contains(q, ignoreCase = true) ||
                route.routeNumber?.contains(q, ignoreCase = true) == true ||
                br.boardingStop.name.contains(q, ignoreCase = true) ||
                br.onwardTerminus.contains(q, ignoreCase = true)
        }
    }

    private fun onTimeOfDaySelected(timeOfDay: TimeOfDay) {
        // Persists selection for route detail guide filtering.
        // Does not re-filter the feed — time is a guide-level field.
        _state.update { it.copy(selectedTimeOfDay = timeOfDay) }
    }

    private fun onSearchQueryChanged(query: String) {
        _state.update { current ->
            current.copy(
                searchQuery = query,
                filteredRoutes = current.boardableRoutes.filterBySearch(query),
            )
        }
    }

    // ── Location ──────────────────────────────────────────────────────────────

    private fun requestLocation() {
        viewModelScope.launch { _effects.send(FeedEffect.RequestLocationPermission) }
    }

    private fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(locationPermissionGranted = granted) }
        if (granted) viewModelScope.launch { _effects.send(FeedEffect.GetLocation) }
    }

    private fun onLocationReceived(
        lat: Double,
        lng: Double,
    ) {
        _state.update { it.copy(userLat = lat, userLng = lng) }

        // Phase 1: immediate sort using termini from route documents
        recomputeBoardable()

        // Phase 2: fetch all stop lists in background, then re-sort
        // using full intermediate stop data for accurate proximity
        viewModelScope.launch {
            prefetchRouteStops(_state.value.routes)
            recomputeBoardable()
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private fun onRouteClicked(route: BoardableRoute) {
        viewModelScope.launch {
            _effects.send(
                FeedEffect.NavigateToRouteDetail(
                    routeId = route.route.id,
                    from = route.boardingStop.name,
                    to = route.onwardTerminus,
                ),
            )
        }
    }

    private fun onSeeAllClicked() {
        viewModelScope.launch { _effects.send(FeedEffect.NavigateToSeeAll) }
    }

    // ── Tooltips ──────────────────────────────────────────────────────────────

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
}
