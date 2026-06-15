package com.dopey.timeawarenessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoryScreen(state: AppUiState, onIntent: (TimeIntent) -> Unit) {
    val month  = state.historyMonth
    val scores = state.allDayScores

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Overall average at top ───────────────────────────────────
        ScoreOrb(score = state.overallAverage)
        Spacer(Modifier.height(4.dp))
        Text(
            "ALL TIME AVERAGE",
            fontFamily  = FontFamily.Monospace,
            fontSize    = 10.sp,
            letterSpacing = 2.sp,
            color       = TerminalGray
        )

        Spacer(Modifier.height(20.dp))

        // ── Month header ───────────────────────────────────────────────
        Text(
            "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()} ${month.year}",
            fontFamily  = FontFamily.Monospace,
            fontWeight  = FontWeight.Bold,
            fontSize    = 13.sp,
            letterSpacing = 2.sp,
            color       = TerminalGreen
        )

        Spacer(Modifier.height(12.dp))

        // ── Calendar grid ──────────────────────────────────────────────
        val firstDay   = month.atDay(1)
        val daysInMonth = month.lengthOfMonth()
        // Offset so grid starts on correct weekday (Mon=0)
        val startOffset = (firstDay.dayOfWeek.value - 1) % 7
        val totalCells  = startOffset + daysInMonth

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Empty leading cells
            items(startOffset) { Box(Modifier.aspectRatio(1f)) }
            // Day cells
            items(daysInMonth) { i ->
                val date  = month.atDay(i + 1)
                val score = scores[date]
                val isToday = date == LocalDate.now()
                DayCell(day = i + 1, score = score, isToday = isToday)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Month navigation ───────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onIntent(TimeIntent.HistoryPrevMonth) }) {
                Text("◀  PREV", fontFamily = FontFamily.Monospace, color = TerminalGreen, fontSize = 13.sp)
            }
            Text(
                month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                fontFamily = FontFamily.Monospace, color = TerminalGray, fontSize = 11.sp
            )
            TextButton(onClick = { onIntent(TimeIntent.HistoryNextMonth) }) {
                Text("NEXT  ▶", fontFamily = FontFamily.Monospace, color = TerminalGreen, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, score: Int?, isToday: Boolean) {
    val fillColor = when {
        score != null -> accuracyColor(score)
        isToday       -> TerminalGray.copy(alpha = 0.3f)
        else          -> Color.Transparent
    }
    val borderColor = when {
        isToday -> TerminalGreen
        score != null -> accuracyColor(score).copy(alpha = 0.6f)
        else    -> TerminalGray.copy(alpha = 0.2f)
    }

    Box(
        modifier          = Modifier.aspectRatio(1f),
        contentAlignment  = Alignment.Center
    ) {
        // Circle background
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f * 0.82f
            drawCircle(fillColor, r)
            drawCircle(
                borderColor, r,
                style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "%02d".format(day),
                fontFamily = FontFamily.Monospace,
                fontSize   = 9.sp,
                color      = if (score != null || isToday) TerminalWhite else TerminalGray.copy(alpha = 0.4f)
            )
            if (score != null) {
                Text(
                    score.toString(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 11.sp,
                    color      = TerminalWhite
                )
            }
        }
    }
}

// Re-export accuracyColor for use in this file
private fun accuracyColor(score: Int) =
    com.dopey.timeawarenessapp.ui.theme.accuracyColor(score)
