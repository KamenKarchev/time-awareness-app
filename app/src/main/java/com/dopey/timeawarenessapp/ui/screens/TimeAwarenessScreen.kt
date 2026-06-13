package com.dopey.timeawarenessapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.*

private val ClockFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeAwarenessScreen(viewModel: TimeViewModel) {
    val state by viewModel.state.collectAsState()
    TimeAwarenessContent(state = state, onIntent = viewModel::processIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeAwarenessContent(
    state: TimeState,
    onIntent: (TimeIntent) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SideDrawer(
                state = state,
                onIntent = onIntent,
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            containerColor = TerminalBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "TIME AWARENESS",
                            color = TerminalGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 3.sp
                        )
                    },
                    actions = {
                        Text(
                            text = currentTime.format(ClockFormatter),
                            color = TerminalGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = TerminalGreen
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0D0D0D)
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(TerminalBackground),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ScoreDisplay(score = state.score)
                    Text(
                        "accuracy score",
                        color = TerminalGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }

                TrackOval(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    nodes = state.hourNodes,
                    logs = state.logs
                )

                Button(
                    onClick = { onIntent(TimeIntent.LogCurrentTime) },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(60.dp)
                        .padding(bottom = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerminalGreen,
                        contentColor = TerminalBackground
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "[ MARK HOUR ]",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun TrackOval(
    modifier: Modifier = Modifier,
    nodes: List<Int>,
    logs: List<TimeLog>
) {
    val loggedHours = logs.map { it.expectedHour }.toSet()
    val nodeCount = nodes.size

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 4.dp.toPx()
        val nodeRadius = 10.dp.toPx()
        val cornerRadius = w / 2f
        val r = cornerRadius
        val straightH = (h - 2 * r).coerceAtLeast(0f)
        val semiPerimeter = PI.toFloat() * r
        val totalPerimeter = 2 * straightH + 2 * semiPerimeter

        val trackPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, w, h),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            )
        }
        drawPath(
            path = trackPath,
            color = TerminalGreen.copy(alpha = 0.2f),
            style = Stroke(width = strokeWidth)
        )

        if (nodeCount == 0) return@Canvas

        val spacing = totalPerimeter / nodeCount
        val cx = w / 2f
        val topArcRight = semiPerimeter / 2f
        val bottomArc = semiPerimeter

        fun perimeterToOffset(dist: Float): Offset {
            val d = ((dist % totalPerimeter) + totalPerimeter) % totalPerimeter
            return when {
                d < topArcRight -> {
                    val angle = (-PI / 2 + (d / topArcRight) * (PI / 2)).toFloat()
                    Offset(cx + r * cos(angle), r + r * sin(angle))
                }
                d < topArcRight + straightH -> {
                    val t = (d - topArcRight) / straightH
                    Offset(w, r + t * straightH)
                }
                d < topArcRight + straightH + bottomArc -> {
                    val t = (d - topArcRight - straightH) / bottomArc
                    val angle = (t * PI).toFloat()
                    Offset(cx + r * cos(angle), (h - r) + r * sin(angle))
                }
                d < topArcRight + straightH + bottomArc + straightH -> {
                    val t = (d - topArcRight - straightH - bottomArc) / straightH
                    Offset(0f, (h - r) - t * straightH)
                }
                else -> {
                    val t = (d - topArcRight - straightH - bottomArc - straightH) / (semiPerimeter / 2f)
                    val angle = (PI + t * (PI / 2)).toFloat()
                    Offset(cx + r * cos(angle), r + r * sin(angle))
                }
            }
        }

        nodes.forEachIndexed { index, hour ->
            val pos = perimeterToOffset(index * spacing + spacing / 2f)
            val isLogged = hour in loggedHours
            val isLastLogged = logs.lastOrNull()?.expectedHour == hour

            if (isLogged) {
                drawCircle(
                    color = TerminalGreen,
                    radius = nodeRadius * 2.2f,
                    center = pos,
                    alpha = if (isLastLogged) pulseAlpha * 0.4f else 0.25f
                )
            }
            drawCircle(
                color = if (isLogged) TerminalGreen else TerminalBackground,
                radius = nodeRadius,
                center = pos
            )
            drawCircle(
                color = if (isLogged) TerminalGreen else TerminalGray,
                radius = nodeRadius,
                center = pos,
                style = Stroke(width = 2.5f)
            )
        }
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
        fontSize = 64.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
fun SideDrawer(
    state: TimeState,
    onIntent: (TimeIntent) -> Unit,
    onClose: () -> Unit
) {
    var startHour by remember(state.startHour) { mutableIntStateOf(state.startHour) }
    var endHour by remember(state.endHour) { mutableIntStateOf(state.endHour) }

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0D0D0D),
        drawerContentColor = TerminalGreen
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "SETTINGS",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            fontSize = 13.sp,
            color = TerminalGreen
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
            color = TerminalGreen.copy(alpha = 0.3f)
        )
        Text(
            "START HOUR",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TerminalGray
        )
        Spacer(Modifier.height(6.dp))
        HourStepper(value = startHour, range = 0..22, onValueChange = { startHour = it })
        Spacer(Modifier.height(20.dp))
        Text(
            "END HOUR",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TerminalGray
        )
        Spacer(Modifier.height(6.dp))
        HourStepper(value = endHour, range = 1..23, onValueChange = { endHour = it })
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                onIntent(TimeIntent.SetTimeRange(startHour, endHour))
                onClose()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TerminalGreen,
                contentColor = Color(0xFF0D0D0D)
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("APPLY RANGE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { onIntent(TimeIntent.ExportData) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(48.dp),
            enabled = !state.isExporting && state.logs.isNotEmpty(),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalGreen)
        ) {
            Text(
                if (state.isExporting) "EXPORTING..." else "EXPORT XML",
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun HourStepper(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
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
        ) {
            Text("-", fontFamily = FontFamily.Monospace, fontSize = 18.sp)
        }
        Text(
            text = "%02d:00".format(value),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TerminalGreen
        )
        OutlinedButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalGreen),
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("+", fontFamily = FontFamily.Monospace, fontSize = 18.sp)
        }
    }
}
