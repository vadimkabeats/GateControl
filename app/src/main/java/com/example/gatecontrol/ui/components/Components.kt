package com.example.gatecontrol.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        Text(text = label, modifier = Modifier.padding(bottom = 4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(options[selectedKey] ?: selectedKey.toString())
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { (key, title) ->
                    DropdownMenuItem(onClick = {
                        onSelect(key)
                        expanded = false
                    }) {
                        Text(text = title)
                    }
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
        Text("$label: $displayText")
        Slider(
            value = value.coerceIn(range).toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}
