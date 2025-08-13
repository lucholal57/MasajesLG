package com.example.masajeslg.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val serviceId: Long,
    val startAt: Long, // millis epoch
    val endAt: Long,   // calculado = startAt + duración servicio
    val status: String = "pending", // pending | done | canceled
    val notes: String? = null
)
