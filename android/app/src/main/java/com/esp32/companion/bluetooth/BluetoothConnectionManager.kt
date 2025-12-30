package com.esp32.companion.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.esp32.companion.data.BluetoothDeviceInfo
import com.esp32.companion.data.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Manages Bluetooth Classic connections to ESP32 devices
 */
class BluetoothConnectionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "BluetoothManager"
        // Standard SPP UUID
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
    
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _connectedDevice = MutableStateFlow<BluetoothDeviceInfo?>(null)
    val connectedDevice: StateFlow<BluetoothDeviceInfo?> = _connectedDevice.asStateFlow()
    
    private val _receivedData = MutableStateFlow<String>("")
    val receivedData: StateFlow<String> = _receivedData.asStateFlow()
    
    /**
     * Check if Bluetooth is available on this device
     */
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null
    }
    
    /**
     * Check if Bluetooth is enabled
     */
    @SuppressLint("MissingPermission")
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Get list of paired devices
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        if (!hasBluetoothPermissions()) {
            Log.e(TAG, "Missing Bluetooth permissions")
            return emptyList()
        }
        
        return bluetoothAdapter?.bondedDevices?.map { device ->
            BluetoothDeviceInfo(
                name = device.name ?: "Unknown",
                address = device.address,
                paired = true
            )
        } ?: emptyList()
    }
    
    /**
     * Connect to a Bluetooth device
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(deviceAddress: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!hasBluetoothPermissions()) {
                return@withContext Result.failure(SecurityException("Missing Bluetooth permissions"))
            }
            
            _connectionState.value = ConnectionState.CONNECTING
            
            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
                ?: return@withContext Result.failure(IllegalArgumentException("Device not found"))
            
            // Close any existing connection
            disconnect()
            
            // Create socket and connect
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothSocket?.connect()
            
            inputStream = bluetoothSocket?.inputStream
            outputStream = bluetoothSocket?.outputStream
            
            _connectedDevice.value = BluetoothDeviceInfo(
                name = device.name ?: "Unknown",
                address = device.address,
                paired = true
            )
            _connectionState.value = ConnectionState.CONNECTED
            
            // Start listening for incoming data
            startListening()
            
            Result.success(Unit)
        } catch (e: IOException) {
            Log.e(TAG, "Connection failed", e)
            _connectionState.value = ConnectionState.ERROR
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
            _connectionState.value = ConnectionState.ERROR
            Result.failure(e)
        }
    }
    
    /**
     * Disconnect from the current device
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing connection", e)
        } finally {
            inputStream = null
            outputStream = null
            bluetoothSocket = null
            _connectedDevice.value = null
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
    
    /**
     * Send data to the connected device
     */
    suspend fun sendData(data: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val stream = outputStream ?: return@withContext Result.failure(
                IllegalStateException("Not connected")
            )
            
            val bytes = (data + "\n").toByteArray(Charsets.UTF_8)
            stream.write(bytes)
            stream.flush()
            
            Result.success(Unit)
        } catch (e: IOException) {
            Log.e(TAG, "Error sending data", e)
            Result.failure(e)
        }
    }
    
    /**
     * Start listening for incoming data
     */
    private fun startListening() {
        Thread {
            val buffer = ByteArray(1024)
            val stringBuilder = StringBuilder()
            
            try {
                while (bluetoothSocket?.isConnected == true) {
                    val stream = inputStream ?: break
                    
                    val bytes = stream.read(buffer)
                    if (bytes > 0) {
                        val data = String(buffer, 0, bytes, Charsets.UTF_8)
                        stringBuilder.append(data)
                        
                        // Check if we have complete messages (ending with newline)
                        val messages = stringBuilder.toString().split("\n")
                        if (messages.size > 1) {
                            // Process all complete messages
                            for (i in 0 until messages.size - 1) {
                                _receivedData.value = messages[i]
                            }
                            // Keep the incomplete message
                            stringBuilder.clear()
                            stringBuilder.append(messages.last())
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error reading data", e)
                _connectionState.value = ConnectionState.ERROR
            }
        }.start()
    }
    
    /**
     * Check if app has necessary Bluetooth permissions
     */
    private fun hasBluetoothPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
