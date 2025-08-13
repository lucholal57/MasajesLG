package com.example.masajeslg.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)