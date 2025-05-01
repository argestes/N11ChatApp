package tr.yigitunlu.n11chatapp.domain.model

/**
 * Base interface for all chat messages
 */
interface ChatMessage {
    val id: String
    val timestamp: Long
    val syncStatus: SyncStatus
}

/**
 * Message from the server to the user
 */
data class ServerMessage(
    override val id: String,
    override val timestamp: Long,
    override val syncStatus: SyncStatus,
    val content: String,
    val mediaUrl: String? = null,
    val options: List<MessageOption>? = null,
    val type: ServerMessageType,
    val serverAction: String
) : ChatMessage

/**
 * Types of server messages
 */
enum class ServerMessageType {
    TEXT,
    IMAGE,
    OPTIONS,
    SYSTEM
}

/**
 * User's choice/response in the conversation
 */
data class UserChoice(
    override val id: String,
    override val timestamp: Long,
    override val syncStatus: SyncStatus,
    val selectedOption: MessageOption,
) : ChatMessage

/**
 * Options presented to the user
 */
data class MessageOption(
    val id: String,
    val text: String,
    val action: String,
    val isDestructive: Boolean = false
)

/**
 * Sync status for messages
 */
enum class SyncStatus {
    INIT,       // Initial state
    SENDING,    // Message is being sent to server
    SENT,       // Message was sent but no confirmation yet
    SYNCED,     // Message is confirmed by server
    FAILED,     // Message failed to send
    LOCAL_ONLY  // Message exists only locally (e.g., system messages)
}
