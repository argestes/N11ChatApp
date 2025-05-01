package tr.yigitunlu.n11chatapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tr.yigitunlu.n11chatapp.domain.model.ChatState
import tr.yigitunlu.n11chatapp.domain.usecase.ChatInteractionUseCase
import tr.yigitunlu.n11chatapp.ui.components.ActionOption
import tr.yigitunlu.n11chatapp.ui.components.ChatUiComponent
import javax.inject.Inject

/**
 * ViewModel for the chat screen
 * Handles both online and offline chat interactions
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatInteractionUseCase: ChatInteractionUseCase,
) : ViewModel() {
    // UI state
    val chatComponents: StateFlow<List<ChatUiComponent>> = chatInteractionUseCase
        .getChatComponents { actionId -> onActionSelected(actionId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val conversationTerminated: StateFlow<Boolean> = chatInteractionUseCase.connectionState.map {
        it == ChatState.Terminated
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    init {
        // Connect to chat service when ViewModel is created
        connectToChat()
    }

    /**
     * Connects to the chat service
     * Handles both online and offline modes
     */
    private fun connectToChat() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                chatInteractionUseCase.connect()
            } catch (e: Exception) {
                _error.value = "Failed to connect: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Handles user selection of an action
     * Works in both online and offline modes
     */
    private fun onActionSelected(actionOption: ActionOption) {
        viewModelScope.launch {
            try {
                chatInteractionUseCase.sendUserChoice(
                    actionId = actionOption.actionId,
                    optionText = actionOption.label
                )
            } catch (e: Exception) {
                _error.value = "Failed to send action: ${e.message}"
            }
        }
    }

    /**
     * Disconnects from the chat service
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            chatInteractionUseCase.disconnect()
        }
    }

    fun onClickFeedback() {
        chatInteractionUseCase.onClickFeedback()
    }

    fun onClickReconnect() {
        connectToChat()
    }
}
