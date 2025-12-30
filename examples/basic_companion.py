from esp32_companion import BluetoothManager, ReplManager, TelemetryManager

bluetooth = BluetoothManager()
repl = ReplManager()
telemetry = TelemetryManager()

bluetooth.start_advertising()

print("ESP32 companion started")
print("REPL sample output:", repl.execute("2 + 2"))
print("Telemetry:", telemetry.collect())
