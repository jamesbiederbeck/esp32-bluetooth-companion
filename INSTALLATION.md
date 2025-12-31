# Installation Guide

This guide will walk you through setting up both the Android app and the ESP32 MicroPython module.

## Table of Contents

1. [ESP32 Setup](#esp32-setup)
2. [Android App Installation](#android-app-installation)
3. [First Connection](#first-connection)
4. [Troubleshooting](#troubleshooting)

## ESP32 Setup

### Prerequisites

- ESP32 development board (ESP32, ESP32-S2, ESP32-S3, or ESP32-C3)
- USB cable for programming
- MicroPython firmware installed on ESP32

### Step 1: Install MicroPython Firmware

If you haven't already installed MicroPython on your ESP32:

1. Download the latest MicroPython firmware from [micropython.org](https://micropython.org/download/esp32/)

2. Erase the flash:
   ```bash
   esptool.py --chip esp32 --port /dev/ttyUSB0 erase_flash
   ```

3. Flash the firmware:
   ```bash
   esptool.py --chip esp32 --port /dev/ttyUSB0 write_flash -z 0x1000 esp32-*.bin
   ```

### Step 2: Install Required Tools

Install a file transfer tool. Choose one:

**Option A: ampy**
```bash
pip install adafruit-ampy
```

**Option B: rshell**
```bash
pip install rshell
```

**Option C: mpremote** (recommended for MicroPython 1.19+)
```bash
pip install mpremote
```

**Option D: Use Thonny IDE** (easiest for beginners)
- Download from [thonny.org](https://thonny.org/)
- Select "MicroPython (ESP32)" as the interpreter

### Step 3: Upload the Companion Module

**Using ampy:**
```bash
ampy --port /dev/ttyUSB0 put micropython/lib/companion.py /lib/companion.py
```

**Using rshell:**
```bash
rshell --port /dev/ttyUSB0
> cp micropython/lib/companion.py /pyboard/lib/companion.py
```

**Using mpremote:**
```bash
mpremote connect /dev/ttyUSB0 fs mkdir lib
mpremote connect /dev/ttyUSB0 fs cp micropython/lib/companion.py :lib/companion.py
```

**Using Thonny:**
1. Open `micropython/lib/companion.py` in Thonny
2. Save the file to the device as `/lib/companion.py`

### Step 4: Create Your Main Script

Create a `main.py` file on your ESP32:

```python
from companion import Companion
import time

# Create companion instance with your device name
comp = Companion(device_name="ESP32-MyDevice")

# Start the companion service
comp.start()

# Send a startup notification
comp.notify("System", "ESP32 Companion started", "success")

print("ESP32 Companion is running...")

# Main loop
while True:
    # Process companion tasks (handles incoming messages, telemetry, etc.)
    comp.loop()
    
    # Your application code goes here
    # Example: Read sensors, control actuators, etc.
    
    time.sleep(0.1)  # 100ms loop delay
```

Upload this file as `/main.py` so it runs on boot.

### Step 5: Configure Bluetooth (Optional)

By default, the ESP32 advertises with the name you provided. To customize Bluetooth settings:

```python
# In your main.py, before comp.start()
comp.device_name = "ESP32-CustomName"
```

### Step 6: Test the Installation

Connect to your ESP32 via serial console and verify:

```python
>>> import companion
>>> print(companion.__doc__)
```

You should see the module documentation.

## Android App Installation

### Option 1: Install Pre-built APK (Recommended)

1. Download the latest APK from the [Releases](../../releases) page
2. On your Android device, enable "Install from Unknown Sources"
3. Open the APK file to install
4. Grant the requested permissions

### Option 2: Build from Source

#### Prerequisites

- Android Studio Arctic Fox or later
- JDK 17 or higher
- Android SDK 34

#### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/jamesbiederbeck/esp32-bluetooth-companion.git
   cd esp32-bluetooth-companion/android
   ```

2. Open the `android` directory in Android Studio

3. Wait for Gradle sync to complete

4. Connect your Android device or start an emulator

5. Click "Run" or use:
   ```bash
   ./gradlew installDebug
   ```

## First Connection

### Step 1: Pair Your Device

1. On your Android device, go to **Settings > Bluetooth**
2. Make sure Bluetooth is enabled
3. Your ESP32 should appear in the available devices list
4. Tap to pair (default PIN is usually `1234` if prompted)

### Step 2: Launch the App

1. Open the **ESP32 Companion** app
2. Grant Bluetooth and Location permissions when prompted
   - Bluetooth: Required for communication
   - Location: Required for Bluetooth scanning on Android 10+

### Step 3: Connect

1. In the app, tap the **Devices** tab
2. Tap the refresh button to load paired devices
3. Tap your ESP32 device to connect
4. Wait for the connection status to show "Connected"

### Step 4: Test Features

**REPL:**
1. Navigate to the **REPL** tab
2. Enter a Python command like `print("Hello!")`
3. Tap send and view the output

**Files:**
1. Navigate to the **Files** tab
2. Browse files on your ESP32
3. Try uploading or downloading files

**Telemetry:**
1. Navigate to the **Telemetry** tab
2. Tap "Start Telemetry"
3. View real-time system information

**Notifications:**
- From ESP32 REPL, run:
  ```python
  from companion import notify
  notify("Test", "Hello from ESP32!", "info")
  ```
- Check the app for the notification

## Troubleshooting

### ESP32 Issues

**Problem: Module not found**
```
ImportError: no module named 'companion'
```
Solution: Verify the module is in `/lib/companion.py`:
```python
import os
print(os.listdir('/lib'))
```

**Problem: Bluetooth not working**
- Check that Bluetooth is enabled in your MicroPython build
- Some ESP32 variants (ESP32-S2) don't support Bluetooth Classic
- Try power cycling the ESP32

**Problem: Out of memory**
```
MemoryError: memory allocation failed
```
Solution: Add garbage collection in your main loop:
```python
import gc
gc.collect()
```

### Android App Issues

**Problem: Cannot find devices**
- Ensure Bluetooth is enabled
- Grant location permissions (Settings > Apps > ESP32 Companion > Permissions)
- Pair the device in Android Bluetooth settings first
- Try restarting Bluetooth

**Problem: Connection fails**
- Verify ESP32 is running the companion module
- Check ESP32 serial console for errors
- Unpair and re-pair the device
- Restart both devices

**Problem: REPL not responding**
- Check connection status in app
- Verify ESP32 `main.py` includes `comp.loop()` in main loop
- Check ESP32 serial console for Python errors

**Problem: Permissions denied**
- Go to Settings > Apps > ESP32 Companion
- Enable all requested permissions
- Restart the app

### Connection Tips

- Keep devices within 10 meters (30 feet)
- Avoid obstacles between devices
- Ensure ESP32 has stable power supply
- Close other apps using Bluetooth
- Try reconnecting if connection drops

## Next Steps

Now that everything is set up, check out:

- [MicroPython Examples](micropython/examples/) - Sample scripts
- [Protocol Documentation](PROTOCOL.md) - Communication protocol details
- [Android App Guide](android/README.md) - App features and usage

## Getting Help

If you encounter issues not covered here:

1. Check the [GitHub Issues](../../issues) for similar problems
2. Review the [Discussions](../../discussions) for community help
3. Open a new issue with:
   - Your ESP32 model
   - MicroPython version
   - Android version
   - Steps to reproduce the problem
   - Error messages or logs

## Additional Resources

- [MicroPython Documentation](https://docs.micropython.org/)
- [ESP32 Documentation](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/)
- [Android Bluetooth Guide](https://developer.android.com/guide/topics/connectivity/bluetooth)
