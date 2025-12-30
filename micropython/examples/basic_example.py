"""
ESP32 Companion - Basic Example

This example demonstrates basic usage of the companion module.
"""

from companion import Companion
import time

# Create companion instance
comp = Companion(device_name="ESP32-Demo")

# Start the companion service
comp.start()

# Send a startup notification
comp.notify("System", "ESP32 Companion started", "success")

# Main loop
print("Entering main loop...")
while True:
    # Process companion tasks (telemetry, etc.)
    comp.loop()
    
    # Your application code here
    # ...
    
    time.sleep(0.1)
