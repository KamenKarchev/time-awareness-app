package com.dopey.timeawarenessapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopey.timeawarenessapp.domain.TargetTick
import com.dopey.timeawarenessapp.ui.theme.TerminalGray
import com.dopey.timeawarenessapp.ui.theme.accuracyColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private val TimeFmt = DateTimeFormatter.ofPattern("HH:mm:ss")

@Composable
fun ClockInOrb(
    now: LocalDateTime,
    nextTarget: TargetTick?,
    onClockIn: () -> Unit,
    size: Dp = 140.dp
) {
    // Distance-to-target colour: green if on the hour, red if 59m away
    val minutesFromTarget = if (nextTarget != null) {
        val targetMin = nextTarget.hour * 60
        val nowMin    = now.hour * 60 + now.minute
        abs(nowMin - targetMin).toLong()
    } else 0L

    val accuracy = if (nextTarget != null)
        (100 - (minutesFromTarget.toFloat() / 59f * 100f).toInt()).coerceIn(0, 100)
    else 50

    val color by animateColorAsState(
        targetValue = accuracyColor(accuracy),
        animationSpec = tween(500),
        label = "clockColor"
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(3.dp, color, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClockIn
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = now.format(TimeFmt),
                color = color,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (nextTarget != null) "→ %02d:00".format(nextTarget.hour) else "done",
                color = color.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
