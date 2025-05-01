package tr.yigitunlu.n11chatapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import tr.yigitunlu.n11chatapp.AppState
import tr.yigitunlu.n11chatapp.domain.model.ChatState
import tr.yigitunlu.n11chatapp.domain.model.ChatMessage
import tr.yigitunlu.n11chatapp.domain.model.MessageOption
import tr.yigitunlu.n11chatapp.domain.model.ServerMessage
import tr.yigitunlu.n11chatapp.domain.model.ServerMessageType
import tr.yigitunlu.n11chatapp.domain.model.SyncStatus
import tr.yigitunlu.n11chatapp.domain.model.UserChoice
import tr.yigitunlu.n11chatapp.domain.repository.ChatRepository
import tr.yigitunlu.n11chatapp.ui.components.ActionOption
import tr.yigitunlu.n11chatapp.ui.components.ChatUiComponent
import tr.yigitunlu.n11chatapp.ui.components.LoadingDotsComponent
import tr.yigitunlu.n11chatapp.ui.components.ServerMessageComponent
import tr.yigitunlu.n11chatapp.ui.components.UserAnswerComponent
import tr.yigitunlu.n11chatapp.ui.viewmodel.Screen
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for interacting with the chat system
 * Handles connection, message sending, and mapping domain models to UI components
 */
class ChatInteractionUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val appState: AppState
) {
    /**
     * Get the current connection state
     */
    val connectionState: StateFlow<ChatState> = chatRepository.chatState

    /**
     * Get chat UI components mapped from domain messages
     * @param onActionSelected Callback for when a user selects an action
     */
    fun getChatComponents(onActionSelected: (ActionOption) -> Unit): Flow<List<ChatUiComponent>> {
        return chatRepository.messages.map { messages ->
            messages.mapIndexed { index, message ->
                mapMessageToUiComponent(
                    message = message,
                    isLatestAction = index == messages.size - 1,
                    onActionSelected = onActionSelected
                )
            }
        }
    }

    /**
     * Connect to the chat service
     */
    suspend fun connect() {
        // Try to connect to the chat service
        chatRepository.startOrResumeConversation()

        // Try to sync any pending messages
        chatRepository.syncPendingMessages()
    }

    /**
     * Disconnect from the chat service
     */
    suspend fun disconnect() {
        chatRepository.disconnect()
    }

    /**
     * Send a user choice
     * @param actionId The action ID to send
     * @param optionText Text of the selected option
     * @return true if sent online, false if stored offline
     */
    suspend fun sendUserChoice(actionId: String, optionText: String) {
        val option = MessageOption(
            id = UUID.randomUUID().toString(),
            text = optionText,
            action = actionId,
            isDestructive = actionId.startsWith("end_")
        )
        chatRepository.sendUserChoice(option)
    }

    /**
     * Clear the chat (end conversation)
     */
    suspend fun clearChat() {
        chatRepository.clearChat()
    }

    /**
     * Try to sync any pending messages with the server
     */
    suspend fun syncPendingMessages() {
        chatRepository.syncPendingMessages()
    }

    /**
     * Maps a domain message to a UI component
     * Adds visual indicators for message sync status
     */
    private fun mapMessageToUiComponent(
        message: ChatMessage,
        isLatestAction: Boolean,
        onActionSelected: (ActionOption) -> Unit
    ): ChatUiComponent {
        // Add sync status indicator to the message display

        return when (message) {
            is ServerMessage -> {
                when (message.type) {
                    ServerMessageType.OPTIONS -> {
                        ServerMessageComponent(
                            id = message.id,
                            message = message.content,
                            actions = message.options?.map { option ->
                                ActionOption(
                                    label = option.text,
                                    actionId = option.action,
                                    isDestructive = option.isDestructive
                                )
                            } ?: emptyList(),
                            onActionSelected = onActionSelected,
                            isLatestAction = isLatestAction
                        )
                    }

                    ServerMessageType.IMAGE -> {
                        ServerMessageComponent(
                            id = message.id,
                            message = message.content,
                            imageUrl = message.mediaUrl,
                            isLatestAction = isLatestAction
                        )
                    }

                    else -> {
                        ServerMessageComponent(
                            id = message.id,
                            message = message.content,
                            isLatestAction = isLatestAction
                        )
                    }
                }
            }

            is UserChoice -> {
                UserAnswerComponent(
                    id = message.id,
                    option = ActionOption(
                        label = message.selectedOption.text,
                        actionId = message.selectedOption.action,
                        isDestructive = message.selectedOption.isDestructive
                    )
                )
            }

            else -> {
                // Fallback for unknown message types
                LoadingDotsComponent(
                    id = "loading_${UUID.randomUUID()}",
                    text = "Processing"
                )
            }
        }
    }

    fun onClickFeedback() {
        appState.navigateTo(Screen.Feedback)
    }
}
