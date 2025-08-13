package com.example.masajeslg.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun QuickAddClientSheet(
    onDismiss: () -> Unit,
    onSubmit: suspend (fullName: String, phone: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val canSave = name.trim().isNotEmpty() && !saving

    Surface(tonalElevation = 2.dp) {
        Column(Modifier.padding(16.dp)) {
            Text("Nuevo cliente", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre y apellido *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono (opcional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        saving = true
                        // correr en caller (scope externo) — acá solo señalamos intención
                    },
                    enabled = canSave
                ) { Text(if (saving) "Guardando..." else "Guardar") }
            }
        }
    }

    // Nota: este sheet no ejecuta la corrutina por sí mismo.
    // Úsalo así desde tu pantalla:
    // if (showClientSheet) {
    //   ModalBottomSheet(onDismissRequest={show=false}) {
    //     QuickAddClientSheet(onDismiss={show=false}) { name, phone ->
    //        viewModelScope.launch { repoClient.create(name, phone); show=false }
    //     }
    //   }
    // }
}
