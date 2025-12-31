# ESP32 Bluetooth Companion

Android companion application for ESP32 devices running MicroPython, inspired by the Flipper Zero companion app. Provides REPL, file transfer, telemetry, and notifications over Bluetooth.

<div align="center">

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://www.android.com/)
[![MicroPython](https://img.shields.io/badge/MicroPython-1.19%2B-blue.svg)](https://micropython.org/)

</div>

## ✨ Features

- 🔵 **BLE Connectivity** - Seamless connection to ESP32 via Bluetooth Low Energy (Nordic UART Service)
- 💻 **REPL Interface** - Execute Python commands remotely on your ESP32
- 📁 **File Transfer** - Upload and download files bidirectionally
- 📊 **Real-time Telemetry** - Monitor CPU, memory, temperature, and custom sensors
- 🔔 **Push Notifications** - Receive alerts from ESP32 to your Android device
- 🎨 **Modern UI** - Built with Jetpack Compose and Material Design 3

## 📱 Screenshots

| Device List | REPL Console | File Browser | Telemetry |
|------------|--------------|--------------|-----------|
| *Connect to ESP32* | *Run Python commands* | *Manage files* | *View system stats* |

## 🚀 Quick Start

> **Note:** The MicroPython module provides a complete BLE implementation using the Nordic UART Service. See [micropython/README.md](micropython/README.md) for details.

### Android App

1. Download and install the APK from [Releases](../../releases)
2. Enable Bluetooth and pair your ESP32 in Android settings
3. Launch the app and grant required permissions
4. Select your device from the list to connect

For building from source, see [android/README.md](android/README.md)

### ESP32 MicroPython Module

1. Upload the companion module to your ESP32:
   ```python
   # Using ampy
   ampy --port /dev/ttyUSB0 put micropython/lib/companion.py /lib/companion.py
   ```

2. Create a simple script:
   ```python
   from companion import Companion
   
   comp = Companion(device_name="ESP32-Demo")
   comp.start()
   comp.notify("Hello", "Companion is running!", "success")
   
   while True:
       comp.loop()
   ```

3. Connect from the Android app!

For detailed instructions, see [micropython/README.md](micropython/README.md)

## 📖 Documentation

- **[Communication Protocol](PROTOCOL.md)** - Detailed protocol specification
- **[Android App Guide](android/README.md)** - Building and using the Android app
- **[MicroPython Module](micropython/README.md)** - ESP32 module documentation
- **[Examples](micropython/examples/)** - Example MicroPython scripts

## 🏗️ Architecture

### Android App (Kotlin + Jetpack Compose)

```
┌─────────────────────────────────────┐
│         MainActivity                │
│  ┌───────────────────────────────┐  │
│  │      MainViewModel            │  │
│  │  • Device Management          │  │
│  │  • Connection State           │  │
│  │  • REPL Command Handling      │  │
│  └───────────┬───────────────────┘  │
│              │                       │
│  ┌───────────▼───────────────────┐  │
│  │  BluetoothConnectionManager   │  │
│  │  • SPP Connection             │  │
│  │  • Data TX/RX                 │  │
│  └───────────┬───────────────────┘  │
│              │                       │
│  ┌───────────▼───────────────────┐  │
│  │     ProtocolHandler           │  │
│  │  • Message Encoding           │  │
│  │  • Message Routing            │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

### ESP32 Module (MicroPython)

```
┌─────────────────────────────────────┐
│       Companion Module              │
│  ┌───────────────────────────────┐  │
│  │  Message Handlers             │  │
│  │  • REPL Execution             │  │
│  │  • File Operations            │  │
│  │  • Telemetry Collection       │  │
│  └───────────┬───────────────────┘  │
│              │                       │
│  ┌───────────▼───────────────────┐  │
│  │  Bluetooth SPP                │  │
│  │  • JSON Protocol              │  │
│  │  • Message TX/RX              │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

### Communication Protocol

All messages use JSON format over Bluetooth Serial Port Profile (SPP):

```json
{
  "type": "message_type",
  "id": "unique_id",
  "payload": { }
}
```

See [PROTOCOL.md](PROTOCOL.md) for complete specification.

## 🛠️ Development

### Prerequisites

**Android App:**
- Android Studio Arctic Fox or later
- JDK 17+
- Android SDK 34

**ESP32 Module:**
- ESP32 with MicroPython firmware
- Python 3.x for development tools
- ampy, rshell, or mpremote for file transfer

### Building

```bash
# Build Android app
cd android
./gradlew assembleDebug

# Install to device
./gradlew installDebug

# Run tests
./gradlew test
```

### Project Structure

```
esp32-bluetooth-companion/
├── android/              # Android app source
│   ├── app/
│   │   └── src/main/
│   │       └── java/com/esp32/companion/
│   └── build.gradle.kts
├── micropython/          # MicroPython module
│   ├── lib/
│   │   └── companion.py
│   └── examples/
├── PROTOCOL.md           # Protocol specification
├── README.md            # This file
└── LICENSE
```

## 🧪 Examples

### Send a Notification

```python
from companion import notify

# Quick notification
notify("Alert", "Temperature exceeded threshold!", "warning")
```

### Execute REPL Command

```kotlin
// In Android app
viewModel.sendReplCommand("print('Hello from Android!')")
```

### Monitor Temperature

```python
from companion import Companion
import time

comp = Companion()
comp.start()

while True:
    temp = read_sensor()
    if temp > 30:
        comp.notify("Alert", f"Temp: {temp}°C", "warning")
    comp.loop()
    time.sleep(5)
```

## 📋 Requirements

### Android
- Android 8.0 (API 26) or higher
- Bluetooth Classic support
- Location permission (for scanning)

### ESP32
- ESP32 or ESP32-S series
- MicroPython 1.19 or later
- Bluetooth Classic enabled in firmware

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Inspired by the Flipper Zero companion app
- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Powered by [MicroPython](https://micropython.org/)

## 📞 Support

- **Issues**: [GitHub Issues](../../issues)
- **Discussions**: [GitHub Discussions](../../discussions)

---

<div align="center">
Made with ❤️ for the ESP32 community
</div> 
