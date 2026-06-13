package com.dopey.timeawarenessapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopey.timeawarenessapp.intent.TimeIntent
import com.dopey.timeawarenessapp.model.TimeLog
import com.dopey.timeawarenessapp.model.TimeState
import com.dopey.timeawarenessapp.ui.theme.*
import com.dopey.timeawarenessapp.viewmodel.TimeViewModel
import java.time.format.DateTimeFormatter

private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

@Composable
fun TimeAwarenessScreen(viewModel: TimeViewModel) {
    val state by viewModel.state.collectAsState()
    TimeAwarenessContent(state = state, onIntent = viewModel::processIntent)
}

@Composable
fun TimeAwarenessContent(
    state: TimeState,
    onIntent: (TimeIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground)
            .padding(horizontal = 20.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TIME AWARENESS",
            color = TerminalGreen,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        ScoreDisplay(score = state.score)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "points remaining",
            color = TerminalGray,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { onIntent(TimeIntent.LogCurrentTime) },
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TerminalGreen,
                contentColor = TerminalBackground
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = "[ MARK HOUR ]",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (state.logs.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("EXPECTED", color = TerminalGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text("ACTUAL", color = TerminalGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text("DELTA", color = TerminalGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
            HorizontalDivider(color = TerminalGreen.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(4.dp))
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.logs.reversed()) { log ->
                LogItemRow(log)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { onIntent(TimeIntent.ExportData) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !state.isExporting && state.logs.isNotEmpty(),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalGreen)
        ) {
            Text(
                text = if (state.isExporting) "EXPORTING..." else "EXPORT XML",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ScoreDisplay(score: Long) {
    val scoreColor by animateColorAsState(
        targetValue = when {
            score >= 80 -> TerminalGreen
            score >= 50 -> TerminalAmber
            else -> TerminalRed
        },
        animationSpec = tween(600),
        label = "scoreColor"
    )

    Text(
        text = score.toString().padStart(3, '0'),
        color = scoreColor,
        fontSize = 72.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
fun LogItemRow(log: TimeLog) {
    val deviationColor = when {
        log.deviationMinutes == 0L -> TerminalGreen
        log.deviationMinutes <= 5 -> TerminalAmber
        else -> TerminalRed
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(TerminalBackground)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = log.expectedTime.format(TimeFormatter),
            color = TerminalGray,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        )
        Text(
            text = log.actualTime.format(TimeFormatter),
            color = TerminalGray,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        )
        Text(
            text = "-${log.deviationMinutes}m",
            color = deviationColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
