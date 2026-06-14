package com.dopey.timeawarenessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopey.timeawarenessapp.ui.theme.*
import java.time.LocalDateTime

/**
 * DEBUG PANEL – shown at the bottom of the screen on the debug/manual-time branch.
 *
 * Lets the developer pick any HH:MM time and inject it as the "current time".
 * A yellow banner is shown whenever an override is active.
 */
@Composable
fun DebugTimePanel(
    debugTime: LocalDateTime?,
    onSetTime: (LocalDateTime?) -> Unit
) {
    var hourInput   by remember { mutableStateOf(debugTime?.hour?.toString()   ?: "") }
    var minuteInput by remember { mutableStateOf(debugTime?.minute?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A00))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Banner when override active
        if (debugTime != null) {
            Text(
                text = "⚠ DEBUG TIME ACTIVE: %02d:%02d".format(debugTime.hour, debugTime.minute),
                color = TerminalAmber,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "DEBUG TIME",
                color = TerminalAmber,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            // Hour field
            OutlinedTextField(
                value = hourInput,
                onValueChange = { if (it.length <= 2) hourInput = it.filter(Char::isDigit) },
                label = { Text("HH", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.width(64.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TerminalAmber,
                    unfocusedBorderColor = TerminalAmber.copy(alpha = 0.5f),
                    focusedTextColor     = TerminalAmber,
                    unfocusedTextColor   = TerminalAmber,
                    cursorColor          = TerminalAmber,
                    focusedLabelColor    = TerminalAmber,
                    unfocusedLabelColor  = TerminalAmber.copy(alpha = 0.5f)
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            )

            Text(":", color = TerminalAmber, fontSize = 20.sp, fontFamily = FontFamily.Monospace)

            // Minute field
            OutlinedTextField(
                value = minuteInput,
                onValueChange = { if (it.length <= 2) minuteInput = it.filter(Char::isDigit) },
                label = { Text("MM", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.width(64.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TerminalAmber,
                    unfocusedBorderColor = TerminalAmber.copy(alpha = 0.5f),
                    focusedTextColor     = TerminalAmber,
                    unfocusedTextColor   = TerminalAmber,
                    cursorColor          = TerminalAmber,
                    focusedLabelColor    = TerminalAmber,
                    unfocusedLabelColor  = TerminalAmber.copy(alpha = 0.5f)
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            )

            // SET button
            Button(
                onClick = {
                    val h = hourInput.toIntOrNull()?.coerceIn(0, 23)
                    val m = minuteInput.toIntOrNull()?.coerceIn(0, 59)
                    if (h != null && m != null) {
                        val base = LocalDateTime.now()
                        onSetTime(base.withHour(h).withMinute(m).withSecond(0))
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerminalAmber,
                    contentColor   = Color(0xFF0A0A0A)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("SET", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            // CLEAR button
            OutlinedButton(
                onClick = {
                    hourInput = ""; minuteInput = ""
                    onSetTime(null)
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalAmber),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("CLEAR", fontFamily = FontFamily.Monospace)
            }
        }
    }
}
