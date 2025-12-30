# MicroPython Module for ESP32 Companion

This directory contains the MicroPython module for the ESP32 Companion Android app.

## ⚠️ Important: Bluetooth Implementation Required

This module provides a **complete protocol implementation** but requires you to add platform-specific Bluetooth SPP code. The module currently:

✅ **Implemented:**
- Complete JSON-based communication protocol
- Message encoding/decoding
- REPL command execution (with security restrictions)
- File transfer handlers
- Telemetry collection
- Notification system
- All message type handlers

⚠️ **Requires Implementation:**
- Bluetooth SPP connection setup (hardware-specific)
- Bluetooth data transmission
- Connection state management

The Bluetooth implementation varies significantly between ESP32 variants and MicroPython versions, so it's left for users to implement based on their specific platform.

## Installation

### Important Note

The companion module provides a **template implementation** of the communication protocol. The Bluetooth SPP (Serial Port Profile) connection code is platform-specific and must be implemented by the user based on their ESP32 variant and MicroPython version.

The module currently:
- ✅ Implements the JSON-based protocol
- ✅ Handles all message types
- ✅ Provides the API structure
- ⚠️ Requires Bluetooth SPP implementation (platform-specific)

### Prerequisites

- ESP32 development board (ESP32, ESP32-S2, ESP32-S3, or ESP32-C3)
- MicroPython firmware with Bluetooth support
- File transfer tool (ampy, rshell, mpremote, or Thonny)

### Step 1: Upload the Companion Module

1. **Upload the companion module to your ESP32:**

   ```bash
   # Using ampy (https://github.com/scientifichackers/ampy)
   ampy --port /dev/ttyUSB0 put lib/companion.py /lib/companion.py
   ```

   Or use any other file transfer tool like rshell, mpremote, or the Thonny IDE.

2. **Verify installation:**

   ```python
   >>> import companion
   >>> print(companion.__doc__)
   ```

## Quick Start

### Basic Usage

```python
from companion import Companion

# Create and start the companion
comp = Companion(device_name="ESP32-MyDevice")
comp.start()

# Send a notification
comp.notify("Hello", "Companion is running!", "success")

# Main loop
while True:
    comp.loop()  # Process companion tasks
    # Your code here
    time.sleep(0.1)
```

### Sending Notifications

```python
# Using the companion instance
comp.notify("Alert", "Temperature high!", "warning")

# Or use the convenience function
from companion import notify
notify("Quick Alert", "Something happened", "info")
```

Notification levels:
- `info` - Informational message (default)
- `success` - Success message
- `warning` - Warning message
- `error` - Error message

### Handling REPL Commands

REPL commands are automatically handled by the companion module. The Android app can send Python commands that will be executed on the ESP32.

### File Transfer

File listing and transfer are handled automatically. The Android app can:
- List files on the ESP32
- Download files from the ESP32
- Upload files to the ESP32

### Telemetry

Telemetry is sent automatically when requested by the Android app:

```python
# Request telemetry every 5 seconds
comp.request_telemetry(5000)
```

The module automatically collects:
- CPU frequency
- Free memory
- Uptime
- Temperature (if sensor available)

## API Reference

### Companion Class

#### `__init__(device_name="ESP32-Companion")`

Initialize the companion module.

**Parameters:**
- `device_name` (str): Bluetooth device name to advertise

#### `start()`

Start the companion service and begin listening for connections.

#### `notify(title, message, level="info")`

Send a notification to the Android app.

**Parameters:**
- `title` (str): Notification title
- `message` (str): Notification message
- `level` (str): Notification level (info, warning, error, success)

#### `send_telemetry()`

Manually send telemetry data to the Android app.

#### `loop()`

Main processing loop. Call this regularly (e.g., every 100ms) to handle:
- Periodic telemetry updates
- Incoming message processing
- Connection maintenance

### Convenience Functions

#### `notify(title, message, level="info")`

Send a notification without creating a Companion instance.

**Parameters:**
- `title` (str): Notification title
- `message` (str): Notification message
- `level` (str): Notification level

## Examples

See the `examples/` directory for complete examples:

- `basic_example.py` - Basic companion usage
- `temperature_monitor.py` - Temperature monitoring with alerts
- `notification_examples.py` - Different notification types

## Bluetooth Setup

The companion module uses Bluetooth Classic (SPP - Serial Port Profile) for communication.

### Pairing

1. Make sure Bluetooth is enabled on your ESP32
2. Open the Android app
3. Scan for devices
4. Select your ESP32 device
5. Pair if prompted

### Connection

Once paired, the Android app will automatically connect when in range.

## Protocol

The module implements a JSON-based protocol for communication. See `PROTOCOL.md` in the root directory for full details.

## Troubleshooting

### Module not found

Make sure the module is uploaded to `/lib/companion.py` on your ESP32:

```python
import os
print(os.listdir('/lib'))  # Should show 'companion.py'
```

### Bluetooth not working

- Verify Bluetooth is enabled on ESP32
- Check that the device is paired
- Ensure no other device is connected to the ESP32

### Connection drops

- Check power supply (ESP32 may reset if underpowered)
- Reduce distance between devices
- Check for interference from other devices

## Requirements

- ESP32 with MicroPython firmware
- MicroPython 1.19 or later recommended
- Bluetooth support enabled in firmware

## License

MIT License - See LICENSE file in the root directory
