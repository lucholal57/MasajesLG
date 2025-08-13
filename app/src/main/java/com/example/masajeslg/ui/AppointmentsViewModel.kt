package com.example.masajeslg.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.masajeslg.data.*
import com.example.masajeslg.util.endOfDayMillis
import com.example.masajeslg.util.startOfDayMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class AppointmentsViewModel(
    private val repo: AppointmentRepository,
    private val db: AppDatabase
) : ViewModel() {

    // ---------------- UI events (snackbar) ----------------
    private val _uiMsg = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val uiMsg: SharedFlow<String> = _uiMsg.asSharedFlow()

    // ---------------- Estado de calendario ----------------
    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    /** Conteo por día del mes para pintar badges en el calendario */
    @RequiresApi(Build.VERSION_CODES.O)
    val dayCounts: StateFlow<List<DayCount>> =
        currentMonth
            .flatMapLatest { month -> repo.getDayCountsForMonth(month, status = null) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Turnos UI del día seleccionado (lista inferior) */
    @RequiresApi(Build.VERSION_CODES.O)
    val appointmentsForSelectedDay: StateFlow<List<AppointmentUi>> =
        selectedDate
            .flatMapLatest { date -> repo.getDayUi(date) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---------------- Navegación de calendario ----------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun prevMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }
    @RequiresApi(Build.VERSION_CODES.O)
    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }

    @RequiresApi(Build.VERSION_CODES.O)
    fun selectDate(date: LocalDate) { _selectedDate.value = date }

    /** Compatibilidad con tu método anterior: ir a un día por millis */
    @RequiresApi(Build.VERSION_CODES.O)
    fun goToDay(timeMillis: Long) {
        // Mantengo tu helper de bounds como compat, pero fijamos selectedDate.
        val date = java.time.Instant.ofEpochMilli(timeMillis)
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        _selectedDate.value = date
        _currentMonth.value = YearMonth.from(date)
    }

    // ---------------- CRUD ----------------

    /** CREAR con validación (retorna id) y hook para recordatorio */
    @RequiresApi(Build.VERSION_CODES.O)
    fun create(
        clientId: Long,
        serviceId: Long,
        startAt: Long,
        notes: String?,
        reminderMinutesBefore: Int? = 60 // 30 o 60; null para no recordar
    ) {
        viewModelScope.launch {
            val service = db.serviceDao().getActive().first().first { it.id == serviceId }
            try {
                val newId = repo.create(clientId, service, startAt, notes)
                _uiMsg.tryEmit("Turno creado")

                // Hook para programar notificación local (lo implementamos luego)
                if (reminderMinutesBefore != null) {
                    scheduleReminder(
                        appointmentId = newId,
                        startAt = startAt,
                        minutesBefore = reminderMinutesBefore
                    )
                }

                // Si el turno creado cae en otro mes/día, actualizamos selección
                val createdDate = java.time.Instant.ofEpochMilli(startAt)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                _currentMonth.value = YearMonth.from(createdDate)
                _selectedDate.value = createdDate

            } catch (e: IllegalStateException) {
                _uiMsg.tryEmit("Ya hay un turno en ese horario")
            } catch (e: Throwable) {
                _uiMsg.tryEmit("Error al crear turno")
            }
        }
    }

    /** EDITAR con validación */
    @RequiresApi(Build.VERSION_CODES.O)
    fun update(
        id: Long,
        clientId: Long,
        serviceId: Long,
        startAt: Long,
        notes: String?,
        reminderMinutesBefore: Int? = 60 // reprogramar si cambia fecha/hora
    ) {
        viewModelScope.launch {
            try {
                repo.update(id, clientId, serviceId, startAt, notes)
                _uiMsg.tryEmit("Cambios guardados")

                // Reprogramar recordatorio si corresponde
                if (reminderMinutesBefore != null) {
                    scheduleReminder(
                        appointmentId = id,
                        startAt = startAt,
                        minutesBefore = reminderMinutesBefore
                    )
                }

                // refrescar selección para llevarte al día editado
                val editedDate = java.time.Instant.ofEpochMilli(startAt)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                _currentMonth.value = YearMonth.from(editedDate)
                _selectedDate.value = editedDate

            } catch (e: IllegalStateException) {
                _uiMsg.tryEmit("Ya hay un turno en ese horario")
            } catch (e: Throwable) {
                _uiMsg.tryEmit("Error al actualizar turno")
            }
        }
    }

    /** ELIMINAR */
    fun delete(id: Long) {
        viewModelScope.launch {
            try {
                repo.delete(id)
                _uiMsg.tryEmit("Turno eliminado")
            } catch (e: Throwable) {
                _uiMsg.tryEmit("Error al eliminar turno")
            }
        }
    }

    fun setStatus(id: Long, status: String) {
        viewModelScope.launch {
            try {
                repo.setStatus(id, status)
                val label = when (status) {
                    "done" -> "realizado"
                    "canceled" -> "cancelado"
                    else -> status
                }
                _uiMsg.tryEmit("Estado: $label")
            } catch (e: Throwable) {
                _uiMsg.tryEmit("Error al cambiar estado")
            }
        }
    }

    // ---------------- Helpers previos (por compat) ----------------
    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("unused")
    private fun dayBounds(timeMillis: Long): Pair<Long, Long> {
        val date = java.time.Instant.ofEpochMilli(timeMillis)
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val start = startOfDayMillis(date)
        val end = endOfDayMillis(date)
        return start to end
    }

    // ---------------- Recordatorios (hook) ----------------
    private fun scheduleReminder(appointmentId: Long, startAt: Long, minutesBefore: Int) {
        // TODO: lo implementamos cuando agreguemos WorkManager:
        // ReminderWorker.schedule(context, appointmentId, startAt, minutesBefore)
        // Por ahora queda como placeholder para no romper el flujo.
    }
}
