package com.example.masajeslg.work

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.masajeslg.data.AppDatabase
import com.example.masajeslg.util.Notifications
import java.time.Instant
import java.time.ZoneId

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong(KEY_APPOINTMENT_ID, -1L)
        if (id <= 0) return Result.success()

        val db = AppDatabase.get(applicationContext)
        val ap = db.appointmentDao().getById(id) ?: return Result.success()
        val client = db.clientDao().getById(ap.clientId)
        val service = db.serviceDao().getById(ap.serviceId)

        val dt = Instant.ofEpochMilli(ap.startAt).atZone(ZoneId.systemDefault())
        val hora = "%02d:%02d".format(dt.hour, dt.minute)
        val fecha = "%02d/%02d/%04d".format(dt.dayOfMonth, dt.monthValue, dt.year)

        val title = "Recordatorio de turno"
        val text = buildString {
            append("Turno ")
            if (service != null) append("de ${service.name} ")
            if (client != null) append("con ${client.name} ")
            append("hoy $fecha a las $hora.")
        }

        Notifications.showAppointmentReminder(
            applicationContext,
            notifId = id.toInt(),
            title = title,
            text = text
        )
        return Result.success()
    }

    companion object {
        const val KEY_APPOINTMENT_ID = "appointment_id"
        fun input(id: Long) = workDataOf(KEY_APPOINTMENT_ID to id)
    }
}
