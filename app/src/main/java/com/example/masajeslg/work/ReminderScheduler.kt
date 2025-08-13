package com.example.masajeslg.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlin.math.max

object ReminderScheduler {

    private fun uniqueName(appointmentId: Long) = "appointment_reminder_$appointmentId"

    /**
     * Programa una notificación para [startAt - minutesBefore].
     * - Si el turno ya pasó: cancela y no programa.
     * - Si ya estás dentro de la ventana (delay <= 0): dispara ASAP (~5s).
     */
    fun schedule(
        context: Context,
        appointmentId: Long,
        startAt: Long,
        minutesBefore: Int
    ) {
        val now = System.currentTimeMillis()

        // Si el turno ya pasó, cancelar cualquier pendiente y salir
        if (startAt <= now) {
            cancel(context, appointmentId)
            return
        }

        val triggerAt = startAt - minutesBefore * 60_000L
        val delayMs = triggerAt - now

        // Si ya estás dentro de la ventana, dispará lo antes posible (5s)
        val effectiveDelayMs = if (delayMs <= 0L) 5_000L else delayMs

        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(max(1L, effectiveDelayMs), TimeUnit.MILLISECONDS)
            .setInputData(ReminderWorker.input(appointmentId))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueName(appointmentId),
            ExistingWorkPolicy.REPLACE, // si ya existe, reprograma
            req
        )
    }

    /** Cancela un recordatorio programado para el turno. */
    fun cancel(context: Context, appointmentId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(appointmentId))
    }
}
