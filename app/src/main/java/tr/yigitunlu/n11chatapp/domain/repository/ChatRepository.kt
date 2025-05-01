package tr.yigitunlu.n11chatapp.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import tr.yigitunlu.n11chatapp.domain.model.ChatState
import tr.yigitunlu.n11chatapp.domain.model.ChatMessage
import tr.yigitunlu.n11chatapp.domain.model.MessageOption
import tr.yigitunlu.n11chatapp.domain.model.SyncStatus

/**
 * Repository interface for chat functionality
 */
interface ChatRepository {
    /**
     * Current state of the chat connection
     */
    val chatState: StateFlow<ChatState>
    
    /**
     * Stream of messages in the current chat
     */
    val messages: Flow<List<ChatMessage>>
    
    /**
     * Connect to the chat service
     */
    suspend fun connect()
    
    /**
     * Disconnect from the chat service
     */
    suspend fun disconnect()
    
    /**
     * Send a user choice
     * @param option The selected option
     */
    suspend fun sendUserChoice(option: MessageOption)
    
    /**
     * Clear all messages (end conversation)
     */
    suspend fun clearChat()
    
    /**
     * Save a message to local storage
     * @param message The message to save
     */
    suspend fun saveMessage(message: ChatMessage)
    
    /**
     * Save multiple messages to local storage
     * @param messages The messages to save
     */
    suspend fun saveMessages(messages: List<ChatMessage>)
    
    /**
     * Update the sync status of a message
     * @param messageId The ID of the message to update
     * @param status The new sync status
     */
    suspend fun updateMessageSyncStatus(messageId: String, status: SyncStatus)
    
    /**
     * Get messages that need to be synced with the server
     * @return List of unsynced messages
     */
    suspend fun getUnsynedMessages(): List<ChatMessage>
    
    /**
     * Sync all pending messages with the server
     */
    suspend fun syncPendingMessages()
    
    /**
     * Start a new conversation or resume an existing one based on the current state
     * - If waiting for user input: Does nothing (UI will show options)
     * - If conversation ended or no messages: Connects and sends step_1
     */
    suspend fun startOrResumeConversation()
    suspend fun isConversationEnded(): Boolean
}
