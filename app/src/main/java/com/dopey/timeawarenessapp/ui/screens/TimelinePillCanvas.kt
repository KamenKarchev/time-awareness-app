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
    val infiniteTransition = rememberInfiniteTransition(label = "pill")

    // White outer glow for perfect hit
    val perfectPulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "perfect"
    )
    // Gray fill circle for missed: radius fraction 0→1
    val missedPulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "missed"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke     = 5.dp.toPx()
        val nodeR      = 13.dp.toPx()
        val capR       = w / 2f
        val straightH  = (h - 2 * capR).coerceAtLeast(0f)
        val semiPerim  = PI.toFloat() * capR
        val totalPerim = 2 * straightH + 2 * semiPerim
        val cx         = w / 2f
        val totalHours = (endHour - startHour).toFloat().coerceAtLeast(1f)
        val hourSeg    = totalPerim / totalHours

        fun perimToOffset(raw: Float): Offset {
            val d    = ((raw % totalPerim) + totalPerim) % totalPerim
            val seg1 = semiPerim / 2f
            val seg2 = seg1 + straightH
            val seg3 = seg2 + semiPerim
            val seg4 = seg3 + straightH
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

        // ── Dim track ───────────────────────────────────────────────
        val trackPath = Path().apply {
            addRoundRect(RoundRect(Rect(0f, 0f, w, h), CornerRadius(capR)))
        }
        drawPath(trackPath, TerminalMidGray.copy(alpha = 0.5f), style = Stroke(stroke))

        // ── Progress arc (minute precision) ────────────────────────────
        val elapsedHours = (now.hour - startHour) + now.minute / 60f
        val progressDist = (elapsedHours * hourSeg).coerceIn(0f, totalPerim)
        if (progressDist > 0f) {
            val arcPath = Path()
            val steps = 200
            for (i in 0..steps) {
                val off = perimToOffset(progressDist * i / steps)
                if (i == 0) arcPath.moveTo(off.x, off.y) else arcPath.lineTo(off.x, off.y)
            }
            drawPath(arcPath, accuracyColor(dayScore), style = Stroke(stroke))
        }

        if (targets.isEmpty()) return@Canvas

        // ── Target nodes ──────────────────────────────────────────────
        // Rules (all solid fills, no outlines):
        // Pending, arc not passed  → solid TerminalGray
        // Pending, arc passed      → solid accuracyColor(dayScore) [live, same as arc]
        // Hit normal               → solid accuracyColor(s.accuracy) [frozen at hit time]
        // Hit perfect (current)    → solid TerminalGreen + white outer pulse
        // Missed                   → solid TerminalDeepRed + pulsating gray circle
        targets.forEachIndexed { idx, target ->
            val dist      = idx * hourSeg
            val pos       = perimToOffset(dist)
            val arcPassed = dist < progressDist

            when (val s = target.status) {

                is TargetStatus.Pending -> {
                    val fillColor = if (arcPassed) accuracyColor(dayScore) else TerminalGray
                    drawCircle(fillColor, nodeR, pos)
                }

                is TargetStatus.Hit -> {
                    val isPerfectNow = s.isPerfect && target.hour == perfectHitHour
                    val fillColor    = if (s.isPerfect) TerminalGreen else accuracyColor(s.accuracy)
                    if (isPerfectNow) {
                        // Pulsating white outer glow
                        drawCircle(TerminalWhite, nodeR * 2.5f, pos, alpha = perfectPulse * 0.6f)
                    }
                    drawCircle(fillColor, nodeR, pos)
                }

                is TargetStatus.Missed -> {
                    // Solid deep-red triangle
                    val triPath = Path().apply {
                        moveTo(pos.x, pos.y - nodeR)
                        lineTo(pos.x + nodeR * 0.866f, pos.y + nodeR * 0.5f)
                        lineTo(pos.x - nodeR * 0.866f, pos.y + nodeR * 0.5f)
                        close()
                    }
                    drawPath(triPath, TerminalDeepRed)
                    // Pulsating light-gray circle: radius 0 → nodeR, full alpha
                    drawCircle(TerminalGray, missedPulse * nodeR, pos)
                }
            }
        }

        // ── Press marker dots (always on top) ───────────────────────
        markers.forEach { marker ->
            val dist = marker.pathFraction * totalPerim
            val pos  = perimToOffset(dist)
            drawCircle(accuracyColor(marker.accuracy), nodeR * 0.55f, pos)
            drawCircle(TerminalBackground, nodeR * 0.25f, pos)
        }
    }
}
