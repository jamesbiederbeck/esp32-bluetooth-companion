"""
ESP32 Companion Module for MicroPython

This module implements the communication protocol for the ESP32 Companion Android app.
It provides functionality for REPL, file transfer, telemetry, and notifications over
Bluetooth Classic (SPP).

Usage:
    from companion import Companion
    
    comp = Companion()
    comp.start()
    
    # Send a notification
    comp.notify("Alert", "Temperature high!", "warning")
"""

import json
import time
import machine
import gc
import os
from bluetooth import BluetoothError

try:
    import ubluetooth
except ImportError:
    import bluetooth as ubluetooth


class Companion:
    """
    Main companion class for ESP32 Android app communication
    """
    
    def __init__(self, device_name="ESP32-Companion"):
        """
        Initialize the companion module
        
        Args:
            device_name: Bluetooth device name to advertise
        """
        self.device_name = device_name
        self.bt = None
        self.connected = False
        self.client_addr = None
        self.handlers = {}
        self.telemetry_interval = 0
        self.last_telemetry = 0
        self.version = "1.0.0"
        
        # Register default message handlers
        self._register_handlers()
    
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
    
    def start(self):
        """
        Start the companion service
        Sets up Bluetooth SPP and begins listening for connections
        
        NOTE: This is a template implementation. Actual Bluetooth SPP
        setup requires platform-specific code that varies by ESP32 variant
        and MicroPython version. Users should implement the Bluetooth
        connection handling based on their specific hardware and requirements.
        
        For reference implementations, see:
        - ESP32: Use machine.UART with Bluetooth SPP profile
        - ESP32-S3: Similar to ESP32
        - Other variants: Check MicroPython documentation
        """
        print(f"Starting ESP32 Companion v{self.version}")
        print(f"Device name: {self.device_name}")
        print("NOTE: Bluetooth SPP implementation is hardware-specific")
        print("Please implement Bluetooth connection handling for your platform")
        
        # TODO: Implement platform-specific Bluetooth SPP setup
        # Example structure:
        # 1. Initialize Bluetooth adapter
        # 2. Configure SPP service with UUID
        # 3. Start advertising with device name
        # 4. Accept incoming connections
        # 5. Set up read/write handlers
        
        print("Companion service initialized")
        
        # Main loop would go here
        # In a real implementation, this would listen for incoming connections
    
    def send_message(self, msg_type, msg_id, payload):
        """
        Send a protocol message
        
        NOTE: This is a template implementation that prints to console.
        In a real implementation, this should send via Bluetooth SPP.
        
        Args:
            msg_type: Message type string
            msg_id: Message ID for correlation
            payload: Dictionary of message data
        
        Example implementation:
            json_str = json.dumps(message) + "\n"
            self.bt_connection.write(json_str.encode())
        """
        message = {
            "type": msg_type,
            "id": msg_id,
            "payload": payload
        }
        
        json_str = json.dumps(message)
        print(f"TX: {json_str}")
        
        # TODO: Replace with actual Bluetooth transmission
        # self.bt_connection.write((json_str + "\n").encode())
    
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
        payload = {
            "timestamp": time.time(),
            "cpu_freq": machine.freq(),
            "mem_free": gc.mem_free(),
            "uptime": time.time()  # Simplified - would need boot time tracking
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
        """Handle REPL command execution"""
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
                item_path = f"{path}/{item}" if path != "/" else f"/{item}"
                try:
                    stat = os.stat(item_path)
                    is_dir = stat[0] & 0x4000  # Directory flag
                    files.append({
                        "name": item,
                        "size": stat[6],
                        "type": "dir" if is_dir else "file"
                    })
                except:
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
            import ubinascii
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
        except Exception as e:
            self.send_message("error", msg_id, {
                "code": "FILE_NOT_FOUND",
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
    
    Args:
        title: Notification title
        message: Notification message
        level: Notification level (info, warning, error, success)
    """
    comp = Companion()
    comp.notify(title, message, level)
