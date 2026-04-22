package com.samuelokello.mwenyeji.feature.onboarding

private const val TOTAL_PAGES = 5
private const val LAST_PAGE = TOTAL_PAGES - 1

class OnboardingReducer {
    fun reduce(
        state: OnboardingContract.State,
        action: OnboardingContract.Action,
    ): Pair<OnboardingContract.State, OnboardingContract.Effect?> =
        when (action) {
            OnboardingContract.Action.OnNextClicked -> {
                if (state.currentPage < LAST_PAGE) {
                    state.copy(currentPage = state.currentPage + 1) to null
                } else {
                    // Last page — require user type selection before finishing
                    if (state.selectedUserType == null) {
                        // Don't advance — user must pick an option
                        state to null
                    } else {
                        state.copy(isCompleted = true) to OnboardingContract.Effect.NavigateToHome
                    }
                }
            }

            OnboardingContract.Action.OnSkipClicked -> {
                state.copy(isCompleted = true) to OnboardingContract.Effect.NavigateToHome
            }

            is OnboardingContract.Action.OnPageChanged -> {
                state.copy(currentPage = action.page) to null
            }

            is OnboardingContract.Action.OnUserTypeSelected -> {
                state.copy(selectedUserType = action.userType) to null
            }
        }
}
