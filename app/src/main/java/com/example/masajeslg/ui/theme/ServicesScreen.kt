package com.example.masajeslg.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.masajeslg.data.Service
import com.example.masajeslg.util.asCurrency

@Composable
fun ServicesScreen() {
    val ctx = LocalContext.current.applicationContext
    val vm: ServicesViewModel = viewModel(factory = ServicesViewModelFactory(ctx))
    val list by vm.services.collectAsState()

    var query by remember { mutableStateOf("") }
    val filtered = remember(list, query) {
        list.filter { it.name.contains(query, ignoreCase = true) }
    }

    var editing by remember { mutableStateOf<Service?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Service?>(null) }

    // Snackbars
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        // requiere vm.uiMsg en tu ViewModel (ya te lo pasé)
        vm.uiMsg.collect { msg -> snack.showSnackbar(msg) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snack) },
        topBar = {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Servicios", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Buscar servicio...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        floatingActionButton = { FloatingActionButton(onClick = { showCreate = true }) { Text("+") } }
    ) { padding ->
        if (filtered.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin servicios", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { s ->
                    ServiceCard(
                        service = s,
                        onEdit = { editing = s },
                        onDelete = { toDelete = s }
                    )
                }
            }
        }
    }

    // Crear
    if (showCreate) {
        ServiceDialog(
            onDismiss = { showCreate = false },
            onSave = { name, minutes, price ->
                vm.add(name, minutes, price)
                showCreate = false
            }
        )
    }

    // Editar
    editing?.let { s ->
        ServiceDialog(
            onDismiss = { editing = null },
            onSave = { name, minutes, price ->
                vm.update(s.id, name, minutes, price)
                editing = null
            },
            initialName = s.name,
            initialMinutes = s.durationMinutes,
            initialPrice = s.price,
            confirmLabel = "Guardar cambios"
        )
    }

    // Confirmar eliminar
    toDelete?.let { svc ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar servicio") },
            text = { Text("Esto eliminará “${svc.name}” de forma permanente.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(svc.id)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ServiceCard(
    service: Service,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(service.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text("${service.durationMinutes} min") })
                        AssistChip(onClick = {}, label = { Text(service.price.asCurrency()) })
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Editar") }, onClick = { onEdit(); showMenu = false })
                        DropdownMenuItem(text = { Text("Eliminar") }, onClick = { onDelete(); showMenu = false })
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int, Double) -> Unit,
    initialName: String = "",
    initialMinutes: Int = 60,
    initialPrice: Double = 0.0,
    confirmLabel: String = "Guardar"
) {
    var name by remember { mutableStateOf(TextFieldValue(initialName)) }
    var minutes by remember { mutableStateOf(TextFieldValue(initialMinutes.toString())) }
    var price by remember { mutableStateOf(TextFieldValue(if (initialPrice == 0.0) "" else initialPrice.toString())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isBlank()) "Nuevo servicio" else "Editar servicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre *") })
                OutlinedTextField(minutes, { minutes = it }, label = { Text("Duración (min) *") })
                OutlinedTextField(price, { price = it }, label = { Text("Precio $ *") })
            }
        },
        confirmButton = {
            val canSave =
                name.text.isNotBlank() &&
                        (minutes.text.toIntOrNull() ?: 0) > 0 &&
                        (price.text.replace(",", ".").toDoubleOrNull() ?: -1.0) >= 0.0

            TextButton(enabled = canSave, onClick = {
                onSave(
                    name.text.trim(),
                    minutes.text.toInt(),
                    price.text.replace(",", ".").toDouble()
                )
            }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
