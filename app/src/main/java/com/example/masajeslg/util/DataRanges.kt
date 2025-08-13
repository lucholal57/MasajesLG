package com.example.masajeslg.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.*

/** Inicio del día (00:00:00.000) en millis epoch zona local */
@RequiresApi(Build.VERSION_CODES.O)
fun startOfDayMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

/** Fin del día (23:59:59.999) en millis epoch zona local */
@RequiresApi(Build.VERSION_CODES.O)
fun endOfDayMillis(date: LocalDate): Long =
    date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

/** Rango [inicio, fin] del mes de `yearMonth` en millis (ambos inclusive) */
@RequiresApi(Build.VERSION_CODES.O)
fun monthRangeMillis(yearMonth: YearMonth): Pair<Long, Long> {
    val start = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return start to end
}

/** Convierte "YYYY-MM-DD" (sqlite strftime) a LocalDate en zona local */
@RequiresApi(Build.VERSION_CODES.O)
fun parseSqlDayToLocalDate(day: String): LocalDate = LocalDate.parse(day)
