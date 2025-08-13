package com.example.masajeslg.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.*

@RequiresApi(Build.VERSION_CODES.O)
fun startOfDayMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

@RequiresApi(Build.VERSION_CODES.O)
fun endOfDayMillis(date: LocalDate): Long =
    date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

@RequiresApi(Build.VERSION_CODES.O)
fun monthRangeMillis(month: YearMonth): Pair<Long, Long> =
    startOfDayMillis(month.atDay(1)) to endOfDayMillis(month.atEndOfMonth())
