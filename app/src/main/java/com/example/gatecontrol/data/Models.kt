package com.example.gatecontrol.data

data class GateConfig(
    val mode: Int = 1,
    val time: Int = 0,
    val count: Int = 0,
    val rpa: Int = 1,
    val seriesLen: Int = 0,
    val spd: List<Int> = listOf(0, 0, 0, 0) // 0=off,1–8=скорость
)

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}
