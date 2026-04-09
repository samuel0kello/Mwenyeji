package com.samuelokello.mwenyeji.feature.contribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.models.RouteTag
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContributeViewModel : ViewModel() {

    private val _state = MutableStateFlow(ContributeState())
    val state: StateFlow<ContributeState> = _state.asStateFlow()

    private val _effects = Channel<ContributeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()


    fun onAction(actio: ContributeActions) {
        when (actio) {
            // Navigation
            is ContributeActions.NextStep        -> onNextStep()
            is ContributeActions.PreviousStep    -> onPreviousStep()

            // Step 1
            is ContributeActions.FromChanged     -> _state.update { it.copy(from = actio.value, errors = it.errors - "from") }
            is ContributeActions.ToChanged       -> _state.update { it.copy(to = actio.value, errors = it.errors - "to") }
            is ContributeActions.ViaChanged      -> _state.update { it.copy(via = actio.value) }
            is ContributeActions.FareChanged     -> _state.update { it.copy(fareKsh = actio.value, errors = it.errors - "fare") }

            // Step 2
            is ContributeActions.TimeOfDaySelected    -> _state.update { it.copy(bestTimeOfDay = actio.timeOfDay) }
            is ContributeActions.TimingReasonChanged  -> _state.update { it.copy(timingReason = actio.value) }

            // Step 3
            is ContributeActions.StepChanged     -> onStepChanged(actio.index, actio.value)
            is ContributeActions.AddStep         -> onAddStep()
            is ContributeActions.RemoveStep      -> onRemoveStep(actio.index)

            // Step 4
            is ContributeActions.WarningsChanged -> _state.update { it.copy(warnings = actio.value) }
            is ContributeActions.TagToggled      -> onTagToggled(actio.tag)

            // Submit
            is ContributeActions.SubmitGuide     -> onSubmitGuide()
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
        val current = _state.value
        if (current.isFirstStep) {
            viewModelScope.launch { _effects.send(ContributeEffect.NavigateBack) }
            return
        }
        _state.update { it.copy(currentStep = it.currentStep - 1, errors = emptyMap()) }
    }


    private fun onStepChanged(index: Int, value: String) {
        val updatedSteps = _state.value.steps.toMutableList()
        if (index in updatedSteps.indices) {
            updatedSteps[index] = value
            _state.update { it.copy(steps = updatedSteps, errors = it.errors - "steps") }
        }
    }

    private fun onAddStep() {
        _state.update { it.copy(steps = it.steps + "") }
    }

    private fun onRemoveStep(index: Int) {
        val updatedSteps = _state.value.steps.toMutableList()
        if (updatedSteps.size > 1 && index in updatedSteps.indices) {
            updatedSteps.removeAt(index)
            _state.update { it.copy(steps = updatedSteps) }
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
            try {
                val route = _state.value.toRoute()
                // TODO: inject and call RouteRepository
                // routeRepository.submitRoute(route)
                _state.update { it.copy(isSubmitting = false, isSubmitted = true) }
                _effects.send(ContributeEffect.NavigateToSuccess)
            } catch (e: Exception) {
                _state.update { it.copy(isSubmitting = false) }
                _effects.send(ContributeEffect.ShowError(e.message ?: "Failed to submit guide"))
            }
        }
    }


    private fun validateStep(state: ContributeState): Map<String, String> =
        buildMap {
            when (state.currentStep) {
                ContributeStep.ROUTE -> {
                    if (state.from.isBlank()) put("from", "Where are you boarding from?")
                    if (state.to.isBlank())   put("to", "Where does this route go?")
                    state.fareKsh.let {
                        if (it.isNotBlank() && it.toDoubleOrNull() == null)
                            put("fare", "Enter a valid fare amount")
                    }
                }
                ContributeStep.INSTRUCTIONS -> {
                    val filledSteps = state.steps.filter { it.isNotBlank() }
                    if (filledSteps.isEmpty()) put("steps", "Add at least one step")
                }
                // Timing and Warnings are optional — no required fields
            }
        }
}