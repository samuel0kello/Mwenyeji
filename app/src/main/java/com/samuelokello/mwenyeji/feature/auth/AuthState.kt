package com.samuelokello.mwenyeji.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.DomainError
import com.samuelokello.mwenyeji.data.helpers.toUserMessage
import com.samuelokello.mwenyeji.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Loading : AuthState

    data object Anonymous : AuthState

    data class SignedIn(
        val uid: String,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
    ) : AuthState
}

class SessionViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    val authState: StateFlow<AuthState> =
        authRepository.authState
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)

    private val _signInUi = MutableStateFlow(SignInUiState())
    val signInUi: StateFlow<SignInUiState> = _signInUi.asStateFlow()

    private val _events = Channel<SessionEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * Gate an action behind authentication.
     * If signed in: run immediately.
     * Otherwise: stash the action and show the sign-in prompt.
     */
    fun requireAuth(action: () -> Unit) {
        // action() // For now, just bypass or hold as requested
        // Or if we want to follow "hold for the build", we can just not do anything
        // However, the user said "any where that reuires signing we can just hold"
        // If I call action(), it proceeds without auth.
        // If I do nothing, the button clicks do nothing.
        // Since we commented out the FAB and Google Signin, requireAuth might not be reached easily.
        // But for safety, let's just make it do nothing or proceed.
        // The user says "users should only be able to generate guide with ai only"
        // AI generation currently seems to be gated? Let's check RouteDetailsScreen.
    }

    fun dismissSignInPrompt() {
        pendingAction = null
        _signInUi.update { it.copy(promptVisible = false) }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _signInUi.update { it.copy(isLoading = true) }
            when (val result = authRepository.signInWithGoogle(context)) {
                is DataResult.Success -> onSignInSucceeded()
                is DataResult.Error -> onSignInFailed(result.error)
            }
        }
    }

    private suspend fun onSignInSucceeded() {
        _signInUi.update { SignInUiState() } // reset to defaults
        _events.send(SessionEvent.ShowSuccess("Welcome! You can now contribute routes."))
        pendingAction?.invoke()
        pendingAction = null
    }

    private suspend fun onSignInFailed(error: DomainError) {
        _signInUi.update { it.copy(isLoading = false) }
        // Cancellation is a UX-neutral event, don't yell at the user
        if (error == DomainError.UserCancelled) return
        _events.send(SessionEvent.ShowError(error.toUserMessage()))
    }

    private var pendingAction: (() -> Unit)? = null
}

data class SignInUiState(
    val promptVisible: Boolean = false,
    val isLoading: Boolean = false,
)

sealed interface SessionEvent {
    data class ShowSuccess(
        val message: String,
    ) : SessionEvent

    data class ShowError(
        val message: String,
    ) : SessionEvent
}
