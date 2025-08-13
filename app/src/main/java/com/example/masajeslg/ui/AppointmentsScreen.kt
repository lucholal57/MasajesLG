package com.example.masajeslg.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.masajeslg.data.AppDatabase
import com.example.masajeslg.data.AppointmentUi
import com.example.masajeslg.data.Client
import com.example.masajeslg.data.Service
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ================== estilos de estado ==================
data class StatusStyle(
    val label: String,
    val container: Color,
    val onContainer: Color,
    val icon: ImageVector
)

@Composable
private fun statusStyle(status: String): StatusStyle {
    return when (status) {
        "done" -> StatusStyle("Realizado", Color(0xFFE6F4EA), Color(0xFF1E8E3E), Icons.Rounded.CheckCircle)
        "canceled" -> StatusStyle("Cancelado", Color(0xFFFDECEA), Color(0xFFD93025), Icons.Rounded.Cancel)
        else -> StatusStyle("Pendiente", Color(0xFFFFF8E1), Color(0xFF8A6D00), Icons.Rounded.Schedule)
    }
}

// ================== pantalla principal ==================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppointmentsScreen() {
    val ctx = LocalContext.current
    val appCtx = ctx.applicationContext
    val vm: AppointmentsViewModel = viewModel(factory = AppointmentsViewModelFactory(appCtx))
    val snack = remember { SnackbarHostState() }

    var showCreate by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AppointmentUi?>(null) }

    LaunchedEffect(Unit) {
        vm.uiMsg.collect { msg -> snack.showSnackbar(message = msg) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snack) },
        floatingActionButton = { /* FAB lo maneja CalendarScreen */ }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            CalendarScreen(
                viewModel = vm,
                onAddClick = { showCreate = true }
            )
        }
    }

    // Crear
    if (showCreate) {
        CreateAppointmentDialog(
            onDismiss = { showCreate = false },
            onSave = { clientId, serviceId, whenMillis, notes ->
                vm.create(
                    clientId = clientId,
                    serviceId = serviceId,
                    startAt = whenMillis,
                    notes = notes,
                    reminderMinutesBefore = 30
                )
                showCreate = false
            }
        )
    }

    // Editar
    editing?.let { row ->
        CreateAppointmentDialog(
            onDismiss = { editing = null },
            onSave = { clientId, serviceId, whenMillis, notes ->
                vm.update(
                    id = row.appointment.id,
                    clientId = clientId,
                    serviceId = serviceId,
                    startAt = whenMillis,
                    notes = notes,
                    reminderMinutesBefore = 30
                )
                editing = null
            },
            initialClientId = row.appointment.clientId,
            initialServiceId = row.appointment.serviceId,
            initialWhenMillis = row.appointment.startAt,
            initialNotes = row.appointment.notes,
            confirmLabel = "Guardar cambios"
        )
    }
}

// ================== diálogo crear/editar turno ==================
@Composable
private fun CreateAppointmentDialog(
    onDismiss: () -> Unit,
    onSave: (Long, Long, Long, String?) -> Unit,
    // edición
    initialClientId: Long? = null,
    initialServiceId: Long? = null,
    initialWhenMillis: Long? = null,
    initialNotes: String? = null,
    confirmLabel: String = "Guardar"
) {
    val ctx = LocalContext.current
    val appCtx = ctx.applicationContext
    val db = remember { AppDatabase.get(appCtx) }
    val scope = rememberCoroutineScope()

    var selectedClientId by remember { mutableStateOf<Long?>(initialClientId) }
    var selectedServiceId by remember { mutableStateOf<Long?>(initialServiceId) }
    var notes by remember { mutableStateOf(initialNotes.orEmpty()) }
    var whenMillis by remember { mutableStateOf(initialWhenMillis ?: System.currentTimeMillis()) }

    // Flujos que se actualizan solos al insertar
    val clients by db.clientDao().getAll().collectAsState(initial = emptyList())
    val services by db.serviceDao().getActive().collectAsState(initial = emptyList())

    // Subdiálogos
    var showNewClient by remember { mutableStateOf(false) }
    var showNewService by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialClientId == null) "Nuevo turno" else "Editar turno") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // -------- Cliente --------
                ExposedDropdownWithActions(
                    label = "Cliente *",
                    items = clients.map { it.id to it.name },
                    selectedId = selectedClientId,
                    onSelected = { selectedClientId = it },
                    onAddNew = { showNewClient = true },
                    addLabel = "Nuevo cliente"
                )

                // -------- Servicio --------
                ExposedDropdownWithActions(
                    label = "Servicio *",
                    items = services.map { it.id to it.name },
                    selectedId = selectedServiceId,
                    onSelected = { selectedServiceId = it },
                    onAddNew = { showNewService = true },
                    addLabel = "Nuevo servicio"
                )

                DateTimePickerRow(
                    millis = whenMillis,
                    onPick = { whenMillis = it }
                )

                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val enabled = selectedClientId != null && selectedServiceId != null
            TextButton(enabled = enabled, onClick = {
                onSave(selectedClientId!!, selectedServiceId!!, whenMillis, notes.ifBlank { null })
            }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    // ----- Diálogo: Nuevo Cliente -----
    if (showNewClient) {
        NewClientDialog(
            onDismiss = { showNewClient = false },
            onCreated = { newId ->
                selectedClientId = newId
                showNewClient = false
            }
        )
    }

    // ----- Diálogo: Nuevo Servicio -----
    if (showNewService) {
        NewServiceDialog(
            onDismiss = { showNewService = false },
            onCreated = { newId ->
                selectedServiceId = newId
                showNewService = false
            }
        )
    }
}

// ================== pickers ==================
@Composable
private fun DateTimePickerRow(millis: Long, onPick: (Long) -> Unit) {
    val ctx = LocalContext.current
    val cal = remember(millis) { Calendar.getInstance().apply { timeInMillis = millis } }
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = {
            DatePickerDialog(
                ctx,
                { _, y, m, dOfM ->
                    cal.set(y, m, dOfM)
                    onPick(cal.timeInMillis)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) { Text(dateFmt.format(Date(millis))) }

        OutlinedButton(onClick = {
            TimePickerDialog(
                ctx,
                { _, h, min ->
                    cal.set(Calendar.HOUR_OF_DAY, h)
                    cal.set(Calendar.MINUTE, min)
                    onPick(cal.timeInMillis)
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }) { Text(timeFmt.format(Date(millis))) }
    }
}

// ================== dropdown con botón "Nuevo" ==================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownWithActions(
    label: String,
    items: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
    onAddNew: () -> Unit,
    addLabel: String
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = items.firstOrNull { it.first == selectedId }?.second ?: ""

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                items.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = { onSelected(id); expanded = false }
                    )
                }
            }
        }
        // acción para crear rápido
        TextButton(onClick = onAddNew) { Text(addLabel) }
    }
}

// ================== subdiálogo: crear cliente ==================
@Composable
private fun NewClientDialog(
    onDismiss: () -> Unit,
    onCreated: (Long) -> Unit
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx.applicationContext) }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nombre *") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Teléfono") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val enabled = name.isNotBlank()
            TextButton(enabled = enabled, onClick = {
                scope.launch {
                    // ⚠️ Ajustá el constructor según tu entidad `Client`
                    val newId = db.clientDao().insert(
                        Client(
                            id = 0, // autogen por Room
                            name = name.trim(),
                            phone = phone.ifBlank { null }
                        )
                    )
                    onCreated(newId)
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ================== subdiálogo: crear servicio ==================
@Composable
private fun NewServiceDialog(
    onDismiss: () -> Unit,
    onCreated: (Long) -> Unit
) {
    val ctx = LocalContext.current
    val db = remember { AppDatabase.get(ctx.applicationContext) }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("60") }
    var priceText by remember { mutableStateOf("") }

    val durationInt = duration.toIntOrNull() ?: -1
    val price = priceText.replace(',', '.').toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo servicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nombre *") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duration, onValueChange = { duration = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Duración (min) *") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    isError = durationInt <= 0
                )
                OutlinedTextField(
                    value = priceText, onValueChange = { priceText = it.replace(',', '.').filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Precio") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val enabled = name.isNotBlank() && durationInt > 0
            TextButton(enabled = enabled, onClick = {
                scope.launch {
                    val newId = db.serviceDao().insert(
                        Service(
                            id = 0,
                            name = name.trim(),
                            durationMinutes = durationInt,
                            price = (price ?: 0.0),
                            active = true
                        )
                    )
                    onCreated(newId)
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

