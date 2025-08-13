package com.example.masajeslg.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class Service(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durationMinutes: Int,
    val price: Double,          // en $ (simple por ahora)
    val active: Boolean = true
)
