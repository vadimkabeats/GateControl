package com.example.gatecontrol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gatecontrol.bluetooth.BluetoothService
import com.example.gatecontrol.data.ConnectionState
import com.example.gatecontrol.data.GateConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val btService: BluetoothService = BluetoothService()
) : ViewModel() {

    private val _connState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connState: StateFlow<ConnectionState> = _connState

    private val _config = MutableStateFlow(GateConfig())
    val config: StateFlow<GateConfig> = _config

    private val _result = MutableStateFlow("—")
    val result: StateFlow<String> = _result

    init {
        observeIncoming()
        connect()
    }

    private fun observeIncoming() {
        viewModelScope.launch {
            btService.incoming.collect { line ->
                when {
                    line.startsWith("PAS") -> handlePas(line.removePrefix("PAS"))
                    line.startsWith("REZ") -> _result.value = line.removePrefix("REZ")
                }
            }
        }
    }

    fun connect() {
        viewModelScope.launch {
            _connState.value = ConnectionState.CONNECTING
            if (btService.connect()) {
                _connState.value = ConnectionState.CONNECTED
            } else {
                _connState.value = ConnectionState.DISCONNECTED
            }
        }
    }

    fun disconnect() {
        btService.disconnect()
        _connState.value = ConnectionState.DISCONNECTED
    }

    private fun handlePas(payload: String) {
        val speeds = payload.map { it.digitToInt() }
        _config.update { it.copy(spd = speeds) }
    }

    fun updateConfig(updater: GateConfig.() -> GateConfig) {
        _config.update(updater)
    }

    fun sendStart() {
        val c = _config.value
        val pasStr = c.spd.joinToString("") { it.toString() }
        val cmd = "REG${c.mode},TIM${c.time},SMI${c.count}," +
                "RPA${c.rpa},SER${c.seriesLen},PAS$pasStr"
        viewModelScope.launch {
            btService.send(cmd)
        }
    }

    fun sendStop() {
        viewModelScope.launch {
            btService.send("STOP")
        }
    }
}