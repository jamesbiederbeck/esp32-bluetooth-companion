"""
ESP32 Companion Module for MicroPython

This module implements the communication protocol for the ESP32 Companion Android app.
It provides functionality for REPL, file transfer, telemetry, and notifications over
Bluetooth Low Energy (BLE).

Usage:
    from companion import Companion
    
    comp = Companion()
    comp.start()
    
    # Send a notification
    comp.notify("Alert", "Temperature high!", "warning")
    
    # Main loop
    while True:
        comp.loop()
        time.sleep(0.1)
"""

import json
import time
import machine
import gc
import os
import ubinascii
import struct

try:
    import ubluetooth
    from ubluetooth import BLE
except ImportError:
    ubluetooth = None
    BLE = None


# BLE UUIDs for ESP32 Companion service
_SERVICE_UUID = ubluetooth.UUID("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")  # Nordic UART Service
_RX_CHAR_UUID = ubluetooth.UUID("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")  # RX Characteristic
_TX_CHAR_UUID = ubluetooth.UUID("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")  # TX Characteristic

# BLE flags
_FLAG_READ = 0x0002
_FLAG_WRITE_NO_RESPONSE = 0x0004
_FLAG_WRITE = 0x0008
_FLAG_NOTIFY = 0x0010

# BLE IRQ events
_IRQ_CENTRAL_CONNECT = 1
_IRQ_CENTRAL_DISCONNECT = 2
_IRQ_GATTS_WRITE = 3

# Maximum message chunk size for BLE
_MAX_CHUNK_SIZE = 512


class Companion:
    """
    Main companion class for ESP32 Android app communication over BLE
    """
    
    def __init__(self, device_name="ESP32-Companion"):
        """
        Initialize the companion module
        
        Args:
            device_name: Bluetooth device name to advertise
        """
        self.device_name = device_name
        self.ble = BLE() if BLE else None
        self.connected = False
        self.conn_handle = None
        self.rx_handle = None
        self.tx_handle = None
        self.handlers = {}
        self.telemetry_interval = 0
        self.last_telemetry = 0
        self.version = "1.0.0"
        self.rx_buffer = ""
        
        # Register default message handlers
        self._register_handlers()
        
        # Register BLE services
        if self.ble:
            self._register_services()
            self.ble.irq(self._ble_irq)
    
    def _register_handlers(self):
        """Register default message type handlers"""
        self.handlers = {
            "repl_cmd": self._handle_repl_cmd,
            "file_list": self._handle_file_list,
            "file_download": self._handle_file_download,
            "file_upload": self._handle_file_upload,
            "telemetry_request": self._handle_telemetry_request,
            "status_request": self._handle_status_request,
            "ping": self._handle_ping,
        }
    
    def _register_services(self):
        """Register BLE GATT services and characteristics"""
        # Define the Nordic UART Service
        service = (
            _SERVICE_UUID,
            (
                (_RX_CHAR_UUID, _FLAG_WRITE | _FLAG_WRITE_NO_RESPONSE),
                (_TX_CHAR_UUID, _FLAG_NOTIFY | _FLAG_READ),
            ),
        )
        
        # Register services
        ((self.rx_handle, self.tx_handle),) = self.ble.gatts_register_services((service,))
        
        # Set initial value
        self.ble.gatts_write(self.tx_handle, b'')
    
    def _ble_irq(self, event, data):
        """Handle BLE IRQ events"""
        if event == _IRQ_CENTRAL_CONNECT:
            # A central has connected to this peripheral
            self.conn_handle, _, _ = data
            self.connected = True
            print(f"BLE connected: handle={self.conn_handle}")
            
        elif event == _IRQ_CENTRAL_DISCONNECT:
            # A central has disconnected
            self.conn_handle, _, _ = data
            self.connected = False
            print(f"BLE disconnected: handle={self.conn_handle}")
            self.conn_handle = None
            # Start advertising again
            self._advertise()
            
        elif event == _IRQ_GATTS_WRITE:
            # A client has written to a characteristic
            conn_handle, attr_handle = data
            if conn_handle == self.conn_handle and attr_handle == self.rx_handle:
                # Read the data
                value = self.ble.gatts_read(self.rx_handle)
                self._handle_rx_data(value)
    
    def _handle_rx_data(self, data):
        """Handle received data from BLE"""
        try:
            # Decode the received data
            msg = data.decode('utf-8')
            self.rx_buffer += msg
            
            # Process complete messages (ending with newline)
            while '\n' in self.rx_buffer:
                line, self.rx_buffer = self.rx_buffer.split('\n', 1)
                if line:
                    self.process_message(line)
        except Exception as e:
            print(f"Error handling RX data: {e}")
    
    def _advertise(self, interval_us=500000):
        """Start BLE advertising"""
        # Advertising payload
        name = self.device_name.encode('utf-8')
        payload = bytearray()
        
        # Flags
        payload.extend(struct.pack("BB", 2, 0x01))
        payload.append(0x06)  # General discoverable + BR/EDR not supported
        
        # Name
        payload.extend(struct.pack("BB", len(name) + 1, 0x09))
        payload.extend(name)
        
        # Advertise
        self.ble.gap_advertise(interval_us, adv_data=payload)
        print(f"BLE advertising as '{self.device_name}'")
    
    def start(self):
        """
        Start the companion service with BLE
        """
        if not self.ble:
            print("ERROR: BLE not available on this device")
            return False
            
        print(f"Starting ESP32 Companion v{self.version}")
        print(f"Device name: {self.device_name}")
        
        try:
            # Activate BLE
            self.ble.active(True)
            
            # Start advertising
            self._advertise()
            
            print("BLE service started successfully")
            print("Waiting for connections...")
            return True
            
        except Exception as e:
            print(f"Error starting BLE service: {e}")
            return False
    
    def send_message(self, msg_type, msg_id, payload):
        """
        Send a protocol message via BLE
        
        Args:
            msg_type: Message type string
            msg_id: Message ID for correlation
            payload: Dictionary of message data
        """
        if not self.connected or not self.conn_handle:
            print(f"Not connected, cannot send {msg_type}")
            return False
            
        try:
            message = {
                "type": msg_type,
                "id": msg_id,
                "payload": payload
            }
            
            json_str = json.dumps(message) + "\n"
            data = json_str.encode('utf-8')
            
            # Send data in chunks if needed
            for i in range(0, len(data), _MAX_CHUNK_SIZE):
                chunk = data[i:i + _MAX_CHUNK_SIZE]
                self.ble.gatts_notify(self.conn_handle, self.tx_handle, chunk)
                time.sleep_ms(10)  # Small delay between chunks
            
            print(f"TX: {msg_type} [{msg_id}]")
            return True
            
        except Exception as e:
            print(f"Error sending message: {e}")
            return False
    
    def notify(self, title, message, level="info"):
        """
        Send a notification to the Android app
        
        Args:
            title: Notification title
            message: Notification message
            level: Notification level (info, warning, error, success)
        """
        msg_id = f"notif_{int(time.time())}"
        payload = {
            "title": title,
            "message": message,
            "level": level,
            "timestamp": time.time()
        }
        
        self.send_message("notification", msg_id, payload)
    
    def send_telemetry(self):
        """
        Send telemetry data to the Android app
        """
        msg_id = f"telem_{int(time.time())}"
        
        # Gather system information
        # Note: Uptime calculation requires tracking boot time
        # This is a simplified implementation
        payload = {
            "timestamp": time.time(),
            "cpu_freq": machine.freq(),
            "mem_free": gc.mem_free(),
            # For accurate uptime, track boot time and calculate:
            # "uptime": time.time() - boot_time
        }
        
        # Add temperature if available
        # Note: ESP32 internal temperature sensor is not well documented
        # and varies between chip versions. This is disabled by default.
        # If you have an external temperature sensor, add it here.
        # Example:
        # try:
        #     import dht
        #     sensor = dht.DHT22(machine.Pin(4))
        #     sensor.measure()
        #     payload["temperature"] = sensor.temperature()
        #     payload["humidity"] = sensor.humidity()
        # except:
        #     pass
        
        self.send_message("telemetry", msg_id, payload)
    
    def _handle_repl_cmd(self, msg_id, payload):
        """
        Handle REPL command execution
        
        Security Note: While this implementation uses a whitelist of safe
        built-in functions and restricts module access, eval() and exec()
        can still be exploited through complex expressions or module methods.
        For production use in untrusted environments, consider:
        - Using a dedicated sandboxed Python interpreter
        - Implementing command whitelisting instead of execution
        - Adding user authentication
        - Logging all executed commands
        """
        command = payload.get("command", "")
        
        try:
            # Create a safe builtins dictionary with only allowed functions
            safe_builtins = {
                'abs': abs,
                'all': all,
                'any': any,
                'bin': bin,
                'bool': bool,
                'bytearray': bytearray,
                'bytes': bytes,
                'chr': chr,
                'dict': dict,
                'divmod': divmod,
                'enumerate': enumerate,
                'filter': filter,
                'float': float,
                'format': format,
                'hex': hex,
                'int': int,
                'isinstance': isinstance,
                'issubclass': issubclass,
                'iter': iter,
                'len': len,
                'list': list,
                'map': map,
                'max': max,
                'min': min,
                'next': next,
                'oct': oct,
                'ord': ord,
                'pow': pow,
                'print': print,
                'range': range,
                'repr': repr,
                'reversed': reversed,
                'round': round,
                'set': set,
                'slice': slice,
                'sorted': sorted,
                'str': str,
                'sum': sum,
                'tuple': tuple,
                'type': type,
                'zip': zip,
            }
            
            # Create execution namespace with safe modules
            # Note: 'os' module is excluded for security reasons
            exec_globals = {
                '__builtins__': safe_builtins,
                'machine': __import__('machine'),
                'time': __import__('time'),
                'gc': __import__('gc'),
            }
            exec_locals = {}
            
            # Try to evaluate as expression first
            try:
                result = eval(command, exec_globals, exec_locals)
                output = str(result) if result is not None else ""
            except SyntaxError:
                # If not an expression, execute as statement
                exec(command, exec_globals, exec_locals)
                output = ""
            
            self.send_message("repl_response", msg_id, {
                "output": output,
                "error": None
            })
        except Exception as e:
            self.send_message("repl_response", msg_id, {
                "output": "",
                "error": str(e)
            })
    
    def _handle_file_list(self, msg_id, payload):
        """Handle file list request"""
        path = payload.get("path", "/")
        
        try:
            files = []
            for item in os.listdir(path):
                # Properly join paths to avoid double slashes
                if path == "/":
                    item_path = f"/{item}"
                else:
                    item_path = f"{path}/{item}"
                try:
                    stat = os.stat(item_path)
                    is_dir = stat[0] & 0x4000  # Directory flag
                    files.append({
                        "name": item,
                        "size": stat[6],
                        "type": "dir" if is_dir else "file"
                    })
                except OSError:
                    # Skip items that can't be stat'd
                    pass
            
            self.send_message("file_list_response", msg_id, {
                "files": files
            })
        except Exception as e:
            self.send_message("error", msg_id, {
                "code": "FILE_ERROR",
                "message": str(e)
            })
    
    def _handle_file_download(self, msg_id, payload):
        """Handle file download request"""
        path = payload.get("path", "")
        
        try:
            # Read file in binary mode to handle both text and binary files
            with open(path, "rb") as f:
                content = f.read()
            
            # In real implementation, this would be chunked
            encoded = ubinascii.b2a_base64(content).decode().strip()
            
            self.send_message("file_data", msg_id, {
                "path": path,
                "size": len(content),
                "chunks": 1
            })
            
            self.send_message("file_chunk", msg_id, {
                "chunk_num": 0,
                "data": encoded
            })
        except OSError as e:
            self.send_message("error", msg_id, {
                "code": "FILE_NOT_FOUND",
                "message": str(e)
            })
        except Exception as e:
            self.send_message("error", msg_id, {
                "code": "FILE_ERROR",
                "message": str(e)
            })
    
    def _handle_file_upload(self, msg_id, payload):
        """Handle file upload initiation"""
        # Implementation would handle receiving chunks
        pass
    
    def _handle_telemetry_request(self, msg_id, payload):
        """Handle telemetry request"""
        interval = payload.get("interval", 5000)
        self.telemetry_interval = interval
        self.send_telemetry()
    
    def _handle_status_request(self, msg_id, payload):
        """Handle status request"""
        self.send_message("status_response", msg_id, {
            "device_name": self.device_name,
            "version": self.version,
            "uptime": time.time(),
            "mem_free": gc.mem_free()
        })
    
    def _handle_ping(self, msg_id, payload):
        """Handle ping request"""
        self.send_message("pong", msg_id, {
            "timestamp": time.time()
        })
    
    def process_message(self, json_str):
        """
        Process an incoming JSON message
        
        Args:
            json_str: JSON string containing the message
        """
        try:
            message = json.loads(json_str)
            msg_type = message.get("type")
            msg_id = message.get("id")
            payload = message.get("payload", {})
            
            print(f"RX: {msg_type} [{msg_id}]")
            
            handler = self.handlers.get(msg_type)
            if handler:
                handler(msg_id, payload)
            else:
                self.send_message("error", msg_id, {
                    "code": "UNKNOWN_TYPE",
                    "message": f"Unknown message type: {msg_type}"
                })
        except Exception as e:
            print(f"Error processing message: {e}")
    
    def loop(self):
        """
        Main processing loop
        Call this regularly to handle telemetry and other periodic tasks
        """
        current_time = time.time()
        
        # Send telemetry if interval is set
        if self.telemetry_interval > 0:
            if current_time - self.last_telemetry > (self.telemetry_interval / 1000):
                self.send_telemetry()
                self.last_telemetry = current_time


# Convenience function for quick notifications
def notify(title, message, level="info"):
    """
    Send a notification without creating a Companion instance
    
    NOTE: This is a convenience function that prints to console.
    For full functionality, use the Companion class with proper
    Bluetooth setup.
    
    Args:
        title: Notification title
        message: Notification message
        level: Notification level (info, warning, error, success)
    """
    print(f"[{level.upper()}] {title}: {message}")
    
    # TODO: In a real implementation, this would create a singleton
    # Companion instance or use an existing one to send notifications
