package com.example.masajeslg.data

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import com.example.masajeslg.util.startOfDayMillis
import com.example.masajeslg.util.endOfDayMillis
import com.example.masajeslg.util.monthRangeMillis

class AppointmentRepository(
    private val appointmentDao: AppointmentDao,
    private val serviceDao: ServiceDao,
    private val clientDao: ClientDao
) {
    /** Turnos crudos por rango exacto del día (lo que ya usabas) */
    fun getForDay(dayStartMillis: Long, dayEndMillis: Long) =
        appointmentDao.getBetween(dayStartMillis, dayEndMillis)

    // ==========================
    // NUEVO: soporte Calendario
    // ==========================

    /** Conteo por día (para “badges” del calendario) entre inicio/fin del mes dado */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayCountsForMonth(month: YearMonth, status: String? = null): Flow<List<DayCount>> {
        val (start, end) = monthRangeMillis(month)
        return appointmentDao.dayCountsBetween(start, end, status)
    }

    /** Lista UI de un día (para la lista inferior al seleccionar una fecha en el calendario) */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayUi(date: LocalDate): Flow<List<AppointmentUi>> {
        val start = startOfDayMillis(date)
        val end = endOfDayMillis(date)
        return appointmentDao.getDayUi(start, end)
    }

    /** Lista UI del mes (si querés mostrar todos los del mes en otra vista) */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthUi(month: YearMonth): Flow<List<AppointmentUi>> {
        val (start, end) = monthRangeMillis(month)
        return appointmentDao.getBetweenUi(start, end)
    }

    /** Próximos turnos desde ‘ahora’ (útil para pantalla Próximos o notificaciones) */
    fun getUpcoming(nowMillis: Long = System.currentTimeMillis()): Flow<List<Appointment>> =
        appointmentDao.getUpcoming(nowMillis)

    // ====================================
    // CREAR / EDITAR / ELIMINAR / ESTADO
    // ====================================

    // CREAR con validación de solapamiento — ahora devuelve el ID (para notificaciones)
    suspend fun create(
        clientId: Long,
        service: Service,
        startAt: Long,
        notes: String?
    ): Long {
        val endAt = startAt + service.durationMinutes * 60_000L
        if (appointmentDao.countOverlaps(startAt, endAt) > 0) {
            throw IllegalStateException("Ya hay un turno en ese horario")
        }
        return appointmentDao.insertReturnId(
            Appointment(
                clientId = clientId,
                serviceId = service.id,
                startAt = startAt,
                endAt = endAt,
                status = "pending",
                notes = notes?.trim()
            )
        )
    }

    // EDITAR con validación de solapamiento (excluye el propio turno)
    suspend fun update(
        id: Long,
        clientId: Long,
        serviceId: Long,
        startAt: Long,
        notes: String?
    ) {
        // buscamos el service para recalcular duración
        val service = serviceDao.getActive().first().first { it.id == serviceId }
        val endAt = startAt + service.durationMinutes * 60_000L

        if (appointmentDao.countOverlapsExcluding(id, startAt, endAt) > 0) {
            throw IllegalStateException("Ya hay un turno en ese horario")
        }
        appointmentDao.update(
            id = id,
            clientId = clientId,
            serviceId = serviceId,
            startAt = startAt,
            endAt = endAt,
            notes = notes?.trim()
        )
    }

    // ELIMINAR
    suspend fun delete(id: Long) = appointmentDao.delete(id)

    // ESTADO
    suspend fun setStatus(id: Long, status: String) =
        appointmentDao.updateStatus(id, status)

    // (Opcional) obtener por id — útil para reprogramar notificación tras editar
    suspend fun getById(id: Long): Appointment? = appointmentDao.getById(id)
}
