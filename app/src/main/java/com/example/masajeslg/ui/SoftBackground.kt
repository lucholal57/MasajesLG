package com.example.masajeslg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun SoftBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val gradient = remember {
        Brush.verticalGradient(
            listOf(
                Color(0xFFFDFBFB),
                Color(0xFFEDEDED),
                Color(0xFFE0E0E0)
            )
        )
    }
    Box(
        modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        content()
    }
}
