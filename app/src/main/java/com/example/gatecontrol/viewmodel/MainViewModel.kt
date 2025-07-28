package com.example.gatecontrol.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gatecontrol.bluetooth.BluetoothService
import com.example.gatecontrol.data.ConnectionState
import com.example.gatecontrol.data.GateConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val btService = BluetoothService(application)

    private val _connState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connState: StateFlow<ConnectionState> = _connState.asStateFlow()

    private val _config = MutableStateFlow(GateConfig())
    val config: StateFlow<GateConfig> = _config.asStateFlow()

    private val _result = MutableStateFlow("—")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _statusMessages = MutableSharedFlow<String>()
    val statusMessages: SharedFlow<String> = _statusMessages.asSharedFlow()

    init {
        viewModelScope.launch {
            btService.isConnected.collect { connected ->
                val newState = when {
                    connected -> ConnectionState.CONNECTED
                    else      -> ConnectionState.DISCONNECTED
                }
                if (_connState.value != newState) {
                    _connState.value = newState
                    _statusMessages.emit(
                        if (connected) "Соединение установлено"
                        else               "Соединение потеряно"
                    )
                }
            }
        }

        observeIncoming()

        connect()
    }

    private fun observeIncoming() {
        viewModelScope.launch {
            btService.incoming.collect { line ->
                when {
                    line.startsWith("PAS") -> {
                        _statusMessages.emit("Получено от сервера: $line")
                        handlePas(line.removePrefix("PAS"))
                    }
                    line.startsWith("REZ") -> {
                        _result.value = line.removePrefix("REZ")
                    }
                }
            }
        }
    }

    fun connect() {
        viewModelScope.launch {
            _connState.value = ConnectionState.CONNECTING
            btService.connect()
        }
    }

    fun disconnect() {
        btService.disconnect()
    }

    private fun handlePas(payload: String) {
        val oldSpeeds = _config.value.spd
        val newSpeeds = payload.mapIndexed { idx, ch ->
            if (ch == '0') {
                0
            } else {
                oldSpeeds.getOrNull(idx)?.takeIf { it in 1..8 } ?: 1
            }
        }
        _config.update { it.copy(spd = newSpeeds) }
    }


    fun updateConfig(updater: GateConfig.() -> GateConfig) {
        _config.update(updater)
    }


    fun sendStart() {
        val c = _config.value
        val pasStr = c.spd.joinToString("") { it.toString() }
        val cmd = "REG${c.mode},TIM${c.time},SMI${c.count}," +
                "RPA${c.rpa},SER${c.seriesLen},PAS$pasStr"
        viewModelScope.launch { btService.send(cmd) }
    }


    fun sendStop() {
        viewModelScope.launch { btService.send("STOP") }
    }
}
