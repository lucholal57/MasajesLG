package com.example.masajeslg.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.masajeslg.data.Client
import com.example.masajeslg.util.openWhatsApp

@Composable
fun ClientsScreen() {
    val ctx = LocalContext.current.applicationContext
    val vm: ClientsViewModel = viewModel(factory = ClientsViewModelFactory(ctx))
    val list by vm.clients.collectAsState()

    var query by remember { mutableStateOf("") }
    val filtered = remember(list, query) {
        list.filter {
            it.name.contains(query, ignoreCase = true) ||
                    (it.phone?.contains(query, ignoreCase = true) == true)
        }
    }

    var editing by remember { mutableStateOf<Client?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Client?>(null) }

    // snackbars
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.uiMsg.collect { msg -> snack.showSnackbar(msg) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snack) },
        topBar = {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Clientes", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Buscar por nombre o teléfono...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        floatingActionButton = { FloatingActionButton(onClick = { showCreate = true }) { Text("+") } }
    ) { padding ->
        if (filtered.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin clientes", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { c ->
                    ClientCard(
                        client = c,
                        onEdit = { editing = c },
                        onDelete = { toDelete = c }
                    )
                }
            }
        }
    }

    // Crear
    if (showCreate) {
        ClientDialog(
            onDismiss = { showCreate = false },
            onSave = { name, phone, notes ->
                vm.add(name, phone, notes)
                showCreate = false
            }
        )
    }

    // Editar
    editing?.let { c ->
        ClientDialog(
            onDismiss = { editing = null },
            onSave = { name, phone, notes ->
                vm.update(c.id, name, phone, notes)
                editing = null
            },
            initialName = c.name,
            initialPhone = c.phone,
            initialNotes = c.notes,
            confirmLabel = "Guardar cambios"
        )
    }

    // Confirmar eliminar
    toDelete?.let { cli ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar cliente") },
            text = { Text("Esto eliminará “${cli.name}” de forma permanente.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(cli.id)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ClientCard(
    client: Client,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    ElevatedCard {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(client.name, style = MaterialTheme.typography.titleMedium)

                    // Teléfono + acciones (Llamar / WhatsApp con plantillas)
                    client.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Llamar
                            AssistChip(
                                onClick = {
                                    val clean = phone.filter { it.isDigit() || it == '+' }
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_DIAL,
                                        android.net.Uri.parse("tel:$clean")
                                    )
                                    ctx.startActivity(intent)
                                },
                                label = { Text(phone) },
                                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) }
                            )

                            // WhatsApp con menú de plantillas
                            var showWaMenu by remember { mutableStateOf(false) }
                            Box {
                                AssistChip(
                                    onClick = { showWaMenu = true },
                                    label = { Text("WhatsApp") },
                                    leadingIcon = { Icon(Icons.Default.Send, contentDescription = null) }
                                )
                                DropdownMenu(
                                    expanded = showWaMenu,
                                    onDismissRequest = { showWaMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Saludo") },
                                        onClick = {
                                            val msg = "Hola ${client.name}, te escribo de Masajes LG ✨"
                                            openWhatsApp(ctx, phone, msg)
                                            showWaMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Recordatorio de turno") },
                                        onClick = {
                                            val msg = "Hola ${client.name}, ¿confirmás tu turno de masaje? " +
                                                    "Si necesitás cambiar el horario, avisame por acá 🙂"
                                            openWhatsApp(ctx, phone, msg)
                                            showWaMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Reagendar") },
                                        onClick = {
                                            val msg = "Hola ${client.name}, ¿podemos reprogramar tu turno? " +
                                                    "Decime días/horarios que te vengan bien y lo acomodamos."
                                            openWhatsApp(ctx, phone, msg)
                                            showWaMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Post-sesión") },
                                        onClick = {
                                            val msg = "¡Gracias por venir hoy, ${client.name}! 🙌 " +
                                                    "Cualquier molestia o consulta, escribime y lo vemos."
                                            openWhatsApp(ctx, phone, msg)
                                            showWaMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Notas (si hay)
                    client.notes?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Notas: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Menú ⋮ (Editar / Eliminar)
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
private fun ClientDialog(
    onDismiss: () -> Unit,
    onSave: (String, String?, String?) -> Unit,
    initialName: String = "",
    initialPhone: String? = null,
    initialNotes: String? = null,
    confirmLabel: String = "Guardar"
) {
    var name by remember { mutableStateOf(TextFieldValue(initialName)) }
    var phone by remember { mutableStateOf(TextFieldValue(initialPhone.orEmpty())) }
    var notes by remember { mutableStateOf(TextFieldValue(initialNotes.orEmpty())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isBlank()) "Nuevo cliente" else "Editar cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre *") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") })
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.text.isNotBlank(),
                onClick = {
                    onSave(name.text.trim(), phone.text.ifBlank { null }, notes.text.ifBlank { null })
                }
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
