package com.spartanai.spartanaimedia.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString

@Serializable
sealed class SyncEvent {
    @Serializable data class Join(val roomId: String, val userId: String) : SyncEvent()
    @Serializable data class Play(val position: Long) : SyncEvent()
    @Serializable data class Pause(val position: Long) : SyncEvent()
    @Serializable data class Seek(val position: Long) : SyncEvent()
    @Serializable data class Heartbeat(val position: Long, val isPlaying: Boolean) : SyncEvent()
    @Serializable data class Message(val userId: String, val username: String, val text: String, val timestamp: Long = System.currentTimeMillis()) : SyncEvent()
    @Serializable data class Reaction(val userId: String, val emoji: String) : SyncEvent()
    @Serializable data class TrackChange(val trackType: Int, val trackIndex: Int) : SyncEvent()
}

class MediaSyncManager {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _incomingEvents = MutableStateFlow<SyncEvent?>(null)
    val incomingEvents = _incomingEvents.asStateFlow()

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus = _connectionStatus.asStateFlow()

    private var currentRoomId: String? = null

    fun connect(roomId: String, userId: String) {
        currentRoomId = roomId
        val request = Request.Builder()
            .url("wss://sync.spartanai.com/media?room=$roomId&user=$userId") // Simulated endpoint
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionStatus.value = true
                sendEvent(SyncEvent.Join(roomId, userId))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val event = json.decodeFromString<SyncEvent>(text)
                    _incomingEvents.value = event
                } catch (e: Exception) {}
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionStatus.value = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionStatus.value = false
            }
        })
    }

    fun sendEvent(event: SyncEvent) {
        val text = json.encodeToString(event)
        webSocket?.send(text)
    }

    fun disconnect() {
        webSocket?.close(1000, "User left")
        _connectionStatus.value = false
        currentRoomId = null
    }
}
