package tr.yigitunlu.n11chatapp.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tr.yigitunlu.n11chatapp.data.local.dao.ChatMessageDao
import tr.yigitunlu.n11chatapp.data.local.mapper.ChatEntityMapper
import tr.yigitunlu.n11chatapp.data.remote.dto.ChatStepDto
import tr.yigitunlu.n11chatapp.data.remote.websocket.ChatWebSocketClient
import tr.yigitunlu.n11chatapp.domain.model.ChatMessage
import tr.yigitunlu.n11chatapp.domain.model.ChatState
import tr.yigitunlu.n11chatapp.domain.model.MessageOption
import tr.yigitunlu.n11chatapp.domain.model.ServerMessage
import tr.yigitunlu.n11chatapp.domain.model.ServerMessageType
import tr.yigitunlu.n11chatapp.domain.model.SyncStatus
import tr.yigitunlu.n11chatapp.domain.model.UserChoice
import tr.yigitunlu.n11chatapp.domain.repository.ChatRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ChatRepository that uses WebSocket for communication
 * and Room for offline storage
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val webSocketClient: ChatWebSocketClient,
    private val chatMessageDao: ChatMessageDao,
) : ChatRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Internal mutable state
    private val _chatState = MutableStateFlow(ChatState.NotStarted)

    // Public immutable state
    override val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    // Messages are now sourced from Room database
    override val messages: Flow<List<ChatMessage>> =
        chatMessageDao.getAllMessages().map { entities ->
            ChatEntityMapper.toDomainList(entities)
        }

    init {
        coroutineScope.launch {
            syncPendingMessages()
        }
    }

    override suspend fun connect() {
        coroutineScope.launch {
            webSocketClient.connect()
                .collect { chatStepDto ->
                    // Convert DTO to domain entity and add to messages
                    val serverMessage = when (chatStepDto) {
                        is ChatStepDto.ButtonStep -> {
                            ServerMessage(
                                id = UUID.randomUUID().toString(),
                                timestamp = System.currentTimeMillis(),
                                syncStatus = SyncStatus.SYNCED,
                                content = chatStepDto.content.text,
                                options = chatStepDto.content.buttons.map { button ->
                                    MessageOption(
                                        id = UUID.randomUUID().toString(),
                                        text = button.label,
                                        action = button.action,
                                        isDestructive = button.action.startsWith("end_")
                                    )
                                },
                                type = ServerMessageType.OPTIONS,
                                serverAction = chatStepDto.action
                            )
                        }

                        is ChatStepDto.TextStep -> {
                            ServerMessage(
                                id = UUID.randomUUID().toString(),
                                timestamp = System.currentTimeMillis(),
                                syncStatus = SyncStatus.SYNCED,
                                content = chatStepDto.content,
                                type = ServerMessageType.TEXT,
                                serverAction = chatStepDto.action
                            )
                        }

                        is ChatStepDto.ImageStep -> {
                            ServerMessage(
                                id = UUID.randomUUID().toString(),
                                timestamp = System.currentTimeMillis(),
                                syncStatus = SyncStatus.SYNCED,
                                content = "",
                                mediaUrl = chatStepDto.content, // Image URL is in content field
                                type = ServerMessageType.IMAGE,
                                serverAction = chatStepDto.action
                            )
                        }
                    }

                    onMessageReceived(serverMessage)
                }
        }
    }

    private suspend fun onMessageReceived(serverMessage: ServerMessage) {
        saveMessage(serverMessage)

        if (serverMessage.serverAction == "show_guide") {
            webSocketClient.sendStep("step_7")
        }

        if (serverMessage.serverAction == "end_conversation") {
            _chatState.update { ChatState.Terminated }
        }
    }

    override suspend fun disconnect() {
        webSocketClient.disconnect()
    }

    override suspend fun sendUserChoice(option: MessageOption) {
        // Create a user choice with initial status based on network availability
        val initialStatus = SyncStatus.INIT

        val userChoice = UserChoice(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            syncStatus = initialStatus,
            selectedOption = option,
        )

        // Save to local database immediately
        saveMessage(userChoice)

        if (userChoice.selectedOption.action == "end_conversation") {
            updateMessageSyncStatus(userChoice.id, SyncStatus.LOCAL_ONLY)
            webSocketClient.disconnect()
            _chatState.update { ChatState.Terminated }
            return
        }
        // Send to server
        val success = webSocketClient.sendStep(option.action)

        // Update status based on result
        val newStatus = if (success) SyncStatus.SENT else SyncStatus.FAILED
        updateMessageSyncStatus(userChoice.id, newStatus)
    }

    override suspend fun clearChat() {
        withContext(Dispatchers.IO) {
            chatMessageDao.deleteAllMessages()
            _chatState.update { ChatState.NotStarted }
        }
    }

    override suspend fun saveMessage(message: ChatMessage) {
        withContext(Dispatchers.IO) {
            val entity = ChatEntityMapper.toEntity(message)
            chatMessageDao.insertMessage(entity)
        }
    }

    override suspend fun saveMessages(messages: List<ChatMessage>) {
        withContext(Dispatchers.IO) {
            val entities = ChatEntityMapper.toEntityList(messages)
            chatMessageDao.insertMessages(entities)
        }
    }

    override suspend fun updateMessageSyncStatus(messageId: String, status: SyncStatus) {
        withContext(Dispatchers.IO) {
            chatMessageDao.updateMessageSyncStatus(messageId, status)
        }
    }

    override suspend fun getUnsynedMessages(): List<ChatMessage> {
        return withContext(Dispatchers.IO) {
            val unsynedEntities = chatMessageDao.getUnsynedMessages()
            ChatEntityMapper.toDomainList(unsynedEntities)
        }
    }

    override suspend fun syncPendingMessages() {
        val unsynedMessages = getUnsynedMessages()
        for (message in unsynedMessages) {
            if (message is UserChoice) {
                // Try to send the message again
                val success = webSocketClient.sendStep(message.selectedOption.action)

                // Update status based on result
                val newStatus = if (success) SyncStatus.SENT else SyncStatus.FAILED
                updateMessageSyncStatus(message.id, newStatus)
            }
        }
    }

    private fun isWaitingForUserInput(): Boolean {
        return chatState.value == ChatState.WaitUserMessage
    }

    override suspend fun isConversationEnded(): Boolean {
        return chatState.value == ChatState.Terminated || chatState.value == ChatState.NotStarted
    }

    override suspend fun startOrResumeConversation() {
        val conversationEnded = isConversationEnded()
        if (conversationEnded) {
            clearChat()
        }

        if (webSocketClient.connectionState.value != ChatWebSocketClient.ConnectionState.Connected) {
            connect()
        }

        val waitingForInput = isWaitingForUserInput()
        if (!waitingForInput || conversationEnded) {
            coroutineScope.launch {
                webSocketClient.sendStep("step_1")
            }
        }
    }
}
