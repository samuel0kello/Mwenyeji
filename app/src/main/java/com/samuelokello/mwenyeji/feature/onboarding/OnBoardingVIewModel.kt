package com.samuelokello.mwenyeji.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface OnboardingNavigationEvent {
    data object NavigateToHome : OnboardingNavigationEvent
}

class OnboardingViewModel(
    private val pref: MwenyejiPrefs
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _navigationEvent = Channel<OnboardingNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    val pages = listOf(
        Page(
            title = R.string.context_maters,
            description = R.string.morning_evening_rush_hour_locals_move_differently,
            drawable = R.drawable.ic_launcher_foreground,
            btnText = "continue"
        ),
        Page(
            title = R.string.onboarding_title_2,
            description = R.string.onboarding_desc_2,
            drawable = R.drawable.ic_launcher_foreground,
            btnText = "continue"
        ),
        Page(
            title = R.string.onboarding_title_3,
            description = R.string.onboarding_desc_3,
            drawable = R.drawable.ic_launcher_foreground,
            btnText = "Find a local route"
        )
    )

    fun onNextPage() {
        if (_currentPage.value < pages.lastIndex) {
            _currentPage.update { it + 1 }
        }
    }

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
    val btnText: String
)
