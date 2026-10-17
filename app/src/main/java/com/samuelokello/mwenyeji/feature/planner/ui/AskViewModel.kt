package com.samuelokello.mwenyeji.feature.planner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelokello.mwenyeji.feature.planner.domain.TripPlanning
import com.samuelokello.mwenyeji.feature.planner.domain.TripPlanning.Outcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One line of the conversation. */
sealed interface ChatMessage {
    data class FromRider(
        val text: String,
    ) : ChatMessage

    /** @param outcome Null for the greeting. */
    data class FromMwenyeji(
        val outcome: Outcome?,
    ) : ChatMessage
}

data class AskState(
    val text: String = "",
    val messages: List<ChatMessage> = listOf(ChatMessage.FromMwenyeji(outcome = null)),
    val isThinking: Boolean = false,
)

sealed interface AskAction {
    data class TextChanged(
        val text: String,
    ) : AskAction

    data object Send : AskAction

    data class Spoken(
        val text: String,
    ) : AskAction

    /** Location was just allowed: answer the last question again. */
    data object LocationGranted : AskAction
}

/**
 * Chat with Mwenyeji. Offline and rule-based: each message is read as a trip and answered with
 * trips or a plain reason. Free-form questions need an online model, which is not part of v1.
 */
class AskViewModel(
    private val planning: TripPlanning,
) : ViewModel() {
    private val _state = MutableStateFlow(AskState())
    val state: StateFlow<AskState> = _state.asStateFlow()

    fun onAction(action: AskAction) {
        when (action) {
            is AskAction.TextChanged -> _state.update { it.copy(text = action.text) }

            AskAction.Send -> send(_state.value.text)

            is AskAction.Spoken -> send(action.text)

            AskAction.LocationGranted ->
                _state.value.messages
                    .filterIsInstance<ChatMessage.FromRider>()
                    .lastOrNull()
                    ?.let { answer(it.text) }
        }
    }

    private fun send(text: String) {
        val message = text.trim()
        if (message.isEmpty() || _state.value.isThinking) return
        _state.update { it.copy(text = "", messages = it.messages + ChatMessage.FromRider(message)) }
        answer(message)
    }

    private fun answer(message: String) {
        viewModelScope.launch {
            _state.update { it.copy(isThinking = true) }
            val outcome = planning.plan(message)
            _state.update { it.copy(isThinking = false, messages = it.messages + ChatMessage.FromMwenyeji(outcome)) }
        }
    }
}
