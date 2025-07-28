package com.example.gatecontrol.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gatecontrol.data.ConnectionState
import com.example.gatecontrol.ui.components.DropdownParam
import com.example.gatecontrol.ui.components.SliderParam
import com.example.gatecontrol.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val conn by vm.connState.collectAsState()
    val cfg by vm.config.collectAsState()
    val rez by vm.result.collectAsState()

    val darkGreen = Color(0xFF006400)

    // Toast для статусных сообщений
    LaunchedEffect(Unit) {
        vm.statusMessages.collectLatest { msg ->
        }
    }

    val modes = mapOf(
        1 to "По очереди", 2 to "Все сразу", 3 to "Только верх",
        4 to "Только низ", 5 to "По часовой", 6 to "Против часовой",
        7 to "Левая сторона", 8 to "Правая сторона",
        9 to "Диагональ 1", 10 to "Диагональ 2"
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
            title = { Text("Подключение ворот") },
            backgroundColor = if (conn == ConnectionState.CONNECTED) darkGreen else MaterialTheme.colors.primary,
            contentColor = Color.White,
            actions = {
                IconButton(onClick = { vm.connect() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Переподключиться", tint = Color.White)
                }
                when (conn) {
                    ConnectionState.DISCONNECTED ->
                        Icon(Icons.Default.BluetoothDisabled, contentDescription = null, tint = Color.White)
                    ConnectionState.CONNECTING ->
                        CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                    ConnectionState.CONNECTED ->
                        Icon(Icons.Default.BluetoothConnected, contentDescription = null, tint = Color.White)
                }
            }
        )

        Spacer(Modifier.height(16.dp))
        Text(
            text = "РЕЗУЛЬТАТ: $rez",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { vm.sendStart() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = darkGreen,
                    contentColor = Color.White
                )
            ) {
                Text("СТАРТ")
            }

            Button(
                onClick = { vm.sendStop() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text("СТОП")
            }
        }

        Spacer(Modifier.height(24.dp))

        DropdownParam(
            label = "Режим ворот",
            selectedKey = cfg.mode,
            options = modes
        ) { vm.updateConfig { copy(mode = it) } }

        SliderParam(
            label = "Время на мишень",
            value = cfg.time,
            range = 5..91,
            displayInfinityAt = 91
        ) { vm.updateConfig { copy(time = it) } }

        SliderParam(
            label = "Количество мишеней",
            value = cfg.count,
            range = 2..25
        ) { vm.updateConfig { copy(count = it) } }

        DropdownParam(
            label = "Режим пасеров",
            selectedKey = cfg.rpa,
            options = mapOf(1 to "Случайно", 2 to "Циклично", 3 to "Серия")
        ) { vm.updateConfig { copy(rpa = it) } }

        if (cfg.rpa == 3) {
            SliderParam(
                label = "Длина серии",
                value = cfg.seriesLen,
                range = 2..10
            ) { vm.updateConfig { copy(seriesLen = it) } }
        }

        Spacer(Modifier.height(16.dp))
        Text("Скорости пасеров", fontWeight = FontWeight.Medium)
        cfg.spd.forEachIndexed { idx, speed ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = speed != 0,
                    onCheckedChange = { checked ->
                        vm.updateConfig {
                            val newSpd = spd.toMutableList()
                            newSpd[idx] = if (checked) spd[idx].coerceIn(1..8) else 0
                            copy(spd = newSpd)
                        }
                    }
                )
                Text("Пасер ${idx + 1}", modifier = Modifier.width(100.dp))
                Slider(
                    value = speed.coerceAtLeast(1).toFloat(),
                    onValueChange = { float ->
                        vm.updateConfig {
                            val newSpd = spd.toMutableList()
                            newSpd[idx] = float.roundToInt().coerceIn(1..8)
                            copy(spd = newSpd)
                        }
                    },
                    valueRange = 1f..8f,
                    enabled = speed != 0,
                    modifier = Modifier.weight(1f)
                )
                Text(speed.toString(), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
