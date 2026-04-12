package com.samuelokello.mwenyeji.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface OnboardingNavigationEvent {
    data object NavigateToHome : OnboardingNavigationEvent
}

class OnboardingViewModel(
    private val pref: MwenyejiPrefs,
) : ViewModel() {
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _navigationEvent = Channel<OnboardingNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onFinish() {
        viewModelScope.launch {
            pref.setOnBoardingComplete(true)
            _navigationEvent.send(OnboardingNavigationEvent.NavigateToHome)
        }
    }
}

data class Page(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val drawable: Int,
    val btnText: String,
)
