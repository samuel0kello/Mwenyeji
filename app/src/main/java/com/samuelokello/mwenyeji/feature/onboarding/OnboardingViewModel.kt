package com.samuelokello.mwenyeji.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val prefs: MwenyejiPrefs,
    private val reducer: OnboardingReducer,
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingContract.State())
    val state: StateFlow<OnboardingContract.State> = _state

    private val _effect = Channel<OnboardingContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun onAction(action: OnboardingContract.Action) {
        val (newState, effect) = reducer.reduce(_state.value, action)
        _state.value = newState

        if (newState.isCompleted) {
            viewModelScope.launch {
                prefs.setOnBoardingComplete(true)

                // Save personalization answer
                newState.selectedUserType?.let { userType ->
                    prefs.saveUserType(userType.name)
                    // Save the default time of day derived from user type
                    // FeedViewModel reads this ogn first launch to set the chip
                    prefs.saveDefaultTimeOfDay(userType.defaultTimeOfDay.name)
                }
            }
        }

        effect?.let {
            viewModelScope.launch { _effect.send(it) }
        }
    }
}
