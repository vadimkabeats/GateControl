package com.example.gatecontrol.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gatecontrol.data.ConnectionState
import com.example.gatecontrol.viewmodel.MainViewModel
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import com.example.gatecontrol.ui.components.DropdownParam
import com.example.gatecontrol.ui.components.SliderParam
import kotlin.math.roundToInt

@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val conn by vm.connState.collectAsState()
    val cfg by vm.config.collectAsState()
    val rez by vm.result.collectAsState()

    val modes = mapOf(
        1 to "По очереди",
        2 to "Все сразу",
        3 to "Только верх",
        4 to "Только низ",
        5 to "По часовой",
        6 to "Против часовой",
        7 to "Левая сторона",
        8 to "Правая сторона",
        9 to "Диагональ 1",
        10 to "Диагональ 2"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("Тренировка ворот") },
            actions = {
                when (conn) {
                    ConnectionState.DISCONNECTED -> Icon(Icons.Filled.BluetoothDisabled, contentDescription = null)
                    ConnectionState.CONNECTING -> CircularProgressIndicator(Modifier.size(24.dp))
                    ConnectionState.CONNECTED -> Icon(Icons.Filled.BluetoothConnected, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Text(
            text = "РЕЗУЛЬТАТ: $rez",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { vm.sendStart() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                )
            ) { Text("СТАРТ") }

            Button(
                onClick = { vm.sendStop() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                )
            ) { Text("СТОП") }
        }

        Spacer(Modifier.height(24.dp))
        DropdownParam(
            label = "Режим ворот",
            selectedKey = cfg.mode,
            options = modes,
            onSelect = { vm.updateConfig { copy(mode = it) } }
        )

        Spacer(Modifier.height(16.dp))
        SliderParam(
            label = "Время на мишень",
            value = cfg.time,
            range = 5..91,
            displayInfinityAt = 91
        ) { vm.updateConfig { copy(time = it) } }

        Spacer(Modifier.height(16.dp))
        SliderParam(
            label = "Количество мишеней",
            value = cfg.count,
            range = 2..25
        ) { vm.updateConfig { copy(count = it) } }

        Spacer(Modifier.height(16.dp))
        DropdownParam(
            label = "Режим пасеров",
            selectedKey = cfg.rpa,
            options = mapOf(1 to "Случайно", 2 to "Циклично", 3 to "Серия"),
            onSelect = { vm.updateConfig { copy(rpa = it) } }
        )

        Spacer(Modifier.height(16.dp))
        SliderParam(
            label = "Длина серии",
            value = cfg.seriesLen,
            range = 2..10
        ) { vm.updateConfig { copy(seriesLen = it) } }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "Скорости пасеров",
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        cfg.spd.forEachIndexed { idx, speed ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = speed != 0,
                    onCheckedChange = { checked ->
                        vm.updateConfig {
                            val newSpd = spd.toMutableList()
                            newSpd[idx] = if (checked) maxOf(1, newSpd[idx]) else 0
                            copy(spd = newSpd)
                        }
                    }
                )
                Text(
                    text = "Пасер ${idx + 1}",
                    modifier = Modifier.width(80.dp),
                    color = Color.Black
                )
                Slider(
                    value = speed.coerceAtLeast(1).toFloat(),
                    onValueChange = { newVal ->
                        vm.updateConfig {
                            val newSpd = spd.toMutableList()
                            newSpd[idx] = newVal.roundToInt().coerceIn(1..8)
                            copy(spd = newSpd)
                        }
                    },
                    valueRange = 1f..8f,
                    enabled = speed != 0,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Black,
                        activeTrackColor = Color.Black,
                        inactiveTrackColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = speed.toString(),
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.Black
                )
            }
        }
    }
}
