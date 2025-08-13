package com.example.masajeslg.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
fun parseSqlDayToLocalDate(day: String): LocalDate = LocalDate.parse(day) // "YYYY-MM-DD"
