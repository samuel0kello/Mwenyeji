package com.samuelokello.mwenyeji.feature.contribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.models.GeoPoint
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.data.models.SearchRequest
import com.samuelokello.mwenyeji.data.models.SearchResult
import com.samuelokello.mwenyeji.data.repository.AuthRepository
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import com.samuelokello.mwenyeji.data.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContributeViewModel(
    private val routeRepository: RoutesRepository,
    private val authRepository: AuthRepository,
    private val searchRepository: SearchRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ContributeState())
    val state: StateFlow<ContributeState> = _state.asStateFlow()

    private val _effects = Channel<ContributeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var fromGeocodeJob: Job? = null
    private var toGeocodeJob: Job? = null

    fun onAction(action: ContributeActions) {
        when (action) {
            is ContributeActions.NextStep -> {
                onNextStep()
            }

            is ContributeActions.PreviousStep -> {
                onPreviousStep()
            }

            is ContributeActions.ViaChanged -> {
                _state.update { it.copy(via = action.value) }
            }

            is ContributeActions.FareChanged -> {
                _state.update {
                    it.copy(
                        fareKsh = action.value,
                        errors = it.errors - "fare",
                    )
                }
            }

            is ContributeActions.TimeOfDaySelected -> {
                _state.update { it.copy(bestTimeOfDay = action.timeOfDay) }
            }

            is ContributeActions.TimingReasonChanged -> {
                _state.update { it.copy(timingReason = action.value) }
            }

            is ContributeActions.StepChanged -> {
                onStepChanged(action.index, action.value)
            }

            is ContributeActions.AddStep -> {
                onAddStep()
            }

            is ContributeActions.RemoveStep -> {
                onRemoveStep(action.index)
            }

            is ContributeActions.WarningsChanged -> {
                _state.update { it.copy(warnings = action.value) }
            }

            is ContributeActions.TagToggled -> {
                onTagToggled(action.tag)
            }

            is ContributeActions.SubmitGuide -> {
                onSubmitGuide()
            }

            is ContributeActions.FromChanged -> {
                _state.update { it.copy(from = action.value, fromSuggestions = emptyList()) }
                debounceSearch(action.value, isFrom = true)
            }

            is ContributeActions.ToChanged -> {
                _state.update { it.copy(to = action.value, toSuggestions = emptyList()) }
                debounceSearch(action.value, isFrom = false)
            }

            is ContributeActions.FromSuggestionSelected -> {
                selectSuggestion(
                    action.result,
                    isFrom = true,
                )
            }

            is ContributeActions.ToSuggestionSelected -> {
                selectSuggestion(
                    action.result,
                    isFrom = false,
                )
            }

            is ContributeActions.FromPinDragged -> {
                _state.update {
                    it.copy(
                        fromGeoPoint =
                            it.fromGeoPoint?.copy(
                                lat = action.lat,
                                lng = action.lng,
                            ),
                    )
                }
            }

            is ContributeActions.ToPinDragged -> {
                _state.update {
                    it.copy(
                        toGeoPoint =
                            it.toGeoPoint?.copy(
                                lat = action.lat,
                                lng = action.lng,
                            ),
                    )
                }
            }

            is ContributeActions.RouteNumberChanged -> {
                _state.update { it.copy(routeNumber = action.value) }
            }

            is ContributeActions.SaccoChanged -> {
                _state.update {
                    val updated = it.saccos.toMutableList()
                    updated[action.index] = action.value
                    it.copy(saccos = updated)
                }
            }

            is ContributeActions.AddSacco -> {
                _state.update { it.copy(saccos = it.saccos + "") }
            }

            is ContributeActions.RemoveSacco -> {
                _state.update {
                    if (it.saccos.size <= 1) return@update it // keep at least one
                    it.copy(saccos = it.saccos.filterIndexed { i, _ -> i != action.index })
                }
            }
        }
    }

    private fun onNextStep() {
        val current = _state.value
        val errors = validateStep(current)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        if (current.isLastStep) {
            onSubmitGuide()
            return
        }
        _state.update { it.copy(currentStep = it.currentStep + 1, errors = emptyMap()) }
    }

    private fun onPreviousStep() {
        if (_state.value.isFirstStep) {
            resetState()
            viewModelScope.launch { _effects.send(ContributeEffect.NavigateBack) }
            return
        }
        _state.update { it.copy(currentStep = it.currentStep - 1, errors = emptyMap()) }
    }

    private fun onStepChanged(index: Int, value: String) {
        val updated = _state.value.steps.toMutableList()
        if (index in updated.indices) {
            updated[index] = value
            _state.update { it.copy(steps = updated, errors = it.errors - "steps") }
        }
    }

    private fun onAddStep() {
        _state.update { it.copy(steps = it.steps + "") }
    }

    private fun onRemoveStep(index: Int) {
        val updated = _state.value.steps.toMutableList()
        if (updated.size > 1 && index in updated.indices) {
            updated.removeAt(index)
            _state.update { it.copy(steps = updated) }
        }
    }

    private fun onTagToggled(tag: RouteTag) {
        _state.update { current ->
            val tags = current.selectedTags.toMutableSet()
            if (tag in tags) tags.remove(tag) else tags.add(tag)
            current.copy(selectedTags = tags)
        }
    }

    private fun onSubmitGuide() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            val route =
                _state.value.toRoute().copy(
                    contributorId = authRepository.currentUserId ?: "anonymous",
                )

            when (routeRepository.submitRoute(route)) {
                is DataResult.Success -> {
                    _effects.send(ContributeEffect.NavigateBack)
                }

                is DataResult.Error -> {
                    _state.update { it.copy(isSubmitting = false) }
                }
            }
        }
    }

    private fun resetState() {
        _state.value = ContributeState()
    }

    private fun debounceSearch(query: String, isFrom: Boolean) {
        if (query.length < 2) return
        val request = SearchRequest(query)

        if (isFrom) {
            fromGeocodeJob?.cancel()
            fromGeocodeJob =
                viewModelScope.launch {
                    delay(400)
                    when (val result = searchRepository.search(request)) {
                        is DataResult.Success -> {
                            _state.update {
                                it.copy(fromSuggestions = result.data)
                            }
                        }

                        is DataResult.Error -> {
                            _state.update {
                                it.copy(fromSuggestions = emptyList())
                            }
                        }
                    }
                }
        } else {
            toGeocodeJob?.cancel()
            toGeocodeJob =
                viewModelScope.launch {
                    delay(400)
                    when (val result = searchRepository.search(request)) {
                        is DataResult.Success -> {
                            _state.update {
                                it.copy(toSuggestions = result.data)
                            }
                        }

                        is DataResult.Error -> {
                            _state.update {
                                it.copy(toSuggestions = emptyList())
                            }
                        }
                    }
                }
        }
    }

    private fun selectSuggestion(result: SearchResult, isFrom: Boolean) {
        val point =
            GeoPoint(
                lat = result.coordinates?.latitude ?: return,
                lng = result.coordinates.longitude,
                displayName = result.fullAddress ?: result.name,
            )

        if (isFrom) {
            _state.update {
                it.copy(
                    from = result.name,
                    fromGeoPoint = point,
                    fromSuggestions = emptyList(),
                )
            }
        } else {
            _state.update {
                it.copy(
                    to = result.name,
                    toGeoPoint = point,
                    toSuggestions = emptyList(),
                )
            }
        }
    }

    private fun validateStep(state: ContributeState): Map<String, String> =
        buildMap {
            when (state.currentStep) {
                ContributeStep.ROUTE -> {
                    if (state.from.isBlank()) put("from", "Where are you boarding from?")
                    if (state.to.isBlank()) put("to", "Where does this route go?")
                    if (state.fareKsh.isNotBlank() && state.fareKsh.toDoubleOrNull() == null) {
                        put("fare", "Enter a valid fare amount")
                    }
                }

                ContributeStep.INSTRUCTIONS -> {
                    if (state.steps.none { it.isNotBlank() }) {
                        put("steps", "Add at least one step")
                    }
                }
            }
        }
}
