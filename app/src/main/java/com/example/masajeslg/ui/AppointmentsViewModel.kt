package com.example.masajeslg.ui

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.masajeslg.data.*
import com.example.masajeslg.work.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class AppointmentsViewModel(
    private val repo: AppointmentRepository,
    private val db: AppDatabase,
    private val appContext: Context
) : ViewModel() {

    private val _uiMsg = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val uiMsg: SharedFlow<String> = _uiMsg.asSharedFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    val dayCounts: StateFlow<List<DayCount>> =
        currentMonth.flatMapLatest { month -> repo.getDayCountsForMonth(month, status = null) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @RequiresApi(Build.VERSION_CODES.O)
    val appointmentsForSelectedDay: StateFlow<List<AppointmentUi>> =
        selectedDate.flatMapLatest { date -> repo.getDayUi(date) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // -------- calendario --------
    @RequiresApi(Build.VERSION_CODES.O) fun prevMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }
    @RequiresApi(Build.VERSION_CODES.O) fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }
    @RequiresApi(Build.VERSION_CODES.O) fun selectDate(date: LocalDate) { _selectedDate.value = date }

    // -------- crear/editar/eliminar --------
    @RequiresApi(Build.VERSION_CODES.O)
    fun create(
        clientId: Long,
        serviceId: Long,
        startAt: Long,
        notes: String?,
        reminderMinutesBefore: Int? = 30   // ← por defecto 30 min
    ) {
        viewModelScope.launch {
            val service = db.serviceDao().getActive().first().first { it.id == serviceId }
            runCatching { repo.create(clientId, service, startAt, notes) }
                .onSuccess { newId ->
                    _uiMsg.tryEmit("Turno creado")
                    // programa recordatorio
                    reminderMinutesBefore?.let { scheduleReminder(newId, startAt, it) }

                    val createdDate = java.time.Instant.ofEpochMilli(startAt)
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    _currentMonth.value = YearMonth.from(createdDate)
                    _selectedDate.value = createdDate
                }
                .onFailure { e ->
                    _uiMsg.tryEmit(if (e is IllegalStateException) "Ya hay un turno en ese horario" else "Error al crear turno")
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun update(
        id: Long,
        clientId: Long,
        serviceId: Long,
        startAt: Long,
        notes: String?,
        reminderMinutesBefore: Int? = 30
    ) {
        viewModelScope.launch {
            runCatching { repo.update(id, clientId, serviceId, startAt, notes) }
                .onSuccess {
                    _uiMsg.tryEmit("Cambios guardados")
                    // reprograma recordatorio
                    reminderMinutesBefore?.let { scheduleReminder(id, startAt, it) }

                    val editedDate = java.time.Instant.ofEpochMilli(startAt)
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    _currentMonth.value = YearMonth.from(editedDate)
                    _selectedDate.value = editedDate
                }
                .onFailure { e ->
                    _uiMsg.tryEmit(if (e is IllegalStateException) "Ya hay un turno en ese horario" else "Error al actualizar turno")
                }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            runCatching {
                // cancela recordatorio si existía
                ReminderScheduler.cancel(appContext, id)
                repo.delete(id)
            }
                .onSuccess { _uiMsg.tryEmit("Turno eliminado") }
                .onFailure { _uiMsg.tryEmit("Error al eliminar turno") }
        }
    }

    fun setStatus(id: Long, status: String) {
        viewModelScope.launch {
            runCatching { repo.setStatus(id, status) }
                .onSuccess {
                    // si se cancela o marca como realizado, no tiene sentido recordar
                    if (status == "canceled" || status == "done") {
                        ReminderScheduler.cancel(appContext, id)
                    }
                    val label = when (status) { "done" -> "realizado"; "canceled" -> "cancelado"; else -> "pendiente" }
                    _uiMsg.tryEmit("Estado: $label")
                }
                .onFailure { _uiMsg.tryEmit("Error al cambiar estado") }
        }
    }

    // -------- recordatorios --------
    private fun scheduleReminder(appointmentId: Long, startAt: Long, minutesBefore: Int) {
        ReminderScheduler.schedule(appContext, appointmentId, startAt, minutesBefore)
    }
}
