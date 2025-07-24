package com.example.gatecontrol.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun <K> DropdownParam(
    label: String,
    selectedKey: K,
    options: Map<K, String>,
    onSelect: (K) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
    ) {
        Text(text = label, color = Color.Black)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) { Text(options[selectedKey] ?: selectedKey.toString()) }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { (key, title) ->
                    DropdownMenuItem(onClick = {
                        onSelect(key)
                        expanded = false
                    }) { Text(text = title, color = Color.Black) }
                }
            }
        }
    }
}

@Composable
fun SliderParam(
    label: String,
    value: Int,
    range: IntRange,
    displayInfinityAt: Int? = null,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val displayText = if (displayInfinityAt != null && value == displayInfinityAt) "∞" else value.toString()
        Text("$label: $displayText", color = Color.Black)
        Slider(
            value = value.coerceIn(range).toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1,
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color.Black,
                inactiveTrackColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}