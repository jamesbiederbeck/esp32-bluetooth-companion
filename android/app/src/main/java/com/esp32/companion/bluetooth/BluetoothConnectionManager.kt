package com.esp32.companion.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import com.esp32.companion.data.BluetoothDeviceInfo
import com.esp32.companion.data.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Manages Bluetooth Low Energy (BLE) connections to ESP32 devices
 */
class BluetoothConnectionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "BLEManager"
        
        // Nordic UART Service UUIDs
        private val SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        private val RX_CHAR_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        private val TX_CHAR_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
        private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        
        // BLE communication constants
        private const val MAX_CHUNK_SIZE = 512
    }
    
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    
    private var bluetoothGatt: BluetoothGatt? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _connectedDevice = MutableStateFlow<BluetoothDeviceInfo?>(null)
    val connectedDevice: StateFlow<BluetoothDeviceInfo?> = _connectedDevice.asStateFlow()
    
    private val _receivedData = MutableStateFlow<String>("")
    val receivedData: StateFlow<String> = _receivedData.asStateFlow()
    
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDeviceInfo>> = _scannedDevices.asStateFlow()
    
    private var isScanning = false
    private val rxBuffer = StringBuilder()
    
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
     * Get list of bonded devices
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
     * Start BLE scan for devices
     */
    @SuppressLint("MissingPermission")
    suspend fun startScan() = withContext(Dispatchers.IO) {
        if (!hasBluetoothPermissions()) {
            Log.e(TAG, "Missing Bluetooth permissions")
            return@withContext
        }
        
        if (isScanning) {
            Log.w(TAG, "Already scanning")
            return@withContext
        }
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        val scanFilters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()
        )
        
        try {
            bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
            isScanning = true
            Log.d(TAG, "BLE scan started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start scan", e)
        }
    }
    
    /**
     * Stop BLE scan
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!isScanning) return
        
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
            isScanning = false
            Log.d(TAG, "BLE scan stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop scan", e)
        }
    }
    
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceInfo = BluetoothDeviceInfo(
                name = device.name ?: "Unknown",
                address = device.address,
                paired = false
            )
            
            // Add to list if not already present
            val currentList = _scannedDevices.value.toMutableList()
            if (!currentList.any { it.address == deviceInfo.address }) {
                currentList.add(deviceInfo)
                _scannedDevices.value = currentList
                Log.d(TAG, "Found device: ${deviceInfo.name} (${deviceInfo.address})")
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE scan failed with error code: $errorCode")
            isScanning = false
        }
    }
    
    /**
     * Connect to a BLE device
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
            
            // Stop scanning if active
            if (isScanning) {
                stopScan()
            }
            
            // Connect to GATT server
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
            _connectionState.value = ConnectionState.ERROR
            Result.failure(e)
        }
    }
    
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to GATT server")
                    _connectedDevice.value = BluetoothDeviceInfo(
                        name = gatt.device.name ?: "Unknown",
                        address = gatt.device.address,
                        paired = false
                    )
                    // Discover services
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from GATT server")
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _connectedDevice.value = null
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                }
            }
        }
        
        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")
                
                // Find the UART service
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    txCharacteristic = service.getCharacteristic(TX_CHAR_UUID)
                    rxCharacteristic = service.getCharacteristic(RX_CHAR_UUID)
                    
                    // Enable notifications for TX characteristic
                    txCharacteristic?.let { char ->
                        gatt.setCharacteristicNotification(char, true)
                        
                        // Write to CCCD to enable notifications
                        val descriptor = char.getDescriptor(CCCD_UUID)
                        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                    }
                    
                    _connectionState.value = ConnectionState.CONNECTED
                    Log.d(TAG, "UART service configured")
                } else {
                    Log.e(TAG, "UART service not found")
                    _connectionState.value = ConnectionState.ERROR
                }
            } else {
                Log.e(TAG, "Service discovery failed: $status")
                _connectionState.value = ConnectionState.ERROR
            }
        }
        
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == TX_CHAR_UUID) {
                val data = characteristic.value
                val text = String(data, Charsets.UTF_8)
                handleReceivedData(text)
            }
        }
        
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic write successful")
            } else {
                Log.e(TAG, "Characteristic write failed: $status")
            }
        }
    }
    
    private fun handleReceivedData(text: String) {
        rxBuffer.append(text)
        
        // Process complete messages (ending with newline)
        val messages = rxBuffer.toString().split("\n")
        if (messages.size > 1) {
            // Process all complete messages
            for (i in 0 until messages.size - 1) {
                _receivedData.value = messages[i]
            }
            // Keep the incomplete message
            rxBuffer.clear()
            rxBuffer.append(messages.last())
        }
    }
    
    /**
     * Disconnect from the current device
     */
    @SuppressLint("MissingPermission")
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
            txCharacteristic = null
            rxCharacteristic = null
            _connectedDevice.value = null
            _connectionState.value = ConnectionState.DISCONNECTED
        } catch (e: Exception) {
            Log.e(TAG, "Error closing connection", e)
        }
    }
    
    /**
     * Send data to the connected device
     */
    @SuppressLint("MissingPermission")
    suspend fun sendData(data: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val characteristic = rxCharacteristic
                ?: return@withContext Result.failure(IllegalStateException("Not connected"))
            
            val gatt = bluetoothGatt
                ?: return@withContext Result.failure(IllegalStateException("Not connected"))
            
            val bytes = (data + "\n").toByteArray(Charsets.UTF_8)
            
            // Send in chunks if needed
            for (i in bytes.indices step MAX_CHUNK_SIZE) {
                val end = minOf(i + MAX_CHUNK_SIZE, bytes.size)
                val chunk = bytes.copyOfRange(i, end)
                
                characteristic.value = chunk
                val success = gatt.writeCharacteristic(characteristic)
                
                if (!success) {
                    return@withContext Result.failure(
                        IllegalStateException("Failed to write characteristic")
                    )
                }
                
                // Small delay between writes
                kotlinx.coroutines.delay(50)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending data", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if app has necessary Bluetooth permissions
     */
    private fun hasBluetoothPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
