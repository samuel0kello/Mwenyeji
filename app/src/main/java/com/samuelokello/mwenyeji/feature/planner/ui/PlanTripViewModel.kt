package com.samuelokello.mwenyeji.feature.planner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.feature.planner.domain.TripPlanning
import com.samuelokello.mwenyeji.feature.planner.domain.TripPlanning.Outcome
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlanTripState(
    val text: String = "",
    val isPlanning: Boolean = false,
    val outcome: Outcome? = null,
)

sealed interface PlanTripAction {
    data class TextChanged(
        val text: String,
    ) : PlanTripAction

    data object Submit : PlanTripAction

    /** What the recogniser heard: shown in the field and planned straight away. */
    data class Spoken(
        val text: String,
    ) : PlanTripAction

    /** Location was just allowed, so plan the same trip again from here. */
    data object LocationGranted : PlanTripAction
}

class PlanTripViewModel(
    private val planning: TripPlanning,
) : ViewModel() {
    private val _state = MutableStateFlow(PlanTripState())
    val state: StateFlow<PlanTripState> = _state.asStateFlow()

    private var planJob: Job? = null

    fun onAction(action: PlanTripAction) {
        when (action) {
            is PlanTripAction.TextChanged -> _state.update { it.copy(text = action.text) }

            PlanTripAction.Submit -> plan()

            is PlanTripAction.Spoken -> {
                _state.update { it.copy(text = action.text) }
                plan()
            }

            PlanTripAction.LocationGranted -> plan()
        }
    }

    private fun plan() {
        val text = _state.value.text.trim()
        if (text.isEmpty()) return
        // A newer request replaces the one still working.
        planJob?.cancel()
        planJob =
            viewModelScope.launch {
                _state.update { it.copy(isPlanning = true) }
                val outcome = planning.plan(text)
                _state.update { it.copy(isPlanning = false, outcome = outcome) }
            }
    }
}
