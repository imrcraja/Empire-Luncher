package com.rcempire.empirelauncher.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Violet = Color(0xFF7C4DFF)
val Blue = Color(0xFF2979FF)
val Bg = Color(0xFF0B0B14)
val Surface1 = Color(0xFF151526)
val EmpireGradient = Brush.linearGradient(listOf(Violet, Blue))

private val Scheme = darkColorScheme(
    primary = Violet, secondary = Blue, background = Bg, surface = Surface1,
    onPrimary = Color.White, onBackground = Color(0xFFEAEAF5), onSurface = Color(0xFFEAEAF5)
)

@Composable
fun EmpireTheme(content: @Composable () -> Unit) {
    // Always dark: RC Empire branding is a dark violet-blue theme
    MaterialTheme(colorScheme = Scheme, content = content)
}
