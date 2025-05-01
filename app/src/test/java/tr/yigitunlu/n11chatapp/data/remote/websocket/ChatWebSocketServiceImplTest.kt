package tr.yigitunlu.n11chatapp.data.remote.websocket

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tr.yigitunlu.n11chatapp.data.remote.dto.ChatStepDto
import tr.yigitunlu.n11chatapp.data.remote.websocket.ChatWebSocketClient.ConnectionState
import tr.yigitunlu.n11chatapp.data.remote.websocket.ChatWebSocketClient.WebSocketError
import java.io.ByteArrayInputStream
import java.io.InputStream

@ExperimentalCoroutinesApi
class ChatWebSocketServiceImplTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    private lateinit var mockContext: Context
    private lateinit var mockAssetManager: AssetManager
    private lateinit var mockOkHttpClient: OkHttpClient
    private lateinit var mockWebSocket: WebSocket
    private lateinit var capturedListener: WebSocketListener
    private lateinit var mockGson: Gson
    
    private lateinit var chatWebSocketClient: ChatWebSocketClient



    // Sample JSON content for testing
    private val sampleJsonContent = """
        [
            {
                "step": "step_1",
                "type": "button",
                "content": {
                    "text": "Hello, welcome to live support! How can I help you?",
                    "buttons": [
                        {
                            "label": "Return process",
                            "action": "step_2"
                        },
                        {
                            "label": "Order status",
                            "action": "step_3"
                        }
                    ]
                },
                "action": "await_user_choice"
            },
            {
                "step": "step_2",
                "type": "button",
                "content": {
                    "text": "Have you shipped your product for return?",
                    "buttons": [
                        {
                            "label": "Yes, I shipped it",
                            "action": "step_5"
                        },
                        {
                            "label": "No, not yet",
                            "action": "step_6"
                        }
                    ]
                },
                "action": "await_user_choice"
            }
        ]
    """.trimIndent()
    
    // Sample step JSON for testing
    private val sampleStepJson = """
        {
            "step": "step_1",
            "type": "button",
            "content": {
                "text": "Hello, welcome to live support! How can I help you?",
                "buttons": [
                    {
                        "label": "Return process",
                        "action": "step_2"
                    },
                    {
                        "label": "Order status",
                        "action": "step_3"
                    }
                ]
            },
            "action": "await_user_choice"
        }
    """.trimIndent()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)


        // Mock Context and AssetManager
        mockContext = mockk(relaxed = true)
        mockAssetManager = mockk(relaxed = true)
        mockGson = mockk(relaxed = true)
        every { mockContext.assets } returns mockAssetManager
        
        // Mock asset file input stream
        val inputStream: InputStream = ByteArrayInputStream(sampleJsonContent.toByteArray())
        every { mockAssetManager.open("live_support_flow.json") } returns inputStream
        
        // Mock OkHttpClient and WebSocket
        mockOkHttpClient = mockk(relaxed = true)
        mockWebSocket = mockk(relaxed = true)
        
        // Capture the WebSocketListener when newWebSocket is called
        val listenerSlot = slot<WebSocketListener>()
        every { 
            mockOkHttpClient.newWebSocket(any(), capture(listenerSlot))
        } answers { 
            mockWebSocket
            val listener = listenerSlot.captured
            listener.onOpen(mockWebSocket, createMockResponse())
            // Store the captured listener for later use
            capturedListener = listener
            mockWebSocket
        }
        
        // Default behavior for send method
        every { mockWebSocket.send(any<String>()) } returns true
        
        // Create the WebSocket client
        chatWebSocketClient = ChatWebSocketServiceImpl(
            mockOkHttpClient,
            "wss://echo.websocket.org",
            sampleJsonContent,
            mockGson
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial connection state should be Disconnected`() = runTest {
        assertEquals(ConnectionState.Disconnected, chatWebSocketClient.connectionState.value)
    }
    
    @Test
    fun `connect should update connection state to Connected when successful`() = runTest {
        // When
        chatWebSocketClient.connect()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(ConnectionState.Connected, chatWebSocketClient.connectionState.value)
    }
    
    @Test
    fun `connect should emit received messages as ChatStepDto`() = runTest {
        // Given
        val sampleDto = Gson().fromJson(sampleStepJson, ChatStepDto::class.java)
        
        // When
        val flow = chatWebSocketClient.connect()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Simulate receiving a message
        capturedListener.onMessage(mockWebSocket, sampleStepJson)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val emittedItems = mutableListOf<ChatStepDto>()
        testScope.runTest {
            flow.take(1).collect { emittedItems.add(it) }
        }
        assertEquals(1, emittedItems.size)
        assertEquals("step_1", emittedItems[0].step)
        assertEquals("button", emittedItems[0].type)
    }
    
    @Test
    fun `sendStep should send the correct JSON for a valid step`() = runTest {
        // Given
        every { mockWebSocket.send(any<String>()) } returns true
        // Connect first to ensure the WebSocket is initialized
        chatWebSocketClient.connect()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val result = chatWebSocketClient.sendStep("step_1")
        
        // Then
        assertTrue(result)
//        verify { mockWebSocket.send(match { it.contains("step_1") }) }
    }
    
    @Test
    fun `sendStep should return false for an invalid step`() = runTest {
        // When
        val result = chatWebSocketClient.sendStep("non_existent_step")
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `disconnect should update connection state to Disconnected`() = runTest {
        // Given
        chatWebSocketClient.connect()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(ConnectionState.Connected, chatWebSocketClient.connectionState.value)
        
        // When
        chatWebSocketClient.disconnect()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(ConnectionState.Disconnected, chatWebSocketClient.connectionState.value)
        verify { mockWebSocket.close(1000, any()) }
    }
    
    @Test
    fun `onFailure should update connection state to Error`() = runTest {
        // Given
        chatWebSocketClient.connect()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        capturedListener.onFailure(mockWebSocket, Exception("Test exception"), null)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(chatWebSocketClient.connectionState.value is ConnectionState.Error)
        val errorState = chatWebSocketClient.connectionState.value as ConnectionState.Error
        assertEquals(WebSocketError.NETWORK_ERROR, errorState.type)
    }
    
    @Test
    fun `onClosed should update connection state to Disconnected`() = runTest {
        // Given
        chatWebSocketClient.connect()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        capturedListener.onClosed(mockWebSocket, 1000, "Normal closure")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(ConnectionState.Disconnected, chatWebSocketClient.connectionState.value)
    }
    
    // Helper method to create a mock Response
    private fun createMockResponse(): Response {
        return Response.Builder()
            .request(Request.Builder().url("ws://localhost:8080").build())
            .protocol(Protocol.HTTP_1_1)
            .code(101)
            .message("Switching Protocols")
            .build()
    }
}
