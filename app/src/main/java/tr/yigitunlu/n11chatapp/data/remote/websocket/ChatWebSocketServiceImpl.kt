package tr.yigitunlu.n11chatapp.data.remote.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import tr.yigitunlu.n11chatapp.data.remote.dto.ChatStepDto
import tr.yigitunlu.n11chatapp.data.remote.websocket.ChatWebSocketClient.ConnectionState
import tr.yigitunlu.n11chatapp.data.remote.websocket.ChatWebSocketClient.WebSocketError
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Implementation of ChatWebSocketClient that handles WebSocket connections
 * and chat flow management
 */
@Singleton
class ChatWebSocketServiceImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @Named("websocket_url") private val websocketUrl: String,
    private val jsonContent: String,
    private val gson: Gson
) : ChatWebSocketClient {

    companion object {
        private const val TAG = "ChatWebSocketServiceImpl"
        private const val RECONNECT_DELAY_MS = 3000L
        private const val MAX_RECONNECT_ATTEMPTS = 5
    }

    // Connection state
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: StateFlow<ConnectionState> = _connectionState

    // WebSocket instance
    private var webSocket: WebSocket? = null
    private val isConnected = AtomicBoolean(false)
    private var reconnectAttempts = 0
    private val shouldReconnect = AtomicBoolean(false)

    // Coroutine scope for managing background tasks
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Chat flow steps loaded from JSON
    private val chatFlowSteps : Map<String, JsonObject>

    init {
        val steps: List<JsonObject> = loadSteps()
        chatFlowSteps = steps.associateBy { step ->
            val stepId = step.get("step").asString
            stepId
        }
    }

    private fun loadSteps(): List<JsonObject> {
        val type = object : TypeToken<List<JsonObject>>() {}.type
        val steps: List<JsonObject> = gson.fromJson(jsonContent, type)
        return steps
    }

    /**
     * Connect to the WebSocket server and receive messages
     * @return Flow of ChatStepDto objects received from the server
     */
    override fun connect(): Flow<ChatStepDto> = callbackFlow {
        if (isConnected.get()) {
            Log.d(TAG, "Already connected to WebSocket")
            close()
            return@callbackFlow
        }

        _connectionState.value = ConnectionState.Connecting
        shouldReconnect.set(true)

        val request = Request.Builder()
            .url(websocketUrl)
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                isConnected.set(true)
                reconnectAttempts = 0
                _connectionState.value = ConnectionState.Connected

                sendStep("step_1")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                try {
                    val chatStepDto = gson.fromJson(text, ChatStepDto::class.java)
                    serviceScope.launch {
                        trySend(chatStepDto)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message: ${e.message}", e)
                    _connectionState.value = ConnectionState.Error(WebSocketError.PARSING_ERROR, e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code, $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code, $reason")
                isConnected.set(false)
                _connectionState.value = ConnectionState.Disconnected

                if (shouldReconnect.get()) {
                    attemptReconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}", t)
                isConnected.set(false)
                _connectionState.value = ConnectionState.Error(WebSocketError.NETWORK_ERROR, t)

                if (shouldReconnect.get()) {
                    attemptReconnect()
                }
            }
        }

        webSocket = okHttpClient.newWebSocket(request, listener)

        awaitClose {
            Log.d(TAG, "Flow closed, closing WebSocket")
            webSocket?.close(1000, "Flow closed")
            webSocket = null
        }
    }.catch { e ->
        Log.e(TAG, "Error in WebSocket flow: ${e.message}", e)
        _connectionState.value = ConnectionState.Error(WebSocketError.FLOW_ERROR, e)
    }.flowOn(Dispatchers.IO)

    /**
     * Attempt to reconnect to the WebSocket server
     */
    private fun attemptReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Log.e(TAG, "Max reconnect attempts reached")
            shouldReconnect.set(false)
            _connectionState.value = ConnectionState.Error(
                WebSocketError.NO_PREVIOUS_CONNECTION,
                null
            )
            return
        }

        reconnectAttempts++
        _connectionState.value = ConnectionState.Reconnecting

        serviceScope.launch {
            Log.d(TAG, "Attempting to reconnect (${reconnectAttempts}/$MAX_RECONNECT_ATTEMPTS)")
            delay(RECONNECT_DELAY_MS)
            connect()
        }
    }

    /**
     * Send a specific step by name
     * @param stepName The name of the step to send (e.g., "step_2")
     * @return true if the step was sent successfully, false otherwise
     */
    override fun sendStep(stepName: String): Boolean {
        val step = chatFlowSteps[stepName]
        if (step == null) {
            Log.e(TAG, "Step not found: $stepName")
            return false
        }

        return sendMessage(step.toString())
    }

    /**
     * Send a message to the WebSocket server
     * @param message The message to send
     * @return true if the message was sent successfully, false otherwise
     */
    private fun sendMessage(message: String): Boolean {
        if (!isConnected.get()) {
            Log.e(TAG, "Cannot send message, WebSocket is not connected")
            _connectionState.value = ConnectionState.Error(WebSocketError.NOT_CONNECTED, null)
            return false
        }

        return try {
            Log.d(TAG, "Sending message: $message")
            val result = webSocket?.send(message) ?: false
            if (!result) {
                Log.e(TAG, "Failed to send message")
                _connectionState.value = ConnectionState.Error(WebSocketError.SEND_ERROR, null)
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}", e)
            _connectionState.value = ConnectionState.Error(WebSocketError.SEND_ERROR, e)
            false
        }
    }

    /**
     * Disconnect from the WebSocket
     */
    override fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        shouldReconnect.set(false)
        webSocket?.close(1000, "Disconnected by user")
        webSocket = null
        isConnected.set(false)
        _connectionState.value = ConnectionState.Disconnected
        serviceScope.cancel()
    }
}
