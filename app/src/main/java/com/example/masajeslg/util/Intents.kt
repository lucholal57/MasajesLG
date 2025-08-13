package com.example.masajeslg.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

fun openWhatsApp(context: Context, phoneRaw: String, message: String) {
    val phone = phoneRaw.filter { it.isDigit() || it == '+' }
    val encoded = URLEncoder.encode(message, "UTF-8")
    val uri = Uri.parse("https://wa.me/$phone?text=$encoded")
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
