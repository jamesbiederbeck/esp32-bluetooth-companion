# ESP32 Bluetooth Companion Protocol Specification

## Overview
This document defines the message protocol used between the Android companion app and the ESP32
running the MicroPython companion module. The protocol is transport-agnostic and can be used over
BLE or Classic Bluetooth serial.

## Framing
Each packet is framed as:

```
| SOF | Version | Type | Flags | Length (u16 LE) | Payload | CRC16 |
```

- **SOF**: `0x7E`
- **Version**: `0x01`
- **Type**: Message type (see below)
- **Flags**: Bitfield
  - bit0: `ACK_REQUIRED`
  - bit1: `IS_RESPONSE`
  - bit2: `IS_ERROR`
- **Length**: Payload length in bytes
- **Payload**: Type-specific payload
- **CRC16**: CRC16-CCITT over Version..Payload

## Message Types
| Type | Name | Purpose |
| ---- | ---- | ------- |
| `0x01` | HELLO | Capability negotiation and device info |
| `0x02` | ACK | Acknowledgement of a request |
| `0x03` | ERROR | Error response |
| `0x10` | REPL_COMMAND | Command to execute in REPL |
| `0x11` | REPL_OUTPUT | Output from REPL execution |
| `0x20` | FILE_PUT | Upload file chunk |
| `0x21` | FILE_GET | Request file chunk |
| `0x22` | FILE_CHUNK | File data response |
| `0x30` | TELEMETRY | Telemetry snapshot |
| `0x31` | NOTIFY | Notification event |

## Payload Formats
### HELLO
```
struct {
  u8 device_name_len;
  u8 device_name[device_name_len];
  u8 firmware_version_len;
  u8 firmware_version[firmware_version_len];
  u8 capabilities; // bitmask
}
```

### REPL_COMMAND
```
struct {
  u32 request_id;
  u16 command_len;
  u8 command[command_len];
}
```

### REPL_OUTPUT
```
struct {
  u32 request_id;
  u8 stream; // 0=stdout,1=stderr
  u16 output_len;
  u8 output[output_len];
}
```

### FILE_PUT
```
struct {
  u32 transfer_id;
  u16 path_len;
  u8 path[path_len];
  u32 offset;
  u16 data_len;
  u8 data[data_len];
  u8 is_last; // 0/1
}
```

### FILE_GET
```
struct {
  u32 transfer_id;
  u16 path_len;
  u8 path[path_len];
  u32 offset;
  u16 max_len;
}
```

### FILE_CHUNK
```
struct {
  u32 transfer_id;
  u32 offset;
  u16 data_len;
  u8 data[data_len];
  u8 is_last;
}
```

### TELEMETRY
```
struct {
  u32 timestamp_ms;
  i16 temperature_c_x10;
  u16 voltage_mv;
  u8 rssi;
  u8 status_flags;
}
```

### NOTIFY
```
struct {
  u32 timestamp_ms;
  u8 level; // 0=info,1=warn,2=error
  u16 message_len;
  u8 message[message_len];
}
```

## Error Codes
| Code | Meaning |
| ---- | ------- |
| `0x01` | UNKNOWN_TYPE |
| `0x02` | BAD_CRC |
| `0x03` | MALFORMED_PACKET |
| `0x04` | UNSUPPORTED |

## Notes
- BLE should use a packet size up to 180 bytes to avoid fragmentation.
- For Classic Bluetooth, RFCOMM channel 1 is recommended.
- ACKs should be sent within 500ms of receiving a request with `ACK_REQUIRED` set.
