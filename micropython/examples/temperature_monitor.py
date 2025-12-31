"""
ESP32 Companion - Temperature Monitor Example

This example demonstrates using the companion module to monitor
temperature and send notifications when thresholds are exceeded.
"""

from companion import Companion
import time
import machine

# Create companion instance
comp = Companion(device_name="ESP32-TempMonitor")

# Start the companion service
comp.start()

# Temperature threshold
TEMP_THRESHOLD = 30.0

# Initialize temperature sensor (example - adjust for your sensor)
# This is a placeholder - use your actual sensor code
def read_temperature():
    """Read temperature from sensor"""
    # Example: Reading from DHT22, DS18B20, or similar
    # For this example, we'll simulate a reading
    import random
    return 20.0 + random.random() * 15.0

# Send startup notification
comp.notify("System", "Temperature monitor started", "success")

# Main loop
print("Monitoring temperature...")
last_alert = 0

while True:
    # Process companion tasks
    comp.loop()
    
    # Read temperature
    temp = read_temperature()
    
    # Check threshold
    current_time = time.time()
    if temp > TEMP_THRESHOLD and (current_time - last_alert) > 60:
        # Send alert (max once per minute)
        comp.notify(
            "Temperature Alert",
            f"Temperature is {temp:.1f}°C (threshold: {TEMP_THRESHOLD}°C)",
            "warning"
        )
        last_alert = current_time
    
    # Wait before next reading
    time.sleep(5)
