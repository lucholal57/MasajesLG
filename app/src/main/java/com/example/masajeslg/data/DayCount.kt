package com.example.masajeslg.data

/**
 * Resultado de conteo por día para el calendario.
 * `day` viene en formato "YYYY-MM-DD" (zona local).
 */
data class DayCount(
    val day: String,
    val count: Int
)
