# esp32-bluetooth-companion
Android application created out of jealousy of the Flipper Zero companion app. Provides REPL, file transfer, telemetry, and notifications.

## Project Layout
- `protocol/`: Bluetooth protocol specification
- `android-app/`: Android Studio project (Kotlin + Jetpack Compose)
- `micropython/`: MicroPython companion module for ESP32
- `docs/`: Usage documentation
- `examples/`: Example MicroPython scripts

## Setup
### Android
1. Open `android-app/` in Android Studio, or build from the command line with `./gradlew assembleDebug`.
2. If building locally without Android Studio, ensure the Android SDK is installed and `ANDROID_SDK_ROOT` is set.
3. Before running `./gradlew` for the first time, run `./scripts/bootstrap-wrapper.sh` to download the Gradle wrapper jar.
4. Run on a device with Bluetooth enabled.

### ESP32 MicroPython
1. Copy `micropython/esp32_companion` to the ESP32 filesystem.
2. Import the module and start the Bluetooth manager.

See [docs/usage.md](docs/usage.md) for more details.
