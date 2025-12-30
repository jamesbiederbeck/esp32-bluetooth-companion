# ESP32 Bluetooth Companion Protocol Specification

## Overview
This document defines the communication protocol between the Android companion app and ESP32 devices running MicroPython over Bluetooth Classic (SPP - Serial Port Profile).

## Connection Details
- **Protocol**: Bluetooth Classic Serial Port Profile (SPP)
- **Service UUID**: `00001101-0000-1000-8000-00805F9B34FB` (Standard SPP UUID)
- **Encoding**: UTF-8
- **Line Endings**: `\n` (newline)

## Message Format
All messages follow a JSON-based format for easy parsing on both sides:

```json
{
  "type": "message_type",
  "id": "unique_message_id",
  "payload": {}
}
```

### Fields
- `type`: String identifying the message type
- `id`: Unique identifier for request-response correlation
- `payload`: Object containing message-specific data

## Message Types

### 1. REPL (Read-Eval-Print Loop)

#### REPL Command (Android → ESP32)
```json
{
  "type": "repl_cmd",
  "id": "msg_001",
  "payload": {
    "command": "print('hello')"
  }
}
```

#### REPL Response (ESP32 → Android)
```json
{
  "type": "repl_response",
  "id": "msg_001",
  "payload": {
    "output": "hello\n",
    "error": null
  }
}
```

#### REPL Error Response
```json
{
  "type": "repl_response",
  "id": "msg_001",
  "payload": {
    "output": "",
    "error": "SyntaxError: invalid syntax"
  }
}
```

### 2. File Transfer

#### File Upload Request (Android → ESP32)
```json
{
  "type": "file_upload",
  "id": "msg_002",
  "payload": {
    "path": "/lib/mymodule.py",
    "size": 1024,
    "chunks": 5
  }
}
```

#### File Upload Chunk (Android → ESP32)
```json
{
  "type": "file_chunk",
  "id": "msg_002",
  "payload": {
    "chunk_num": 0,
    "data": "base64_encoded_data"
  }
}
```

#### File Upload Acknowledgment (ESP32 → Android)
```json
{
  "type": "file_ack",
  "id": "msg_002",
  "payload": {
    "chunk_num": 0,
    "status": "received"
  }
}
```

#### File Download Request (Android → ESP32)
```json
{
  "type": "file_download",
  "id": "msg_003",
  "payload": {
    "path": "/main.py"
  }
}
```

#### File Download Response (ESP32 → Android)
```json
{
  "type": "file_data",
  "id": "msg_003",
  "payload": {
    "path": "/main.py",
    "size": 512,
    "chunks": 2
  }
}
```

#### File List Request (Android → ESP32)
```json
{
  "type": "file_list",
  "id": "msg_004",
  "payload": {
    "path": "/"
  }
}
```

#### File List Response (ESP32 → Android)
```json
{
  "type": "file_list_response",
  "id": "msg_004",
  "payload": {
    "files": [
      {"name": "boot.py", "size": 128, "type": "file"},
      {"name": "lib", "size": 0, "type": "dir"}
    ]
  }
}
```

### 3. Telemetry

#### Telemetry Data (ESP32 → Android)
```json
{
  "type": "telemetry",
  "id": "telem_001",
  "payload": {
    "timestamp": 1234567890,
    "temperature": 25.5,
    "humidity": 60.2,
    "cpu_freq": 160,
    "mem_free": 45000,
    "uptime": 3600
  }
}
```

#### Telemetry Request (Android → ESP32)
```json
{
  "type": "telemetry_request",
  "id": "msg_005",
  "payload": {
    "interval": 5000
  }
}
```

### 4. Notifications

#### Notification (ESP32 → Android)
```json
{
  "type": "notification",
  "id": "notif_001",
  "payload": {
    "title": "Alert",
    "message": "Temperature threshold exceeded",
    "level": "warning",
    "timestamp": 1234567890
  }
}
```

#### Notification Levels
- `info`: Informational message
- `warning`: Warning message
- `error`: Error message
- `success`: Success message

### 5. System Commands

#### Ping (Android → ESP32)
```json
{
  "type": "ping",
  "id": "msg_006",
  "payload": {}
}
```

#### Pong (ESP32 → Android)
```json
{
  "type": "pong",
  "id": "msg_006",
  "payload": {
    "timestamp": 1234567890
  }
}
```

#### Status Request (Android → ESP32)
```json
{
  "type": "status_request",
  "id": "msg_007",
  "payload": {}
}
```

#### Status Response (ESP32 → Android)
```json
{
  "type": "status_response",
  "id": "msg_007",
  "payload": {
    "device_name": "ESP32-DEV",
    "version": "1.0.0",
    "uptime": 3600,
    "mem_free": 45000
  }
}
```

## Error Handling

### Error Response (Any Direction)
```json
{
  "type": "error",
  "id": "msg_xxx",
  "payload": {
    "code": "FILE_NOT_FOUND",
    "message": "The requested file does not exist"
  }
}
```

### Error Codes
- `INVALID_MESSAGE`: Message format is invalid
- `UNKNOWN_TYPE`: Message type is not recognized
- `FILE_NOT_FOUND`: Requested file does not exist
- `PERMISSION_DENIED`: Operation not permitted
- `TIMEOUT`: Operation timed out
- `INTERNAL_ERROR`: Internal error occurred

## Implementation Notes

1. **Chunk Size**: For file transfers, use a maximum chunk size of 512 bytes (base64 encoded)
2. **Timeout**: Implement 10-second timeout for responses
3. **Buffering**: Messages should be buffered on both sides to handle network delays
4. **Connection**: Maintain persistent connection during active session
5. **Reconnection**: Implement automatic reconnection with exponential backoff
