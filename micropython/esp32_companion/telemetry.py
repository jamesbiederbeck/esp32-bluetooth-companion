"""Telemetry data broadcasting."""

import time

class TelemetryManager:
    def __init__(self) -> None:
        self.last_reading = None

    def collect(self) -> dict:
        """Collect telemetry data."""
        reading = {
            "timestamp_ms": int(time.time() * 1000),
            "temperature_c_x10": 0,
            "voltage_mv": 0,
            "rssi": 0,
            "status_flags": 0,
        }
        self.last_reading = reading
        return reading
