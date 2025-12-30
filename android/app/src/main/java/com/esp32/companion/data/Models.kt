package com.esp32.companion.data

/**
 * Represents a message in the ESP32 communication protocol
 */
data class ProtocolMessage(
    val type: String,
    val id: String,
    val payload: Map<String, Any?>
)

/**
 * Message types defined in the protocol
 */
object MessageType {
    const val REPL_CMD = "repl_cmd"
    const val REPL_RESPONSE = "repl_response"
    const val FILE_UPLOAD = "file_upload"
    const val FILE_CHUNK = "file_chunk"
    const val FILE_ACK = "file_ack"
    const val FILE_DOWNLOAD = "file_download"
    const val FILE_DATA = "file_data"
    const val FILE_LIST = "file_list"
    const val FILE_LIST_RESPONSE = "file_list_response"
    const val TELEMETRY = "telemetry"
    const val TELEMETRY_REQUEST = "telemetry_request"
    const val NOTIFICATION = "notification"
    const val PING = "ping"
    const val PONG = "pong"
    const val STATUS_REQUEST = "status_request"
    const val STATUS_RESPONSE = "status_response"
    const val ERROR = "error"
}

/**
 * Represents a Bluetooth device
 */
data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val paired: Boolean = false
)

/**
 * Represents connection state
 */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * Represents a file on the ESP32
 */
data class FileInfo(
    val name: String,
    val size: Long,
    val type: FileType
)

enum class FileType {
    FILE,
    DIR
}

/**
 * Represents telemetry data from ESP32
 */
data class TelemetryData(
    val timestamp: Long,
    val temperature: Float? = null,
    val humidity: Float? = null,
    val cpuFreq: Int? = null,
    val memFree: Int? = null,
    val uptime: Long? = null
)

/**
 * Represents a notification from ESP32
 */
data class Notification(
    val title: String,
    val message: String,
    val level: NotificationLevel,
    val timestamp: Long
)

enum class NotificationLevel {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}
