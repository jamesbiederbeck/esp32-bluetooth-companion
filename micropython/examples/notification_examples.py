"""
ESP32 Companion - Notification Examples

This script demonstrates different types of notifications.
"""

from companion import Companion, notify
import time

# Create companion instance
comp = Companion(device_name="ESP32-NotifyDemo")
comp.start()

# Example 1: Using the companion instance
print("Sending notifications...")

comp.notify("Info", "This is an informational message", "info")
time.sleep(2)

comp.notify("Success", "Operation completed successfully!", "success")
time.sleep(2)

comp.notify("Warning", "System temperature is rising", "warning")
time.sleep(2)

comp.notify("Error", "Failed to read sensor data", "error")
time.sleep(2)

# Example 2: Using the convenience function
notify("Quick Note", "This uses the convenience function", "info")

print("Notification examples complete")
