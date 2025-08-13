package com.example.masajeslg.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

fun openWhatsApp(ctx: Context, phoneRaw: String, msg: String) {
    val phone = phoneRaw.filter { it.isDigit() || it == '+' }
    val url = "https://wa.me/$phone?text=${URLEncoder.encode(msg, Charsets.UTF_8.name())}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try { ctx.startActivity(intent) } catch (_: ActivityNotFoundException) { }
}
