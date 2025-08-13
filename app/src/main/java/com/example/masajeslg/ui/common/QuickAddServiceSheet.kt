package com.example.masajeslg.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun QuickAddServiceSheet(
    onDismiss: () -> Unit,
    onSubmit: suspend (name: String, durationMin: Int, price: Double) -> Unit
) {
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("60") }
    var price by remember { mutableStateOf("0") }
    var saving by remember { mutableStateOf(false) }

    val durationInt = duration.toIntOrNull() ?: -1
    val priceDouble = price.toDoubleOrNull() ?: -1.0

    val canSave = name.trim().isNotEmpty() && durationInt > 0 && priceDouble >= 0 && !saving

    Surface(tonalElevation = 2.dp) {
        Column(Modifier.padding(16.dp)) {
            Text("Nuevo servicio", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { ch -> ch.isDigit() } }, // solo números
                label = { Text("Duración (min) *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = durationInt <= 0
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it.replace(',', '.').filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Precio") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = priceDouble < 0
            )

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            saving = true
                            try {
                                onSubmit(name.trim(), durationInt, priceDouble.coerceAtLeast(0.0))
                                onDismiss()
                            } finally {
                                saving = false
                            }
                        }
                    },
                    enabled = canSave
                ) { Text(if (saving) "Guardando..." else "Guardar") }
            }
        }
    }
}
