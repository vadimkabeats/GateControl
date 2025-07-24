// File: bluetooth/BluetoothService.kt
package com.example.gatecontrol.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothService(
    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
) {
    private val deviceName = "smartspro"
    private val uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")
    private var socket: BluetoothSocket? = null

    private val _incoming = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val incoming: SharedFlow<String> = _incoming

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        adapter.bondedDevices.firstOrNull { it.name == deviceName }?.let { device ->
            socket = device.createRfcommSocketToServiceRecord(uuid).also { it.connect() }
            launchReader()
            return@withContext true
        }
        false
    }


    private fun launchReader() {
        socket?.inputStream?.let { stream ->
            CoroutineScope(Dispatchers.IO).launch {
                val reader = BufferedReader(InputStreamReader(stream))
                while (true) {
                    val line = reader.readLine() ?: break
                    _incoming.emit(line)
                }
            }
        }
    }

    suspend fun send(command: String) = withContext(Dispatchers.IO) {
        socket?.outputStream?.write((command + "\n").toByteArray())
    }

    fun disconnect() {
        socket?.close()
        socket = null
    }
}
