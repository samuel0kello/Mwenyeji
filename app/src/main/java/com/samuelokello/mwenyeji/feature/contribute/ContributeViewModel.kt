package com.samuelokello.mwenyeji.feature.contribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.data.repository.AuthRepository
import com.samuelokello.mwenyeji.data.repository.RouteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContributeViewModel(
    private val routeRepository: RouteRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ContributeState())
    val state: StateFlow<ContributeState> = _state.asStateFlow()

    private val _effects = Channel<ContributeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onAction(action: ContributeActions) {
        when (action) {
            is ContributeActions.NextStep -> {
                onNextStep()
            }

            is ContributeActions.PreviousStep -> {
                onPreviousStep()
            }

            is ContributeActions.FromChanged -> {
                _state.update {
                    it.copy(
                        from = action.value,
                        errors = it.errors - "from",
                    )
                }
            }

            is ContributeActions.ToChanged -> {
                _state.update {
                    it.copy(
                        to = action.value,
                        errors = it.errors - "to",
                    )
                }
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

    private fun onStepChanged(
        index: Int,
        value: String,
    ) {
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

            routeRepository
                .submitRoute(route)
                .onSuccess {
                    resetState()
                    _effects.send(ContributeEffect.NavigateToSuccess)
                }.onFailure { e ->
                    _state.update { it.copy(isSubmitting = false) }
                    _effects.send(ContributeEffect.ShowError(e.message ?: "Failed to submit guide"))
                }
        }
    }

    private fun resetState() {
        _state.value = ContributeState()
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
