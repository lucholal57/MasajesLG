package com.example.masajeslg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

@Composable
fun SoftBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val c1 = Color(0xFFF7F2FF)
    val c2 = Color(0xFFEFFCF7)
    val c3 = Color(0xFFFFF3EF)
    val brush = Brush.verticalGradient(listOf(c1, c2, c3))
    Box(modifier.background(brush)) { content() }
}
