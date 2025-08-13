// com/example/masajeslg/data/AppointmentUi.kt
package com.example.masajeslg.data

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class AppointmentUi(
    @Embedded val appointment: Appointment,
    @ColumnInfo(name = "clientName")  val clientName: String,
    @ColumnInfo(name = "serviceName") val serviceName: String?,   // <-- nullable
    @ColumnInfo(name = "clientPhone") val clientPhone: String?,
    @ColumnInfo(name = "servicePrice") val servicePrice: Double?   // <-- nullable
)
