package com.example.masajeslg.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.masajeslg.util.asCurrency
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen() {
    val ctx = LocalContext.current.applicationContext
    val vm: StatsViewModel = viewModel(factory = StatsViewModelFactory(ctx))

    val byDay by vm.byDay.collectAsState()
    val byService by vm.byService.collectAsState()
    val totalCount by vm.totalCount.collectAsState()
    val totalRevenue by vm.totalRevenue.collectAsState()
    val (start, end) = vm.range.collectAsState().value

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Estadísticas", style = MaterialTheme.typography.headlineSmall)

        // Rango de fechas
        DateRangeRow(start = start, end = end, onChange = vm::setRange)

        // KPIs
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard(title = "Turnos", value = totalCount.toString(), modifier = Modifier.weight(1f))
            KpiCard(title = "Ingresos", value = totalRevenue.asCurrency(), modifier = Modifier.weight(1f))
        }

        // Por día (últimos 30 días o rango elegido)
        SectionCard(title = "Turnos por día") {
            val data = byDay.map { it.day.substring(5) to it.count.toFloat() } // "MM-DD"
            if (data.isEmpty()) EmptyMini() else SimpleBarChart(data = data, height = 160.dp)
        }

        // Ingresos por servicio
        SectionCard(title = "Ingresos por servicio") {
            val data = byService.map { it.serviceName to it.total.toFloat() }
            if (data.isEmpty()) EmptyMini() else SimpleBarChartCurrency(data = data, height = 180.dp)
        }
    }
}

@Composable
private fun DateRangeRow(start: Long, end: Long, onChange: (Long, Long) -> Unit) {
    val ctx = LocalContext.current
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val cal = remember { Calendar.getInstance() }

    fun pickDate(initial: Long, onPicked: (Long) -> Unit) {
        cal.timeInMillis = initial
        DatePickerDialog(
            ctx,
            { _, y, m, d -> cal.set(y, m, d, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0); onPicked(cal.timeInMillis) },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { pickDate(start) { onChange(it, end) } }) { Text(fmt.format(Date(start))) }
        OutlinedButton(onClick = { pickDate(end)   { onChange(start, it + (24*60*60*1000 - 1)) } }) { Text(fmt.format(Date(end))) }
    }
}

@Composable
private fun KpiCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable private fun EmptyMini() {
    Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
        Text("Sin datos en este rango", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Barras simples iguales de ancho (sin librerías externas) */
@Composable
private fun SimpleBarChart(data: List<Pair<String, Float>>, height: Dp) {
    val max = (data.maxOfOrNull { it.second } ?: 0f).coerceAtLeast(1f)
    val barMax = height - 24.dp
    Row(
        Modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, v) ->
            val h = barMax * (v / max)
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    Modifier
                        .height(h)
                        .fillMaxWidth(0.5f)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            RoundedCornerShape(8.dp)
                        )
                )
                Spacer(Modifier.height(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SimpleBarChartCurrency(data: List<Pair<String, Float>>, height: Dp) {
    val max = (data.maxOfOrNull { it.second } ?: 0f).coerceAtLeast(1f)
    val barMax = height - 24.dp
    Row(
        Modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, v) ->
            val h = barMax * (v / max)
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    Modifier
                        .height(h)
                        .fillMaxWidth(0.5f)
                        .background(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f),
                            RoundedCornerShape(8.dp)
                        )
                )
                Spacer(Modifier.height(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
