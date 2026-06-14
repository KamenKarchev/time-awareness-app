package com.dopey.timeawarenessapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// ── Base palette ───────────────────────────────────────────────────────────────
val TerminalBackground = Color(0xFF1C1C1E)  // medium-dark gray
val TerminalGreen      = Color(0xFF39FF14)
val TerminalAmber      = Color(0xFFFFB300)
val TerminalRed        = Color(0xFFFF3D00)
val TerminalDeepRed    = Color(0xFF8B0000)
val TerminalGray       = Color(0xFF888888)  // brighter so it reads on new bg
val TerminalWhite      = Color(0xFFFFFFFF)
val TerminalMidGray    = Color(0xFF555555)  // dim track

// ── Accuracy → colour (0 = deep red, 100 = neon green) ─────────────────
fun accuracyColor(accuracy: Int): Color {
    val f = (accuracy / 100f).coerceIn(0f, 1f)
    return when {
        f >= 0.5f -> lerp(TerminalAmber, TerminalGreen,   (f - 0.5f) * 2f)
        else      -> lerp(TerminalDeepRed, TerminalAmber, f * 2f)
    }
}

// Legacy aliases
val Purple80      = Color(0xFFD0BCFF)
val PurpleGrey80  = Color(0xFFCCC2DC)
val Pink80        = Color(0xFFEFB8C8)
val Purple40      = Color(0xFF6650a4)
val PurpleGrey40  = Color(0xFF625b71)
val Pink40        = Color(0xFF7D5260)
