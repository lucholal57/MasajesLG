package com.example.masajeslg.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.masajeslg.data.AppointmentUi
import com.example.masajeslg.util.openWhatsApp
import com.example.masajeslg.util.parseSqlDayToLocalDate
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    viewModel: AppointmentsViewModel,
    onAddClick: () -> Unit = {}
) {
    val month by viewModel.currentMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dayCounts by viewModel.dayCounts.collectAsState()
    val appointments by viewModel.appointmentsForSelectedDay.collectAsState()

    val locale = Locale("es", "AR")
    val countsMap = remember(dayCounts) {
        dayCounts.associate { parseSqlDayToLocalDate(it.day) to it.count }
    }

    // Confirmar borrado
    var toDelete by remember { mutableStateOf<AppointmentUi?>(null) }

    // Mostrar/ocultar calendario
    var showCalendar by remember { mutableStateOf(true) }

    // Altura máxima del calendario (accesibilidad)
    val fontScale = LocalConfiguration.current.fontScale
    val maxCalendarHeight = when {
        fontScale >= 1.6f -> 280.dp
        fontScale >= 1.3f -> 300.dp
        else -> 320.dp
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${month.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.titlecase(locale) }} ${month.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { viewModel.prevMonth() }) { Text("⟵") }
                        TextButton(onClick = { viewModel.nextMonth() }) { Text("⟶") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { showCalendar = !showCalendar }) {
                            Text(if (showCalendar) "Ver turnos" else "Ver calendario")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // ===== CALENDARIO PLEGABLE =====
            AnimatedVisibility(visible = showCalendar) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxCalendarHeight)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Encabezado días (L..D)
                    Row(Modifier.fillMaxWidth()) {
                        DayOfWeek.values().forEach { dow ->
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(
                                    text = dow.getDisplayName(TextStyle.SHORT, locale).uppercase(locale),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Grilla del mes
                    MonthGrid(
                        month = month,
                        selected = selectedDate,
                        counts = countsMap,
                        onDayClick = { viewModel.selectDate(it) }
                    )

                    Spacer(Modifier.height(12.dp))

                    // Leyenda
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        LegendDot(color = MaterialTheme.colorScheme.tertiary)
                        Text(
                            " = turnos en el día",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(12.dp))
                        LegendSquare(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        Text(
                            " = día seleccionado",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            // ===== FIN CALENDARIO =====

            Spacer(Modifier.height(10.dp))
            Divider()

            Text(
                text = "Turnos del ${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )

            // Lista (ocupa el resto SIEMPRE)
            val listState = rememberLazyListState()
            if (appointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text("No hay turnos en esta fecha.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(items = appointments, key = { it.appointment.id }) { ap ->
                        AppointmentRow(
                            ap = ap,
                            onSetStatus = { newStatus ->
                                viewModel.setStatus(ap.appointment.id, newStatus)
                            },
                            onDelete = { toDelete = ap }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    // Diálogo confirmar eliminar
    toDelete?.let { row ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar turno") },
            text = { Text("¿Eliminar el turno de ${row.clientName} a las ${formatHour(row.appointment.startAt)}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(row.appointment.id)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthGrid(
    month: YearMonth,
    selected: LocalDate,
    counts: Map<LocalDate, Int>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstOfMonth = month.atDay(1)
    val firstIndex = ((firstOfMonth.dayOfWeek.value + 6) % 7) // Lunes=0
    val daysInMonth = month.lengthOfMonth()
    val totalCells = firstIndex + daysInMonth
    val rows = ceil(totalCells / 7.0).toInt()

    Column {
        var dayNum = 1
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = col + (7 * row)
                    val isBlank = cellIndex < firstIndex || dayNum > daysInMonth
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isBlank) {
                            val date = month.atDay(dayNum)
                            val count = counts[date] ?: 0
                            DayCell(
                                date = date,
                                isSelected = date == selected,
                                count = count,
                                onClick = { onDayClick(date) }
                            )
                            dayNum++
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .background(bg)
            .clickable { onClick() }
            .padding(top = 6.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiary,
                    textAlign = TextAlign.Center,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Spacer(Modifier.height(18.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AppointmentRow(
    ap: AppointmentUi,
    onSetStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val statusKey = ap.appointment.status.lowercase()
    val statusColor = when (statusKey) {
        "pending", "scheduled" -> MaterialTheme.colorScheme.primary
        "done" -> Color(0xFF2E7D32)
        "canceled", "cancelled" -> Color(0xFFC62828)
        "no_show", "ausente" -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.secondary
    }
    var menuOpen by remember { mutableStateOf(false) }

    val precio = remember(ap.servicePrice) {
        NumberFormat.getCurrencyInstance(Locale("es", "AR")).format(ap.servicePrice)
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "${formatHour(ap.appointment.startAt)} • ${ap.clientName}",
                fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${ap.serviceName} • $precio",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = when (statusKey) {
                "pending", "scheduled" -> "Pendiente"
                "done" -> "Realizado"
                "canceled", "cancelled" -> "Cancelado"
                "no_show", "ausente" -> "Ausente"
                else -> ap.appointment.status
            },
            color = statusColor,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )

        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Pendiente") }, onClick = { onSetStatus("pending"); menuOpen = false })
                DropdownMenuItem(text = { Text("Realizado") }, onClick = { onSetStatus("done"); menuOpen = false })
                DropdownMenuItem(text = { Text("Cancelado") }, onClick = { onSetStatus("canceled"); menuOpen = false })
                Divider()
                DropdownMenuItem(
                    text = { Text("Recordatorio WhatsApp") },
                    enabled = !ap.clientPhone.isNullOrBlank(),
                    onClick = {
                        val phone = ap.clientPhone!!.filter { it.isDigit() || it == '+' }
                        val fecha = Instant.ofEpochMilli(ap.appointment.startAt).atZone(ZoneId.systemDefault())
                        val dia = "%02d/%02d/%04d".format(fecha.dayOfMonth, fecha.monthValue, fecha.year)
                        val hora = "%02d:%02d".format(fecha.hour, fecha.minute)
                        val serv = ap.serviceName
                        val msg = "Hola ${ap.clientName}, te recuerdo $serv el $dia a las $hora. Si necesitás cambiar el horario, avisame por acá 🙂"
                        openWhatsApp(context, phone, msg)
                        menuOpen = false
                    }
                )
                Divider()
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = { onDelete(); menuOpen = false }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatHour(startAtMillis: Long): String {
    val zone = ZoneId.systemDefault()
    val t = Instant.ofEpochMilli(startAtMillis).atZone(zone).toLocalTime()
    return "%02d:%02d".format(t.hour, t.minute)
}

@Composable private fun LegendDot(color: Color) {
    Box(Modifier.size(10.dp).clip(CircleShape).background(color))
    Spacer(Modifier.width(6.dp))
}
@Composable private fun LegendSquare(color: Color) {
    Box(Modifier.size(14.dp).clip(MaterialTheme.shapes.small).background(color))
    Spacer(Modifier.width(6.dp))
}
