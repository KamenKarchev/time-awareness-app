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
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke    = 3.5.dp.toPx()
        val nodeR     = 9.dp.toPx()
        val capR      = w / 2f
        val straightH = (h - 2 * capR).coerceAtLeast(0f)
        val semiPerim = PI.toFloat() * capR
        val totalPerim = 2 * straightH + 2 * semiPerim
        val cx = w / 2f
        val totalHours = (endHour - startHour).toFloat().coerceAtLeast(1f)
        val hourSeg = totalPerim / totalHours

        // d=0 = top-centre, advances clockwise
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

        // ── Full dim track ────────────────────────────────────────────────
        val trackPath = Path().apply {
            addRoundRect(RoundRect(Rect(0f, 0f, w, h), CornerRadius(capR)))
        }
        drawPath(trackPath, TerminalGray.copy(alpha = 0.3f), style = Stroke(stroke))

        // ── Progress arc ────────────────────────────────────────────────
        val elapsedHours = (now.hour - startHour) + now.minute / 60f + now.second / 3600f
        val progressDist = (elapsedHours * hourSeg).coerceIn(0f, totalPerim)
        if (progressDist > 0f) {
            val steps = 200
            val arcPath = Path()
            for (i in 0..steps) {
                val d   = progressDist * i / steps
                val off = perimToOffset(d)
                if (i == 0) arcPath.moveTo(off.x, off.y) else arcPath.lineTo(off.x, off.y)
            }
            drawPath(arcPath, accuracyColor(dayScore), style = Stroke(stroke))
        }

        if (targets.isEmpty()) return@Canvas

        // ── Target nodes: idx-th node at exactly d = idx * hourSeg ────────
        // The first target (startHour) sits at d=0 (top-centre).
        // Each subsequent node is exactly hourSeg further along.
        targets.forEachIndexed { idx, target ->
            val dist = idx * hourSeg
            val pos  = perimToOffset(dist)

            val isPassed = target.status is TargetStatus.Hit ||
                    target.status is TargetStatus.Missed ||
                    now.hour > target.hour ||
                    (now.hour == target.hour && now.minute > 0)
            val nodeColor = if (isPassed) accuracyColor(dayScore) else TerminalGray

            when (val s = target.status) {
                is TargetStatus.Hit -> {
                    if (s.isPerfect && target.hour == perfectHitHour)
                        drawCircle(TerminalWhite, nodeR * 2.5f, pos, alpha = pulseAlpha * 0.5f)
                    drawCircle(accuracyColor(s.accuracy), nodeR * 2f, pos, alpha = 0.25f)
                    drawCircle(accuracyColor(s.accuracy), nodeR, pos)
                    drawCircle(accuracyColor(s.accuracy), nodeR, pos, style = Stroke(2.5f))
                }
                is TargetStatus.Missed -> {
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

        // ── Press marker dots (drawn after nodes so they render on top) ───
        markers.forEach { marker ->
            val dist = marker.pathFraction * totalPerim
            val pos  = perimToOffset(dist)
            drawCircle(accuracyColor(marker.accuracy), nodeR * 0.55f, pos)
            drawCircle(TerminalBackground, nodeR * 0.25f, pos)
        }
    }
}
