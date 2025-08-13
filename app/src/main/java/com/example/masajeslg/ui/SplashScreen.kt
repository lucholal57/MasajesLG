package com.example.masajeslg.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Animación del gradiente
    val infinite = rememberInfiniteTransition(label = "bg")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    var size by remember { mutableStateOf(IntSize.Zero) }
    val c1 = Color(0xFF6D28D9) // violeta
    val c2 = Color(0xFF10B981) // verde agua
    val c3 = Color(0xFFFFB4A2) // peach

    val start = Offset(size.width * phase, 0f)
    val end   = Offset(0f, size.height * (1f - phase))
    val brush = Brush.linearGradient(listOf(c1, c2, c3), start = start, end = end)

    // Animación de contenido
    val appear by animateFloatAsState(
        targetValue = 1f, animationSpec = tween(900, easing = FastOutSlowInEasing), label = "appear"
    )
    val scale by animateFloatAsState(
        targetValue = 1f, animationSpec = tween(900, easing = FastOutSlowInEasing), label = "scale"
    )

    // Timer para navegar luego de ~1.6s
    LaunchedEffect(Unit) {
        delay(1600)
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .background(brush)
            .padding(28.dp)
    ) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Masajes LG",
                color = Color.White.copy(alpha = appear),
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "bienestar y equilibrio",
                color = Color.White.copy(alpha = appear * 0.9f),
                fontSize = 16.sp
            )
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.85f),
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
