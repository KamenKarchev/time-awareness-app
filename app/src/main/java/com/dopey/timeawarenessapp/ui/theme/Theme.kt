package com.dopey.timeawarenessapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TerminalBackground = Color(0xFF121212)
val TerminalGreen = Color(0xFF00FF00)
val TerminalAmber = Color(0xFFFFBF00)
val TerminalRed = Color(0xFFFF4444)
val TerminalGray = Color(0xFFAAAAAA)

private val TerminalColorScheme = darkColorScheme(
    primary = TerminalGreen,
    background = TerminalBackground,
    surface = Color(0xFF1E1E1E),
    onBackground = TerminalGreen,
    onSurface = TerminalGreen,
    error = TerminalRed
)

@Composable
fun TimeAwarenessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TerminalColorScheme,
        content = content
    )
}
