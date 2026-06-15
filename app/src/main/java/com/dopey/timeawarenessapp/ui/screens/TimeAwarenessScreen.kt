package com.dopey.timeawarenessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopey.timeawarenessapp.domain.TargetResolver
import com.dopey.timeawarenessapp.intent.TimeIntent
import com.dopey.timeawarenessapp.model.AppUiState
import com.dopey.timeawarenessapp.ui.theme.*
import com.dopey.timeawarenessapp.viewmodel.TimeViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val DateFmt = DateTimeFormatter.ofPattern("EEE dd MMM")

@Composable
fun TimeAwarenessScreen(viewModel: TimeViewModel) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        while (true) { viewModel.processIntent(TimeIntent.Tick); delay(1000) }
    }
    TimeAwarenessContent(state = state, onIntent = viewModel::processIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeAwarenessContent(state: AppUiState, onIntent: (TimeIntent) -> Unit) {
    // Pages: 0=Settings, 1=Main, 2=History
    val pagerState = rememberPagerState(initialPage = 1) { 3 }

    val day        = state.day
    val now        = state.now
    val nextTarget = TargetResolver.nextPendingTarget(day.targets, now)

    // Confetti
    var confettiTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(state.perfectHitHour) {
        if (state.perfectHitHour != null) confettiTrigger = !confettiTrigger
    }

    if (state.showEarlyDialog && nextTarget != null) {
        EarlyClockInDialog(
            nextTargetHour = nextTarget.hour,
            onConfirm      = { onIntent(TimeIntent.ConfirmEarlyClockIn) },
            onDismiss      = { onIntent(TimeIntent.DismissEarlyDialog) }
        )
    }
    if (state.showAllDoneDialog) {
        AllDoneDialog(onDismiss = { onIntent(TimeIntent.DismissAllDoneDialog) })
    }

    Scaffold(
        containerColor = TerminalBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "TIME AWARENESS",
                            color = TerminalGreen, fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 3.sp
                        )
                        Text(
                            now.format(DateFmt),
                            color = TerminalGray, fontFamily = FontFamily.Monospace, fontSize = 10.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0D0D))
            )
        }
    ) { padding ->
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            when (page) {
                0 -> SettingsScreen(state = state, onIntent = onIntent)
                1 -> MainPage(
                    state           = state,
                    onIntent        = onIntent,
                    nextTarget      = nextTarget,
                    confettiTrigger = confettiTrigger
                )
                2 -> HistoryScreen(state = state, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun MainPage(
    state: AppUiState,
    onIntent: (TimeIntent) -> Unit,
    nextTarget: com.dopey.timeawarenessapp.domain.TargetTick?,
    confettiTrigger: Boolean
) {
    val day = state.day
    val now = state.now

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            TimelinePillCanvas(
                modifier       = Modifier.fillMaxSize().padding(24.dp),
                targets        = day.targets,
                markers        = day.markers,
                now            = now,
                startHour      = day.startHour,
                endHour        = day.endHour,
                dayScore       = day.score,
                perfectHitHour = state.perfectHitHour
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreOrb(score = day.score)
                ClockInOrb(
                    now        = now,
                    nextTarget = nextTarget,
                    onClockIn  = { onIntent(TimeIntent.ClockIn) }
                )
            }
            ConfettiOverlay(
                trigger  = confettiTrigger,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Debug row — only visible when debug mode is on
        if (state.debugEnabled) {
            var debugTime by remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111100)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    modifier = Modifier.weight(1f).height(52.dp),
                    value    = debugTime,
                    onValueChange = { newText ->
                        debugTime = newText
                        try {
                            val parsedTime = LocalTime.parse(newText, DateTimeFormatter.ISO_LOCAL_TIME)
                            val dateTime   = LocalDateTime.of(LocalDate.now(), parsedTime)
                            onIntent(TimeIntent.DebugSetTime(dateTime))
                        } catch (e: DateTimeParseException) { }
                    },
                    placeholder = { Text("HH:MM:SS", fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp, color = TerminalGray) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color(0xFF111100),
                        unfocusedContainerColor = Color(0xFF111100),
                        focusedTextColor        = TerminalAmber,
                        unfocusedTextColor      = TerminalAmber,
                        cursorColor             = TerminalAmber,
                        focusedIndicatorColor   = TerminalAmber,
                        unfocusedIndicatorColor = TerminalAmber.copy(alpha = 0.4f)
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace, fontSize = 14.sp
                    ),
                    label = { Text(
                        if (state.debugTime != null) "⚠ ${state.debugTime.toLocalTime()}" else "DBG TIME",
                        fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TerminalAmber
                    )}
                )
                TextButton(
                    onClick  = { onIntent(TimeIntent.DebugResetDay) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("RESET", fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp, color = TerminalAmber, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
