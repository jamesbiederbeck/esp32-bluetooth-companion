# ESP32 Companion Android App

Android companion application for ESP32 devices running MicroPython.

## Features

- 🔵 **BLE connectivity** - Connect to ESP32 devices via Bluetooth Low Energy (Nordic UART Service)
- 💻 **REPL interface** - Execute Python commands remotely
- 📁 **File transfer** - Upload and download files to/from ESP32
- 📊 **Telemetry** - Monitor system stats (CPU, memory, temperature)
- 🔔 **Notifications** - Receive alerts from ESP32

## Requirements

- Android 8.0 (API level 26) or higher
- Bluetooth Low Energy (BLE) support
- Location permission (required for Bluetooth scanning on Android 10+)

## Building

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 17 or later
- Android SDK 34

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/jamesbiederbeck/esp32-bluetooth-companion.git
   cd esp32-bluetooth-companion/android
   ```

2. Open the project in Android Studio

3. Sync Gradle files

4. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```

5. Install on device:
   ```bash
   ./gradlew installDebug
   ```

## Usage

### First Time Setup

1. **Enable Bluetooth** on your Android device
2. **Upload companion module** to your ESP32 and start it
3. Launch the **ESP32 Companion** app
4. Grant required permissions when prompted

### Connecting to ESP32

1. Make sure your ESP32 is running the companion module (`comp.start()`)
2. Open the app and go to the **Devices** tab
3. Tap **Scan BLE** to search for devices advertising the Nordic UART Service
4. Your ESP32 should appear in the list
5. Tap your device to connect
6. Wait for connection and service discovery to complete

### Using the REPL

1. Connect to your ESP32 device
2. Navigate to the **REPL** tab
3. Enter Python commands in the input field
4. Tap the send button to execute
5. View output in the console area

**Quick commands:**
- Tap "CPU Freq" to check CPU frequency
- Tap "Free Mem" to check available memory

### Managing Files

1. Navigate to the **Files** tab
2. Browse the ESP32 filesystem
3. Use the action buttons to:
   - Upload files to ESP32
   - Download files from ESP32
   - Create new directories
   - Delete files

### Viewing Telemetry

1. Navigate to the **Telemetry** tab
2. Tap "Start Telemetry" to begin receiving data
3. View real-time system information:
   - CPU frequency and temperature
   - Memory usage
   - Uptime
   - Sensor data

### Receiving Notifications

Notifications from the ESP32 will appear automatically:
- In the app's notification area
- As Android system notifications

## Architecture

### Project Structure

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/esp32/companion/
│   │   │   ├── bluetooth/
│   │   │   │   ├── BluetoothConnectionManager.kt
│   │   │   │   └── ProtocolHandler.kt
│   │   │   ├── data/
│   │   │   │   └── Models.kt
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── DeviceListScreen.kt
│   │   │   │   │   ├── ReplScreen.kt
│   │   │   │   │   ├── FilesScreen.kt
│   │   │   │   │   └── TelemetryScreen.kt
│   │   │   │   ├── theme/
│   │   │   │   └── MainScreen.kt
│   │   │   ├── viewmodel/
│   │   │   │   └── MainViewModel.kt
│   │   │   └── MainActivity.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

### Key Components

#### BluetoothConnectionManager
Manages BLE connections:
- BLE device scanning with service UUID filtering
- GATT client operations
- Nordic UART Service discovery
- Characteristic notifications
- Data transmission with chunking

#### ProtocolHandler
Implements the communication protocol:
- Message encoding/decoding
- Request/response correlation
- Message type handling

#### MainViewModel
Central state management:
- Device list
- Connection state
- REPL output
- Telemetry data

#### UI Screens
- **DeviceListScreen**: Device discovery and connection
- **ReplScreen**: Interactive Python REPL
- **FilesScreen**: File browser and transfer
- **TelemetryScreen**: System monitoring

## Development

### Code Style

This project follows Android development best practices:
- Kotlin for all code
- Jetpack Compose for UI
- Material Design 3 components
- MVVM architecture pattern
- Coroutines for async operations
- StateFlow for reactive state management

### Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

### Linting

Run lint checks:
```bash
./gradlew lint
```

## Protocol

The app communicates with ESP32 using a JSON-based protocol over Bluetooth Low Energy (Nordic UART Service). See `PROTOCOL.md` for detailed specification.

## Permissions

The app requires the following permissions:

- `BLUETOOTH` - Bluetooth communication (Android < 12)
- `BLUETOOTH_ADMIN` - Bluetooth management (Android < 12)
- `BLUETOOTH_CONNECT` - Bluetooth connection (Android 12+)
- `BLUETOOTH_SCAN` - Bluetooth scanning (Android 12+)
- `ACCESS_FINE_LOCATION` - Required for BLE scanning

## Troubleshooting

### Cannot find devices
- Ensure Bluetooth is enabled
- Grant location permissions
- Make sure ESP32 is advertising (comp.start() called)
- Check that ESP32 is advertising the Nordic UART Service UUID

### Connection fails
- Check if ESP32 is running companion module with BLE
- Verify ESP32 has BLE support (not ESP32-S2)
- Check MicroPython version supports BLE (1.19+)
- Try restarting both devices

### REPL not responding
- Verify connection status shows "Connected"
- Check ESP32 console for errors
- Ensure notifications are enabled on TX characteristic
- Try reconnecting

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

MIT License - See LICENSE file in the root directory
