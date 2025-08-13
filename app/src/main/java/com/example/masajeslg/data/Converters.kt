package com.example.masajeslg.data

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromDate(value: Date?): Long? = value?.time

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }
}
