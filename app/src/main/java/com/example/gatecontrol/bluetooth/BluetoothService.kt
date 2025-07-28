
package com.example.gatecontrol.bluetooth

import android.annotation.SuppressLint
import android.content.Context
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothService(
    private val context: Context,
    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
) {
    private val deviceName = "smartspro"
    private val uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")
    private var socket: BluetoothSocket? = null


    private val _incoming = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val incoming: SharedFlow<String> = _incoming.asSharedFlow()


    private val _connected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _connected.asStateFlow()


    @SuppressLint("MissingPermission")
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        val bonded = adapter.bondedDevices
        if (bonded.isEmpty()) {
            withContext(Dispatchers.Main) {

            }
            _connected.value = false
            return@withContext false
        }

        val device = bonded.firstOrNull { it.name == deviceName }
        if (device == null) {
            withContext(Dispatchers.Main) {
            }
            _connected.value = false
            return@withContext false
        }

        return@withContext try {
            socket = device.createRfcommSocketToServiceRecord(uuid).also { it.connect() }
            _connected.value = true
            withContext(Dispatchers.Main) {

            }
            launchReader()
            true
        } catch (e: Exception) {
            _connected.value = false
            withContext(Dispatchers.Main) {

            }
            false
        }
    }


    private fun launchReader() {
        socket?.inputStream?.let { stream ->
            CoroutineScope(Dispatchers.IO).launch {
                val reader = BufferedReader(InputStreamReader(stream))
                try {
                    while (true) {
                        val line = reader.readLine() ?: break
                        withContext(Dispatchers.Main) {

                        }
                        _incoming.emit(line)
                    }
                } catch (e: Exception) {

                } finally {
                    socket?.close()
                    socket = null
                    _connected.value = false
                    withContext(Dispatchers.Main) {

                    }
                }
            }
        }
    }


    suspend fun send(command: String) = withContext(Dispatchers.IO) {
        try {
            socket?.outputStream?.write((command + "\n").toByteArray())
            withContext(Dispatchers.Main) {

            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {

            }
        }
    }

    fun disconnect() {
        socket?.close()
        socket = null
        _connected.value = false
        Toast.makeText(context, "Bluetooth отключён", Toast.LENGTH_SHORT).show()
    }
}
