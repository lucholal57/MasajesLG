package com.example.masajeslg.util


import java.text.NumberFormat
import java.util.Locale
    fun Double.asCurrency(locale: Locale = Locale("es", "AR")): String =
        NumberFormat.getCurrencyInstance(locale).format(this)
