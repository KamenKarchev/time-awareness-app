package com.dopey.timeawarenessapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary   = TerminalGreen,
    background = TerminalBackground,
    surface   = Color(0xFF0D0D0D),
    onPrimary  = TerminalBackground,
    onBackground = TerminalGreen,
    onSurface  = TerminalGreen
)

@Composable
fun TimeAwarenessTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
