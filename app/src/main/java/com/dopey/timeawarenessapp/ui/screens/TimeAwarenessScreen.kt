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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
imx  import java.time.LocalTime
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

    // Live clock state
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
                // Score
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

                // Track oval
                TrackOval(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    nodes = state.hourNodes,
                    logs = state.logs
                )

                // Mark button
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

    // Pulse animation for the last logged node
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

        // Draw the stadium oval (rounded rect)
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

        // Distribute nodes around the perimeter of the stadium oval
        // Perimeter: two straight segments + two semicircles
        val r = cornerRadius
        val straightH = (h - 2 * r).coerceAtLeast(0f)
        val semiPerimeter = Math.PI.toFloat() * r
        val totalPerimeter = 2 * straightH + 2 * semiPerimeter
        val spacing = totalPerimeter / nodeCount

        fun perimeterToOffset(dist: Float): Offset {
            // Segments in order: top-right arc (0..semiPerimeter), right straight, bottom arc, left straight
            var d = ((dist % totalPerimeter) + totalPerimeter) % totalPerimeter

            // Top arc: center=(w-r, r), angles from -90 to +90 ... actually let's go clockwise from top-center
            // top-center → right side → bottom-center → left side
            // Segment 1: top semicircle (right half) = top-center clockwise to bottom-right: but stadium has straight sides
            // Let's define 4 segments clockwise starting at top-center:
            // S1: top arc (right semicircle, 180deg) from top-center going right: cx=w-r, cy=r, angles -90..90
            // S2: right straight from (w, r) to (w, h-r)
            // S3: bottom arc (left semicircle) cx=r, cy=h-r, angles 0..180
            // S4: left straight from (0, h-r) to (0, r)
            // Wait - stadium oval top arc spans the full top: cx=w/2... no.
            // Correct stadium: left cap center=(r, r+straightH/2)? No.
            // A stadium = rectangle with semicircles on top and bottom.
            // Width=w, Height=h. Caps on top and bottom.
            // Top cap: center=(w/2, r), semicircle facing up, angles 180..360 (or π..2π)
            // Bottom cap: center=(w/2, h-r), semicircle facing down, angles 0..π
            // Left straight: from (0, r) down to (0, h-r)
            // Right straight: from (w, r) down to (w, h-r)
            // Clockwise from top-center (w/2, 0):
            // Arc1: top cap right half: (w/2,0) → (w, r), angle -90..0 on center (w/2, r): arc len = semiPerimeter/2
            // Straight1: right side (w, r) → (w, h-r): len = straightH
            // Arc2: bottom cap: (w, h-r) → (0, h-r), angle 0..180 on center (w/2, h-r): arc len = semiPerimeter
            // Straight2: left side (0, h-r) → (0, r): len = straightH
            // Arc3: top cap left half: (0, r) → (w/2, 0), angle 180..270 on center (w/2, r): arc len = semiPerimeter/2
            // Total = semiPerimeter/2 + straightH + semiPerimeter + straightH + semiPerimeter/2 = 2*semiPerimeter + 2*straightH ✓

            val topArcRight = semiPerimeter / 2f
            val rightStraight = straightH
            val bottomArc = semiPerimeter
            val leftStraight = straightH
            // leftArcTop = semiPerimeter/2 (remainder)

            val cx = w / 2f

            return when {
                d < topArcRight -> {
                    val angle = (-PI / 2 + (d / topArcRight) * (PI / 2)).toFloat()
                    Offset(cx + r * cos(angle), r + r * sin(angle))
                }
                d < topArcRight + rightStraight -> {
                    val t = (d - topArcRight) / rightStraight
                    Offset(w, r + t * straightH)
                }
                d < topArcRight + rightStraight + bottomArc -> {
                    val t = (d - topArcRight - rightStraight) / bottomArc
                    val angle = (t * PI).toFloat()
                    Offset(cx + r * cos(angle), (h - r) + r * sin(angle))
                }
                d < topArcRight + rightStraight + bottomArc + leftStraight -> {
                    val t = (d - topArcRight - rightStraight - bottomArc) / leftStraight
                    Offset(0f, (h - r) - t * straightH)
                }
                else -> {
                    val t = (d - topArcRight - rightStraight - bottomArc - leftStraight) / (semiPerimeter / 2f)
                    val angle = (PI + t * (PI / 2)).toFloat()
                    Offset(cx + r * cos(angle), r + r * sin(angle))
                }
            }
        }

        // Draw nodes
        nodes.forEachIndexed { index, hour ->
            val dist = index * spacing + spacing / 2f // offset so first node isn't at top-center seam
            val pos = perimeterToOffset(dist)
            val isLogged = hour in loggedHours
            val isLastLogged = logs.lastOrNull()?.expectedHour == hour

            // Outer glow for logged nodes
            if (isLogged) {
                val glowAlpha = if (isLastLogged) pulseAlpha * 0.4f else 0.25f
                drawCircle(
                    color = TerminalGreen,
                    radius = nodeRadius * 2.2f,
                    center = pos,
                    alpha = glowAlpha
                )
            }

            // Node fill
            drawCircle(
                color = if (isLogged) TerminalGreen else TerminalBackground,
                radius = nodeRadius,
                center = pos
            )
            // Node border
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

        // Start hour
        Text(
            "START HOUR",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TerminalGray
        )
        Spacer(Modifier.height(6.dp))
        HourStepper(
            value = startHour,
            range = 0..22,
            onValueChange = { startHour = it }
        )

        Spacer(Modifier.height(20.dp))

        // End hour
        Text(
            "END HOUR",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TerminalGray
        )
        Spacer(Modifier.height(6.dp))
        HourStepper(
            value = endHour,
            range = 1..23,
            onValueChange = { endHour = it }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onIntent(TimeIntent.SetTimeRange(startHour, endHour))
                onClose()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
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
