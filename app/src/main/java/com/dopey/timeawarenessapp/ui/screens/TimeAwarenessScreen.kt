package com.dopey.timeawarenessapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopey.timeawarenessapp.domain.TargetStatus
import com.dopey.timeawarenessapp.intent.TimeIntent
import com.dopey.timeawarenessapp.model.AppUiState
import com.dopey.timeawarenessapp.domain.TargetResolver
import com.dopey.timeawarenessapp.ui.theme.*
import com.dopey.timeawarenessapp.viewmodel.TimeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

private val DateFmt = DateTimeFormatter.ofPattern("EEE dd MMM")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeAwarenessScreen(viewModel: TimeViewModel) {
    val state by viewModel.state.collectAsState()

    // Drive the Tick intent every second
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.processIntent(TimeIntent.Tick)
            delay(1000)
        }
    }

    TimeAwarenessContent(state = state, onIntent = viewModel::processIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeAwarenessContent(state: AppUiState, onIntent: (TimeIntent) -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Keep drawer in sync with state
    LaunchedEffect(state.isMenuOpen) {
        if (state.isMenuOpen) drawerState.open() else drawerState.close()
    }

    val day = state.day
    val now = state.now
    val nextTarget = TargetResolver.nextPendingTarget(day.targets, now)

    // Dialogs
    if (state.showEarlyDialog && nextTarget != null) {
        EarlyClockInDialog(
            nextTargetHour = nextTarget.hour,
            onConfirm = { onIntent(TimeIntent.ConfirmEarlyClockIn) },
            onDismiss = { onIntent(TimeIntent.DismissEarlyDialog) }
        )
    }
    if (state.showAllDoneDialog) {
        AllDoneDialog(onDismiss = { onIntent(TimeIntent.DismissAllDoneDialog) })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RangeDrawer(
                startHour = day.startHour,
                endHour   = day.endHour,
                isExporting = state.isExporting,
                hasLogs = day.rawEvents.isNotEmpty(),
                onApply = { s, e -> onIntent(TimeIntent.SetTimeRange(s, e)) },
                onExport = { onIntent(TimeIntent.ExportData) },
                onClose  = { onIntent(TimeIntent.CloseMenu) }
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            containerColor = TerminalBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "TIME AWARENESS",
                                color = TerminalGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 3.sp
                            )
                            Text(
                                now.format(DateFmt),
                                color = TerminalGray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onIntent(TimeIntent.OpenMenu) }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TerminalGreen)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0D0D))
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(TerminalBackground)
            ) {
                // ── Timeline pill canvas fills the whole content area ──
                TimelinePillCanvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    targets      = day.targets,
                    markers      = day.markers,
                    now          = now,
                    startHour    = day.startHour,
                    endHour      = day.endHour,
                    dayScore     = day.score,
                    perfectHitHour = state.perfectHitHour
                )

                // ── Two orbs centered inside the pill ─────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Upper orb: score
                    ScoreOrb(score = day.score)

                    // Lower orb: clock-in button
                    ClockInOrb(
                        now = now,
                        nextTarget = nextTarget,
                        onClockIn = { onIntent(TimeIntent.ClockIn) }
                    )
                }
            }
        }
    }
}

// ── Side drawer ────────────────────────────────────────────────────────────────
@Composable
fun RangeDrawer(
    startHour: Int,
    endHour: Int,
    isExporting: Boolean,
    hasLogs: Boolean,
    onApply: (Int, Int) -> Unit,
    onExport: () -> Unit,
    onClose: () -> Unit
) {
    var draftStart by remember(startHour) { mutableIntStateOf(startHour) }
    var draftEnd   by remember(endHour)   { mutableIntStateOf(endHour) }

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0D0D0D),
        drawerContentColor   = TerminalGreen
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "SETTINGS",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            fontSize = 13.sp,
            color = TerminalGreen
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp, horizontal = 20.dp), color = TerminalGreen.copy(0.3f))

        DrawerLabel("START HOUR")
        Spacer(Modifier.height(6.dp))
        HourStepper(draftStart, 0..22) { draftStart = it }

        Spacer(Modifier.height(20.dp))
        DrawerLabel("END HOUR")
        Spacer(Modifier.height(6.dp))
        HourStepper(draftEnd, 1..23) { draftEnd = it }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onApply(draftStart, draftEnd) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen, contentColor = Color(0xFF0D0D0D)),
            shape = RoundedCornerShape(4.dp)
        ) { Text("APPLY RANGE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onExport,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(48.dp),
            enabled = !isExporting && hasLogs,
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalGreen)
        ) {
            Text(if (isExporting) "EXPORTING..." else "EXPORT XML", fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable private fun DrawerLabel(text: String) =
    Text(text, Modifier.padding(horizontal = 20.dp), FontFamily.Monospace, 11.sp, color = TerminalGray)

@Composable
fun HourStepper(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalGreen),
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(0.dp)
        ) { Text("-", fontFamily = FontFamily.Monospace, fontSize = 18.sp) }

        Text("%02d:00".format(value), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TerminalGreen)

        OutlinedButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalGreen),
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(0.dp)
        ) { Text("+", fontFamily = FontFamily.Monospace, fontSize = 18.sp) }
    }
}
