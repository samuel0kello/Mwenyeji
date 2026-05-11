package com.samuelokello.mwenyeji.feature.contribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.data.repository.AuthRepository
import com.samuelokello.mwenyeji.data.repository.RoutesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContributeViewModel(
    private val routesRepository: RoutesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ContributeState())
    val state: StateFlow<ContributeState> = _state.asStateFlow()

    private val _effects = Channel<ContributeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * Called by the screen immediately after opening, passing the GTFS route
     * the user selected from the feed or route detail screen.
     * Sets the locked route context shown at the top of every step.
     */
    fun initWithRoute(route: Route) {
        _state.update {
            it.copy(
                routeId = route.id,
                routeNumber = route.routeNumber,
                routeFrom = route.from,
                routeTo = route.to,
            )
        }
    }

    fun onAction(action: ContributeActions) {
        when (action) {
            is ContributeActions.NextStep -> {
                onNextStep()
            }

            is ContributeActions.PreviousStep -> {
                onPreviousStep()
            }

            is ContributeActions.FareChanged -> {
                _state.update {
                    it.copy(fareKsh = action.value, errors = it.errors - "fare")
                }
            }

            is ContributeActions.SaccoChanged -> {
                _state.update {
                    it.copy(sacco = action.value)
                }
            }

            is ContributeActions.TimeOfDaySelected -> {
                _state.update {
                    it.copy(bestTimeOfDay = action.timeOfDay)
                }
            }

            is ContributeActions.TimingReasonChanged -> {
                _state.update {
                    it.copy(timingReason = action.value)
                }
            }

            is ContributeActions.StepChanged -> {
                onStepChanged(action.index, action.value)
            }

            is ContributeActions.AddStep -> {
                _state.update { it.copy(steps = it.steps + "") }
            }

            is ContributeActions.RemoveStep -> {
                onRemoveStep(action.index)
            }

            is ContributeActions.WarningsChanged -> {
                _state.update {
                    it.copy(warnings = action.value)
                }
            }

            is ContributeActions.TagToggled -> {
                onTagToggled(action.tag)
            }

            is ContributeActions.SubmitGuide -> {
                onSubmitGuide()
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
        val current = _state.value
        val contributorId = authRepository.currentUserId ?: return

        if (current.routeId.isBlank()) {
            viewModelScope.launch {
                _effects.send(ContributeEffect.ShowError("No route selected"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            val guide = current.toGuide(contributorId)

            when (routesRepository.submitGuide(current.routeId, guide)) {
                is DataResult.Success -> {
                    _state.update { it.copy(isSubmitting = false, isSubmitted = true) }
                    _effects.send(ContributeEffect.NavigateToSuccess)
                }

                is DataResult.Error -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _effects.send(ContributeEffect.ShowError("Failed to submit guide. Try again."))
                }
            }
        }
    }

    private fun validateStep(state: ContributeState): Map<String, String> =
        buildMap {
            when (state.currentStep) {
                ContributeStep.FARE -> {
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

    private fun resetState() {
        _state.value = ContributeState()
    }
}
