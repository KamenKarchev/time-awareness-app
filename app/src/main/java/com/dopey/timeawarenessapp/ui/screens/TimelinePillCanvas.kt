package com.dopey.timeawarenessapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dopey.timeawarenessapp.domain.*
import com.dopey.timeawarenessapp.ui.theme.*
import java.time.LocalDateTime
import kotlin.math.*

/**
 * Draws the stadium-pill timeline.
 *
 * The pill perimeter is divided into N equal segments, one per target hour.
 * Current-time position & passed-path segment are derived from [now] vs day range.
 * Each [markers] entry places a coloured dot on the path.
 * Missed targets show a triangle; perfect hits pulse white.
 */
@Composable
fun TimelinePillCanvas(
    modifier: Modifier = Modifier,
    targets: List<TargetTick>,
    markers: List<PressMarker>,
    now: LocalDateTime,
    startHour: Int,
    endHour: Int,
    dayScore: Int,
    perfectHitHour: Int?
) {
    // Pulse animation for perfect nodes
    val infiniteTransition = rememberInfiniteTransition(label = "pill")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 3.5.dp.toPx()
        val nodeR = 9.dp.toPx()
        val capR = w / 2f
        val straightH = (h - 2 * capR).coerceAtLeast(0f)
        val semiPerim = PI.toFloat() * capR
        val totalPerim = 2 * straightH + 2 * semiPerim
        val cx = w / 2f

        // ── Helper: perimeter distance → canvas Offset ────────────────────
        fun perimToOffset(raw: Float): Offset {
            val d = ((raw % totalPerim) + totalPerim) % totalPerim
            val seg1 = semiPerim / 2f          // top arc right half
            val seg2 = seg1 + straightH        // right straight
            val seg3 = seg2 + semiPerim        // bottom arc
            val seg4 = seg3 + straightH        // left straight
            return when {
                d < seg1 -> {
                    val a = (-PI / 2 + (d / seg1) * (PI / 2)).toFloat()
                    Offset(cx + capR * cos(a), capR + capR * sin(a))
                }
                d < seg2 -> {
                    val t = (d - seg1) / straightH
                    Offset(w, capR + t * straightH)
                }
                d < seg3 -> {
                    val t = (d - seg2) / semiPerim
                    val a = (t * PI).toFloat()
                    Offset(cx + capR * cos(a), (h - capR) + capR * sin(a))
                }
                d < seg4 -> {
                    val t = (d - seg3) / straightH
                    Offset(0f, (h - capR) - t * straightH)
                }
                else -> {
                    val t = (d - seg4) / (semiPerim / 2f)
                    val a = (PI + t * (PI / 2)).toFloat()
                    Offset(cx + capR * cos(a), capR + capR * sin(a))
                }
            }
        }

        // ── Full dim track ────────────────────────────────────────────────
        val trackPath = Path().apply {
            addRoundRect(RoundRect(Rect(0f, 0f, w, h), CornerRadius(capR)))
        }
        drawPath(trackPath, TerminalGray.copy(alpha = 0.3f), style = Stroke(stroke))

        // ── Current-time position on the path ────────────────────────────
        val totalHours = (endHour - startHour).toFloat().coerceAtLeast(1f)
        val elapsedFrac = ((now.hour - startHour) + now.minute / 60f + now.second / 3600f) /
                totalHours
        val progressPerim = (elapsedFrac.coerceIn(0f, 1f)) * totalPerim

        // Passed segment colored by day score
        if (progressPerim > 0f) {
            val scoreColor = accuracyColor(dayScore)
            val passedPath = Path()
            val pm = android.graphics.PathMeasure(trackPath.asAndroidPath(), false)
            val dst = android.graphics.Path()
            pm.getSegment(0f, progressPerim, dst, true)
            drawPath(dst.asComposePath(), scoreColor, style = Stroke(stroke))
        }

        // ── Nodes ────────────────────────────────────────────────────────
        val n = targets.size
        if (n == 0) return@Canvas
        val spacing = totalPerim / n

        targets.forEachIndexed { idx, target ->
            val dist = idx * spacing + spacing / 2f
            val pos = perimToOffset(dist)

            val isPassed = when (val s = target.status) {
                is TargetStatus.Hit    -> true
                is TargetStatus.Missed -> true
                else -> now.hour > target.hour ||
                        (now.hour == target.hour && now.minute > 0)
            }
            val nodeColor = when {
                isPassed -> accuracyColor(dayScore)
                else     -> TerminalGray
            }

            when (val s = target.status) {
                is TargetStatus.Hit -> {
                    if (s.isPerfect && target.hour == perfectHitHour) {
                        // Pulsing white glow
                        drawCircle(TerminalWhite, nodeR * 2.5f, pos, alpha = pulseAlpha * 0.5f)
                    }
                    // Glow
                    drawCircle(accuracyColor(s.accuracy), nodeR * 2f, pos, alpha = 0.25f)
                    // Fill
                    drawCircle(accuracyColor(s.accuracy), nodeR, pos)
                    drawCircle(accuracyColor(s.accuracy), nodeR, pos, style = Stroke(2.5f))
                }
                is TargetStatus.Missed -> {
                    // Triangle for missed
                    val triPath = Path().apply {
                        moveTo(pos.x, pos.y - nodeR)
                        lineTo(pos.x + nodeR * 0.866f, pos.y + nodeR * 0.5f)
                        lineTo(pos.x - nodeR * 0.866f, pos.y + nodeR * 0.5f)
                        close()
                    }
                    drawPath(triPath, TerminalDeepRed)
                    drawPath(triPath, TerminalRed, style = Stroke(2f))
                }
                is TargetStatus.Pending -> {
                    drawCircle(TerminalBackground, nodeR, pos)
                    drawCircle(nodeColor, nodeR, pos, style = Stroke(2.5f))
                }
            }
        }

        // ── Press marker dots ─────────────────────────────────────────────
        markers.forEach { marker ->
            val dist = marker.pathFraction * totalPerim
            val pos = perimToOffset(dist)
            val dotColor = accuracyColor(marker.accuracy)
            drawCircle(dotColor, nodeR * 0.55f, pos)
            drawCircle(TerminalBackground, nodeR * 0.25f, pos)
        }
    }
}
