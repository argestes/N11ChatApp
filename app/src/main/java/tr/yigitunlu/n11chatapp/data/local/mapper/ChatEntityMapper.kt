package tr.yigitunlu.n11chatapp.data.local.mapper

import tr.yigitunlu.n11chatapp.data.local.entity.ChatMessageEntity
import tr.yigitunlu.n11chatapp.data.local.entity.MessageType
import tr.yigitunlu.n11chatapp.domain.model.ChatMessage
import tr.yigitunlu.n11chatapp.domain.model.MessageOption
import tr.yigitunlu.n11chatapp.domain.model.ServerMessage
import tr.yigitunlu.n11chatapp.domain.model.UserChoice

/**
 * Mapper for converting between domain models and database entities
 */
object ChatEntityMapper {
    
    /**
     * Convert a domain ChatMessage to a database entity
     */
    fun toEntity(message: ChatMessage): ChatMessageEntity {
        return when (message) {
            is ServerMessage -> {
                ChatMessageEntity(
                    id = message.id,
                    timestamp = message.timestamp,
                    syncStatus = message.syncStatus,
                    messageType = MessageType.SERVER_MESSAGE,
                    content = message.content,
                    mediaUrl = message.mediaUrl,
                    options = message.options,
                    serverMessageType = message.type,
                    serverAction = message.serverAction
                )
            }
            is UserChoice -> {
                ChatMessageEntity(
                    id = message.id,
                    timestamp = message.timestamp,
                    syncStatus = message.syncStatus,
                    messageType = MessageType.USER_CHOICE,
                    selectedOptionId = message.selectedOption.id,
                    selectedOptionText = message.selectedOption.text,
                    selectedOptionAction = message.selectedOption.action,
                    selectedOptionIsDestructive = message.selectedOption.isDestructive
                )
            }
            else -> throw IllegalArgumentException("Unknown message type: ${message::class.java.name}")
        }
    }
    
    /**
     * Convert a list of domain ChatMessages to database entities
     */
    fun toEntityList(messages: List<ChatMessage>): List<ChatMessageEntity> {
        return messages.map { toEntity(it) }
    }
    
    /**
     * Convert a database entity to a domain ChatMessage
     */
    fun toDomain(entity: ChatMessageEntity): ChatMessage {
        return when (entity.messageType) {
            MessageType.SERVER_MESSAGE -> {
                ServerMessage(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    syncStatus = entity.syncStatus,
                    content = entity.content ?: "",
                    mediaUrl = entity.mediaUrl,
                    options = entity.options,
                    type = entity.serverMessageType ?: throw IllegalStateException("Server message type cannot be null"),
                    serverAction = entity.serverAction ?: ""
                )
            }
            MessageType.USER_CHOICE -> {
                UserChoice(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    syncStatus = entity.syncStatus,
                    selectedOption = MessageOption(
                        id = entity.selectedOptionId ?: "",
                        text = entity.selectedOptionText ?: "",
                        action = entity.selectedOptionAction ?: "",
                        isDestructive = entity.selectedOptionIsDestructive ?: false
                    )
                )
            }
        }
    }
    
    /**
     * Convert a list of database entities to domain ChatMessages
     */
    fun toDomainList(entities: List<ChatMessageEntity>): List<ChatMessage> {
        return entities.map { toDomain(it) }
    }
}
