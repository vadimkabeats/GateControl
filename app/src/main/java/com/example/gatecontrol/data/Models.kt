package com.example.gatecontrol.data

data class GateConfig(
    val mode: Int = 1,
    val time: Int = 5,
    val count: Int = 2,
    val rpa: Int = 1,
    val seriesLen: Int = 2,
    val spd: List<Int> = listOf(0,0,0,0)
)

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}
