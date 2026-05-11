package com.samuelokello.mwenyeji.feature.contribute

import com.samuelokello.mwenyeji.data.models.Guide
import com.samuelokello.mwenyeji.data.models.RouteStep
import com.samuelokello.mwenyeji.data.models.RouteTag
import com.samuelokello.mwenyeji.data.models.TimeOfDay

// Step index constants
object ContributeStep {
    const val FARE = 0
    const val TIMING = 1
    const val INSTRUCTIONS = 2
    const val WARNINGS = 3
    const val TOTAL = 4
}

/**
 * State for the guide contribution flow.
 *
 * A guide is attached to an existing GTFS route — the user arrives here
 * after selecting a specific route from the feed or route detail screen.
 * They never type a from/to — the route context is pre-filled and locked.
 */
data class ContributeState(
    val currentStep: Int = ContributeStep.FARE,
    // ── Route context — pre-filled from the selected GTFS route, not editable ──
    val routeId: String = "",
    val routeNumber: String? = null, // display only e.g. "34J"
    val routeFrom: String = "", // display only e.g. "Ambassadeur"
    val routeTo: String = "", // display only e.g. "JKIA"
    // ── Step 1: Fare ──────────────────────────────────────────────────────────
    val fareKsh: String = "", // String for text field; parsed to Double on submit
    val sacco: String = "", // e.g. "City Hoppa", "Embassava"
    // ── Step 2: Timing ────────────────────────────────────────────────────────
    val bestTimeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val timingReason: String = "",
    // ── Step 3: Instructions ──────────────────────────────────────────────────
    val steps: List<String> = listOf("", "", ""),
    // ── Step 4: Warnings & Tags ───────────────────────────────────────────────
    val warnings: String = "",
    val selectedTags: Set<RouteTag> = emptySet(),
    // ── UI state ──────────────────────────────────────────────────────────────
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
) {
    val isFirstStep: Boolean get() = currentStep == ContributeStep.FARE
    val isLastStep: Boolean get() = currentStep == ContributeStep.WARNINGS

    val stepLabel: String
        get() =
            when (currentStep) {
                ContributeStep.FARE -> "STEP 1 OF 4 · FARE"
                ContributeStep.TIMING -> "STEP 2 OF 4 · TIMING"
                ContributeStep.INSTRUCTIONS -> "STEP 3 OF 4 · STEPS"
                ContributeStep.WARNINGS -> "STEP 4 OF 4 · WARNINGS"
                else -> ""
            }

    val stepTitle: String
        get() =
            when (currentStep) {
                ContributeStep.FARE -> "What does it cost?"
                ContributeStep.TIMING -> "When does this work best?"
                ContributeStep.INSTRUCTIONS -> "How do you do it?"
                ContributeStep.WARNINGS -> "What should people avoid?"
                else -> ""
            }

    val stepSubtitle: String
        get() =
            when (currentStep) {
                ContributeStep.FARE -> "Share the fare and sacco for this route"
                ContributeStep.TIMING -> "Locals move differently at different times"
                ContributeStep.INSTRUCTIONS -> "Share step-by-step local advice"
                ContributeStep.WARNINGS -> "Help others dodge common mistakes"
                else -> ""
            }

    /** Route context shown at the top of the sheet — always visible */
    val routeDisplayName: String
        get() =
            if (routeNumber != null) {
                "$routeNumber: $routeFrom → $routeTo"
            } else {
                "$routeFrom → $routeTo"
            }

    /**
     * Builds the [Guide] from all step data.
     * [contributorId] injected by the ViewModel from the auth state.
     */
    fun toGuide(contributorId: String): Guide =
        Guide(
            routeId = routeId,
            fareKsh = fareKsh.toDoubleOrNull(),
            sacco = sacco.trim(),
            bestTimeOfDay = bestTimeOfDay,
            timingReason = timingReason.trim(),
            steps =
                steps
                    .filter { it.isNotBlank() }
                    .mapIndexed { index, instruction ->
                        RouteStep(order = index + 1, instruction = instruction.trim())
                    },
            warnings = warnings.trim(),
            tags = selectedTags,
            contributorId = contributorId,
        )
}

sealed interface ContributeActions {
    data object NextStep : ContributeActions

    data object PreviousStep : ContributeActions

    // Step 1 — Fare
    data class FareChanged(
        val value: String,
    ) : ContributeActions

    data class SaccoChanged(
        val value: String,
    ) : ContributeActions

    // Step 2 — Timing
    data class TimeOfDaySelected(
        val timeOfDay: TimeOfDay,
    ) : ContributeActions

    data class TimingReasonChanged(
        val value: String,
    ) : ContributeActions

    // Step 3 — Instructions
    data class StepChanged(
        val index: Int,
        val value: String,
    ) : ContributeActions

    data object AddStep : ContributeActions

    data class RemoveStep(
        val index: Int,
    ) : ContributeActions

    // Step 4 — Warnings & Tags
    data class WarningsChanged(
        val value: String,
    ) : ContributeActions

    data class TagToggled(
        val tag: RouteTag,
    ) : ContributeActions

    data object SubmitGuide : ContributeActions
}

sealed interface ContributeEffect {
    data object NavigateBack : ContributeEffect

    data object NavigateToSuccess : ContributeEffect

    data class ShowError(
        val message: String,
    ) : ContributeEffect
}
