package com.samuelokello.mwenyeji.feature.onboarding

import com.samuelokello.mwenyeji.feature.onboarding.pages.UserType

interface OnboardingContract {
    data class State(
        val currentPage: Int = 0,
        val isCompleted: Boolean = false,
        val selectedUserType: UserType? = null,
    )

    sealed interface Action {
        data object OnNextClicked : Action

        data class OnPreviousClicked(
            val page: Int,
        ) : Action

        data object OnSkipClicked : Action

        data class OnPageChanged(
            val page: Int,
        ) : Action

        data class OnUserTypeSelected(
            val userType: UserType,
        ) : Action // ← new
    }

    sealed interface Effect {
        data object NavigateToHome : Effect
    }
}
