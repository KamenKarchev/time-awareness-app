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
import com.dopey.timeawarenessapp.intent.TimeIntent
import com.dopey.timeawarenessapp.model.AppUiState
import com.dopey.timeawarenessapp.ui.theme.*

@Composable
fun SettingsScreen(state: AppUiState, onIntent: (TimeIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Text(
            "SETTINGS",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            fontSize = 13.sp,
            color = TerminalGreen
        )
        HorizontalDivider(color = TerminalGreen.copy(alpha = 0.3f))

        // ── Time range ────────────────────────────────────────────────
        var draftStart by remember(state.day.startHour) { mutableIntStateOf(state.day.startHour) }
        var draftEnd   by remember(state.day.endHour)   { mutableIntStateOf(state.day.endHour) }

        SettingsLabel("START HOUR")
        HourStepper(draftStart, 0..22) { draftStart = it }
        SettingsLabel("END HOUR")
        HourStepper(draftEnd, 1..23) { draftEnd = it }

        Button(
            onClick = { onIntent(TimeIntent.SetTimeRange(draftStart, draftEnd)) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = TerminalGreen, contentColor = Color(0xFF0D0D0D)),
            shape    = RoundedCornerShape(4.dp)
        ) {
            Text("APPLY RANGE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(color = TerminalGreen.copy(alpha = 0.3f))

        // ── Export ───────────────────────────────────────────────────
        OutlinedButton(
            onClick  = { onIntent(TimeIntent.ExportData) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled  = !state.isExporting && state.day.rawEvents.isNotEmpty(),
            shape    = RoundedCornerShape(4.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = TerminalGreen)
        ) {
            Text(
                if (state.isExporting) "EXPORTING..." else "EXPORT XML",
                fontFamily = FontFamily.Monospace
            )
        }

        HorizontalDivider(color = TerminalGreen.copy(alpha = 0.3f))

        // ── Debug toggle ─────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "DEBUG MODE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TerminalAmber
                )
                Text(
                    "Enables time override & day reset",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TerminalGray
                )
            }
            Switch(
                checked = state.debugEnabled,
                onCheckedChange = { onIntent(TimeIntent.ToggleDebug) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor   = TerminalAmber,
                    checkedTrackColor   = TerminalAmber.copy(alpha = 0.4f),
                    uncheckedThumbColor = TerminalGray,
                    uncheckedTrackColor = TerminalGray.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun SettingsLabel(text: String) =
    Text(
        text,
        fontFamily = FontFamily.Monospace,
        fontSize   = 11.sp,
        color      = TerminalGray
    )


    @Composable
private fun HourStepper(value: Int, range: IntRange, onValue: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = { if (value > range.first) onValue(value - 1) }) {
            Text("-", fontFamily = FontFamily.Monospace, fontSize = 20.sp, color = TerminalGreen)
        }
        Text(
            "%02d:00".format(value),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TerminalGreen
        )
        IconButton(onClick = { if (value < range.last) onValue(value + 1) }) {
            Text("+", fontFamily = FontFamily.Monospace, fontSize = 20.sp, color = TerminalGreen)
        }
    }
}
