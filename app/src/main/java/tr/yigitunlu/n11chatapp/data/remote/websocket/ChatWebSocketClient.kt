package tr.yigitunlu.n11chatapp.data.remote.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import tr.yigitunlu.n11chatapp.data.remote.dto.ChatStepDto

/**
 * Interface for WebSocket client that handles chat communication
 */
interface ChatWebSocketClient {
    /**
     * The current connection state of the WebSocket
     */
    val connectionState: StateFlow<ConnectionState>
    
    /**
     * Connect to the WebSocket server and receive messages
     * @return Flow of ChatStepDto objects received from the server
     */
    fun connect(): Flow<ChatStepDto>
    
    /**
     * Send a specific step by name
     * @param stepName The name of the step to send (e.g., "step_2")
     * @return true if the step was sent successfully, false otherwise
     */
    fun sendStep(stepName: String): Boolean
    
    /**
     * Disconnect from the WebSocket
     */
    fun disconnect()
    
    /**
     * Represents the possible states of the WebSocket connection
     */
    sealed class ConnectionState {
        object Idle : ConnectionState()
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object Reconnecting : ConnectionState()
        data class Error(val type: WebSocketError, val throwable: Throwable?) : ConnectionState()
    }
    
    /**
     * Enum representing different types of WebSocket errors
     */
    enum class WebSocketError {
        NETWORK_ERROR,
        TIMEOUT,
        PARSING_ERROR,
        NOT_CONNECTED,
        SEND_ERROR,
        NO_PREVIOUS_CONNECTION,
        FLOW_ERROR,
        UNKNOWN_ERROR
    }
}