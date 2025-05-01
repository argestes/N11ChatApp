package tr.yigitunlu.n11chatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tr.yigitunlu.n11chatapp.data.local.entity.ChatMessageEntity
import tr.yigitunlu.n11chatapp.domain.model.SyncStatus

/**
 * Data Access Object for chat messages
 */
@Dao
interface ChatMessageDao {
    /**
     * Insert a new chat message
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    /**
     * Insert multiple chat messages
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    /**
     * Update an existing chat message
     */
    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    /**
     * Get all chat messages as a Flow
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    /**
     * Get a specific chat message by ID
     */
    @Query("SELECT * FROM chat_messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): ChatMessageEntity?

    /**
     * Delete all chat messages
     */
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    /**
     * Update sync status for a message
     */
    @Query("UPDATE chat_messages SET syncStatus = :syncStatus WHERE id = :messageId")
    suspend fun updateMessageSyncStatus(messageId: String, syncStatus: SyncStatus)

    /**
     * Get all messages that need to be synced with the server
     */
    @Query("SELECT * FROM chat_messages WHERE syncStatus IN ('SENDING', 'FAILED') ORDER BY timestamp ASC")
    suspend fun getUnsynedMessages(): List<ChatMessageEntity>
    
    /**
     * Get the latest message by timestamp
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessage(): ChatMessageEntity?
    
    /**
     * Check if there are any messages in the database
     */
    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int
}
