package com.example.masajeslg.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BrandPrimary   = Color(0xFF6D28D9) // violeta suave
private val BrandSecondary = Color(0xFF10B981) // verde agua
private val BrandTertiary  = Color(0xFFFFB4A2) // peach

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    secondary = BrandSecondary,
    onSecondary = Color.White,
    tertiary = BrandTertiary,
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = Color(0xFF5F6368)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small      = RoundedCornerShape(14.dp),
    medium     = RoundedCornerShape(18.dp),
    large      = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MasajesLGTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        shapes = AppShapes,
        typography = Typography(), // podés personalizar después
        content = content
    )
}
