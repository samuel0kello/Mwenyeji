package com.samuelokello.mwenyeji.feature.contribute

import com.samuelokello.mwenyeji.data.models.Route
import com.samuelokello.mwenyeji.data.models.RouteStep
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.data.models.TimeOfDay


// Step index constants
object ContributeStep {
    const val ROUTE        = 0
    const val TIMING       = 1
    const val INSTRUCTIONS = 2
    const val WARNINGS     = 3
    const val TOTAL        = 4
}


// State — the full in-progress route + UI state shared across all steps
data class ContributeState(

    val currentStep: Int = ContributeStep.ROUTE,

    // ── Step 1: Route
    val from: String = "",
    val to: String = "",
    val via: String = "",
    val fareKsh: String = "",           // String for text field; parsed to Double on submit

    val bestTimeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val timingReason: String = "",

    val steps: List<String> = listOf("", "", ""),   // start with 3 empty fields

    val warnings: String = "",
    val selectedTags: Set<RouteTag> = emptySet(),

    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,

    val errors: Map<String, String> = emptyMap(),
) {
    // helpers

    val isFirstStep: Boolean get() = currentStep == ContributeStep.ROUTE
    val isLastStep:  Boolean get() = currentStep == ContributeStep.WARNINGS

    /** Step label shown in the header e.g. "STEP 1 OF 4 · ROUTE" */
    val stepLabel: String get() = when (currentStep) {
        ContributeStep.ROUTE        -> "STEP 1 OF 4 · ROUTE"
        ContributeStep.TIMING       -> "STEP 2 OF 4 · TIMING"
        ContributeStep.INSTRUCTIONS -> "STEP 3 OF 4 · STEPS"
        ContributeStep.WARNINGS     -> "STEP 4 OF 4 · WARNINGS"
        else -> ""
    }

    /** Step title shown as the heading */
    val stepTitle: String get() = when (currentStep) {
        ContributeStep.ROUTE        -> "Where does this guide go?"
        ContributeStep.TIMING       -> "When does this work best?"
        ContributeStep.INSTRUCTIONS -> "How do you do it?"
        ContributeStep.WARNINGS     -> "What should people avoid?"
        else -> ""
    }

    /** Step subtitle shown below the title */
    val stepSubtitle: String get() = when (currentStep) {
        ContributeStep.ROUTE        -> "Tell us the start and end points"
        ContributeStep.TIMING       -> "Locals move differently at different times"
        ContributeStep.INSTRUCTIONS -> "Share step-by-step local advice"
        ContributeStep.WARNINGS     -> "Help others dodge common mistakes"
        else -> ""
    }

    /**
     * Builds the final [Route] from all step data.
     * Called just before submission.
     */
    fun toRoute(): Route = Route(
        from          = from.trim(),
        to            = to.trim(),
        via           = via.trim(),
        fareKsh       = fareKsh.toDoubleOrNull(),
        bestTimeOfDay = bestTimeOfDay,
        timingReason  = timingReason.trim(),
        steps         = steps
            .filter { it.isNotBlank() }
            .mapIndexed { index, instruction ->
                RouteStep(order = index + 1, instruction = instruction.trim())
            },
        warnings      = warnings.trim(),
        tags          = selectedTags,
    )
}


// Intent — every action a user can take across all steps
sealed interface ContributeActions {

    data object NextStep : ContributeActions
    data object PreviousStep : ContributeActions

    data class FromChanged(val value: String) : ContributeActions
    data class ToChanged(val value: String) : ContributeActions
    data class ViaChanged(val value: String) : ContributeActions
    data class FareChanged(val value: String) : ContributeActions

    data class TimeOfDaySelected(val timeOfDay: TimeOfDay) : ContributeActions
    data class TimingReasonChanged(val value: String) : ContributeActions

    data class StepChanged(val index: Int, val value: String) : ContributeActions
    data object AddStep : ContributeActions
    data class RemoveStep(val index: Int) : ContributeActions

    data class WarningsChanged(val value: String) : ContributeActions
    data class TagToggled(val tag: RouteTag) : ContributeActions

    data object SubmitGuide : ContributeActions
}

sealed interface ContributeEffect {
    data object NavigateBack : ContributeEffect
    data object NavigateToSuccess : ContributeEffect
    data class ShowError(val message: String) : ContributeEffect
    data class ShowFieldError(val field: String, val message: String) : ContributeEffect
}