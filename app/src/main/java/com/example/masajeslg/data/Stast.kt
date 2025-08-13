package com.example.masajeslg.data

import androidx.room.ColumnInfo

data class DayStat(
    @ColumnInfo(name = "day") val day: String,         // "2025-08-12"
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "total") val total: Double
)

data class ServiceStat(
    @ColumnInfo(name = "serviceName") val serviceName: String,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "total") val total: Double
)
