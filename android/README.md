# ESP32 Companion Android App

Android companion application for ESP32 devices running MicroPython.

## Features

- 🔵 **Bluetooth connectivity** - Connect to ESP32 devices via Bluetooth Classic
- 💻 **REPL interface** - Execute Python commands remotely
- 📁 **File transfer** - Upload and download files to/from ESP32
- 📊 **Telemetry** - Monitor system stats (CPU, memory, temperature)
- 🔔 **Notifications** - Receive alerts from ESP32

## Requirements

- Android 8.0 (API level 26) or higher
- Bluetooth Classic support
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
2. **Pair your ESP32** device in Android Bluetooth settings
3. Launch the **ESP32 Companion** app
4. Grant required permissions when prompted

### Connecting to ESP32

1. Open the app and go to the **Devices** tab
2. Tap **Scan for Devices** or use the refresh button
3. Select your ESP32 from the list of paired devices
4. Wait for connection to establish

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
Manages Bluetooth Classic connections:
- Device scanning
- Pairing
- Connection management
- Data transmission

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

The app communicates with ESP32 using a JSON-based protocol over Bluetooth Classic (SPP). See `PROTOCOL.md` for detailed specification.

## Permissions

The app requires the following permissions:

- `BLUETOOTH` - Bluetooth communication (Android < 12)
- `BLUETOOTH_ADMIN` - Bluetooth management (Android < 12)
- `BLUETOOTH_CONNECT` - Bluetooth connection (Android 12+)
- `BLUETOOTH_SCAN` - Bluetooth scanning (Android 12+)
- `ACCESS_FINE_LOCATION` - Required for Bluetooth scanning

## Troubleshooting

### Cannot find devices
- Ensure Bluetooth is enabled
- Grant location permissions
- Pair device in Android settings first

### Connection fails
- Check if ESP32 is running companion module
- Verify device is not connected to another app
- Try unpairing and re-pairing

### REPL not responding
- Verify connection status
- Check ESP32 console for errors
- Try reconnecting

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

MIT License - See LICENSE file in the root directory
