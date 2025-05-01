package tr.yigitunlu.n11chatapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import tr.yigitunlu.n11chatapp.domain.model.MessageOption
import tr.yigitunlu.n11chatapp.domain.model.ServerMessageType
import tr.yigitunlu.n11chatapp.domain.model.SyncStatus

/**
 * Base entity for all chat messages stored in Room
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val syncStatus: SyncStatus,
    val messageType: MessageType,
    // Server message fields
    val content: String? = null,
    val mediaUrl: String? = null,
    val options: List<MessageOption>? = null,
    val serverMessageType: ServerMessageType? = null,
    val serverAction: String? = null,
    // User choice fields
    val selectedOptionId: String? = null,
    val selectedOptionText: String? = null,
    val selectedOptionAction: String? = null,
    val selectedOptionIsDestructive: Boolean? = null
)

/**
 * Enum to distinguish between different message types in the database
 */
enum class MessageType {
    SERVER_MESSAGE,
    USER_CHOICE
}
