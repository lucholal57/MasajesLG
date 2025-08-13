package com.example.masajeslg.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    // ----------- EXISTENTES -----------

    @Query(
        """
    SELECT COUNT(*) FROM appointments
    WHERE (:start < endAt) AND (:end > startAt)
      AND status != 'canceled'
    """
    )
    suspend fun countOverlaps(start: Long, end: Long): Int

    @Query(
        """
    SELECT COUNT(*) FROM appointments
    WHERE id != :id AND (:start < endAt) AND (:end > startAt)
      AND status != 'canceled'
    """
    )
    suspend fun countOverlapsExcluding(id: Long, start: Long, end: Long): Int

    @Query(
        """
UPDATE appointments
SET clientId=:clientId, serviceId=:serviceId, startAt=:startAt, endAt=:endAt, notes=:notes
WHERE id=:id
"""
    )
    suspend fun update(
        id: Long,
        clientId: Long,
        serviceId: Long,
        startAt: Long,
        endAt: Long,
        notes: String?
    )

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM appointments WHERE startAt BETWEEN :start AND :end ORDER BY startAt ASC")
    fun getBetween(start: Long, end: Long): Flow<List<Appointment>>

    @Insert
    suspend fun insert(a: Appointment)

    // ✔️ Nueva sobrecarga para obtener el ID (útil para programar recordatorios)
    @Insert
    suspend fun insertReturnId(a: Appointment): Long

    @Query("UPDATE appointments SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: String)

    @Query("SELECT COUNT(*) FROM appointments WHERE serviceId = :serviceId")
    suspend fun countByService(serviceId: Long): Int

    @Query("SELECT COUNT(*) FROM appointments WHERE clientId = :clientId")
    suspend fun countByClient(clientId: Long): Int

    @Query("""
    SELECT a.*,
           c.name  AS clientName,
           c.phone AS clientPhone,
           s.name  AS serviceName,
           s.price AS servicePrice
    FROM appointments a
    JOIN clients  c ON c.id = a.clientId
    JOIN services s ON s.id = a.serviceId
    WHERE a.startAt BETWEEN :start AND :end
    ORDER BY a.startAt ASC
""")
    fun getBetweenUi(start: Long, end: Long): Flow<List<AppointmentUi>>

    @Query("""
    SELECT strftime('%Y-%m-%d', datetime(a.startAt/1000,'unixepoch','localtime')) AS day,
           COUNT(*) AS count,
           COALESCE(SUM(s.price), 0) AS total
    FROM appointments a
    LEFT JOIN services s ON s.id = a.serviceId
    WHERE a.status = 'done' AND a.startAt BETWEEN :start AND :end
    GROUP BY day
    ORDER BY day
""")
    fun statsByDay(start: Long, end: Long): Flow<List<DayStat>>

    @Query("""
    SELECT COALESCE(s.name, '(Eliminado)') AS serviceName,
           COUNT(*) AS count,
           COALESCE(SUM(s.price), 0) AS total
    FROM appointments a
    LEFT JOIN services s ON s.id = a.serviceId
    WHERE a.status = 'done' AND a.startAt BETWEEN :start AND :end
    GROUP BY a.serviceId
    ORDER BY total DESC
""")
    fun statsByService(start: Long, end: Long): Flow<List<ServiceStat>>


    // ----------- NUEVOS PARA CALENDARIO -----------

    /**
     * Devuelve conteo de turnos por día entre [start] y [end] (milisegundos epoch).
     * Si [status] es null, cuenta TODOS los estados. Si no, filtra por ese estado.
     * Útil para pintar los “badges” en el calendario.
     */
    @Query("""
        SELECT strftime('%Y-%m-%d', datetime(a.startAt/1000,'unixepoch','localtime')) AS day,
               COUNT(*) AS count
        FROM appointments a
        WHERE a.startAt BETWEEN :start AND :end
          AND (:status IS NULL OR a.status = :status)
        GROUP BY day
        ORDER BY day
    """)
    fun dayCountsBetween(
        start: Long,
        end: Long,
        status: String? = null
    ): Flow<List<DayCount>>  // <-- DTO nuevo (te lo paso en el siguiente paso)

    /**
     * Lista UI de turnos de un día (ej: 00:00–23:59 del día seleccionado).
     * Ideal para la lista inferior del calendario.
     */
    @Query("""
    SELECT a.*,
           c.name  AS clientName,
           c.phone AS clientPhone,
           s.name  AS serviceName,
           s.price AS servicePrice
    FROM appointments a
    JOIN clients  c ON c.id = a.clientId
    LEFT JOIN services s ON s.id = a.serviceId
    WHERE a.startAt BETWEEN :startOfDay AND :endOfDay
    ORDER BY a.startAt ASC
    """)
    fun getDayUi(
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<AppointmentUi>>

    /**
     * Próximos turnos desde "ahora" (para notificaciones o pantalla "Próximos").
     */
    @Query("""
        SELECT * FROM appointments
        WHERE startAt >= :now
        ORDER BY startAt ASC
    """)
    fun getUpcoming(now: Long): Flow<List<Appointment>>

    /**
     * Obtener un turno puntual por id (útil para reprogramar notificación luego de editar).
     */
    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Appointment?
}
