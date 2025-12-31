package com.esp32.companion.bluetooth

import android.util.Log
import com.esp32.companion.data.MessageType
import com.esp32.companion.data.ProtocolMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID

/**
 * Handles the ESP32 communication protocol
 */
class ProtocolHandler(private val connectionManager: BluetoothConnectionManager) {
    
    companion object {
        private const val TAG = "ProtocolHandler"
    }
    
    private val pendingResponses = mutableMapOf<String, (ProtocolMessage) -> Unit>()
    
    private val _replOutput = MutableStateFlow<String>("")
    val replOutput: StateFlow<String> = _replOutput.asStateFlow()
    
    init {
        // Listen for incoming messages
        observeIncomingMessages()
    }
    
    /**
     * Generate a unique message ID
     */
    private fun generateMessageId(): String {
        return "msg_${UUID.randomUUID().toString().take(8)}"
    }
    
    /**
     * Send a REPL command
     */
    suspend fun sendReplCommand(command: String): Result<Unit> {
        val messageId = generateMessageId()
        val message = ProtocolMessage(
            type = MessageType.REPL_CMD,
            id = messageId,
            payload = mapOf("command" to command)
        )
        
        return sendMessage(message)
    }
    
    /**
     * Request file list
     */
    suspend fun requestFileList(path: String = "/"): Result<Unit> {
        val messageId = generateMessageId()
        val message = ProtocolMessage(
            type = MessageType.FILE_LIST,
            id = messageId,
            payload = mapOf("path" to path)
        )
        
        return sendMessage(message)
    }
    
    /**
     * Request device status
     */
    suspend fun requestStatus(): Result<Unit> {
        val messageId = generateMessageId()
        val message = ProtocolMessage(
            type = MessageType.STATUS_REQUEST,
            id = messageId,
            payload = emptyMap()
        )
        
        return sendMessage(message)
    }
    
    /**
     * Send ping
     */
    suspend fun sendPing(): Result<Unit> {
        val messageId = generateMessageId()
        val message = ProtocolMessage(
            type = MessageType.PING,
            id = messageId,
            payload = emptyMap()
        )
        
        return sendMessage(message)
    }
    
    /**
     * Request telemetry with specified interval
     */
    suspend fun requestTelemetry(intervalMs: Int = 5000): Result<Unit> {
        val messageId = generateMessageId()
        val message = ProtocolMessage(
            type = MessageType.TELEMETRY_REQUEST,
            id = messageId,
            payload = mapOf("interval" to intervalMs)
        )
        
        return sendMessage(message)
    }
    
    /**
     * Send a protocol message
     */
    private suspend fun sendMessage(message: ProtocolMessage): Result<Unit> {
        val json = encodeMessage(message)
        return connectionManager.sendData(json)
    }
    
    /**
     * Encode a protocol message to JSON
     */
    private fun encodeMessage(message: ProtocolMessage): String {
        val json = JSONObject()
        json.put("type", message.type)
        json.put("id", message.id)
        json.put("payload", JSONObject(message.payload))
        return json.toString()
    }
    
    /**
     * Decode a JSON string to a protocol message
     */
    private fun decodeMessage(json: String): ProtocolMessage? {
        return try {
            val obj = JSONObject(json)
            val type = obj.getString("type")
            val id = obj.getString("id")
            val payloadObj = obj.getJSONObject("payload")
            
            val payload = mutableMapOf<String, Any?>()
            payloadObj.keys().forEach { key ->
                payload[key] = payloadObj.get(key)
            }
            
            ProtocolMessage(type, id, payload)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding message: $json", e)
            null
        }
    }
    
    /**
     * Observe incoming messages from the connection manager
     * 
     * TODO: This needs to be connected to the actual Bluetooth data stream.
     * In a complete implementation, this would:
     * 1. Collect flow from connectionManager.receivedData
     * 2. Parse JSON strings into ProtocolMessage objects
     * 3. Call handleIncomingMessage for each parsed message
     * 
     * Example implementation:
     * viewModelScope.launch {
     *     connectionManager.receivedData.collect { jsonString ->
     *         decodeMessage(jsonString)?.let { message ->
     *             handleIncomingMessage(message)
     *         }
     *     }
     * }
     */
    private fun observeIncomingMessages() {
        // This is a placeholder - needs implementation
    }
    
    /**
     * Handle an incoming message
     */
    private fun handleIncomingMessage(message: ProtocolMessage) {
        when (message.type) {
            MessageType.REPL_RESPONSE -> handleReplResponse(message)
            MessageType.FILE_LIST_RESPONSE -> handleFileListResponse(message)
            MessageType.TELEMETRY -> handleTelemetry(message)
            MessageType.NOTIFICATION -> handleNotification(message)
            MessageType.PONG -> handlePong(message)
            MessageType.STATUS_RESPONSE -> handleStatusResponse(message)
            MessageType.ERROR -> handleError(message)
            else -> Log.w(TAG, "Unknown message type: ${message.type}")
        }
        
        // If there's a pending response callback, invoke it
        pendingResponses.remove(message.id)?.invoke(message)
    }
    
    private fun handleReplResponse(message: ProtocolMessage) {
        val output = message.payload["output"] as? String ?: ""
        val error = message.payload["error"] as? String
        
        val result = if (error != null) {
            "Error: $error"
        } else {
            output
        }
        
        _replOutput.value = result
    }
    
    private fun handleFileListResponse(message: ProtocolMessage) {
        // Implementation would update file list state
        Log.d(TAG, "File list response: ${message.payload}")
    }
    
    private fun handleTelemetry(message: ProtocolMessage) {
        // Implementation would update telemetry state
        Log.d(TAG, "Telemetry: ${message.payload}")
    }
    
    private fun handleNotification(message: ProtocolMessage) {
        // Implementation would show notification
        Log.d(TAG, "Notification: ${message.payload}")
    }
    
    private fun handlePong(message: ProtocolMessage) {
        Log.d(TAG, "Pong received")
    }
    
    private fun handleStatusResponse(message: ProtocolMessage) {
        // Implementation would update status state
        Log.d(TAG, "Status response: ${message.payload}")
    }
    
    private fun handleError(message: ProtocolMessage) {
        val errorCode = message.payload["code"] as? String
        val errorMessage = message.payload["message"] as? String
        Log.e(TAG, "Error received: $errorCode - $errorMessage")
    }
}
