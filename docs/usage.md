# Usage

## Android App
1. Open the Android project located in `android-app/` with Android Studio.
2. Ensure Bluetooth permissions are granted.
3. Scan for the ESP32 device and connect.
4. Use the REPL, file transfer, telemetry, and notification views.

## ESP32 MicroPython
1. Copy the `micropython/esp32_companion` directory to the ESP32 filesystem.
2. Import and initialize the managers in your boot script.
3. Start Bluetooth advertising and wait for the Android app to connect.

