package com.example.gatecontrol.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
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
    private val context: Context,
    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
) {
    private val deviceName = "smartspro"
    private val uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")
    private var socket: BluetoothSocket? = null

    private val _incoming = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val incoming: SharedFlow<String> = _incoming

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) return@withContext false
        val btAdapter = adapter ?: return@withContext false
        if (!btAdapter.isEnabled) return@withContext false

        val device = try {
            btAdapter.bondedDevices.firstOrNull { it.name == deviceName }
        } catch (_: SecurityException) {
            return@withContext false
        } ?: return@withContext false

        socket = try {
            device.createRfcommSocketToServiceRecord(uuid)
                .also { it.connect() }
        } catch (_: Exception) {
            return@withContext false
        }

        launchReader()
        true
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
        Log.d("BT_COMMAND", "BluetoothService.send() → $command")
        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) return@withContext
            Log.d("BT_COMMAND", command)
            socket?.outputStream
                ?.write((command + "\n").toByteArray())
        } catch (_: Exception) {
        }
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (_: Exception) {
        }
        socket = null
    }
}
