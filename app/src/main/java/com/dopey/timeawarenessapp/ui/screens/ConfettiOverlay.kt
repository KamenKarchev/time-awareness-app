package com.dopey.timeawarenessapp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

private data class Particle(
    val angle: Float,       // radians
    val speed: Float,       // px per unit progress
    val radius: Float,
    val color: Color,
    val rotSpeed: Float     // rotation per unit progress
)

@Composable
fun ConfettiOverlay(trigger: Boolean, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    val particles = remember {
        val colors = listOf(
            Color(0xFF39FF14), Color(0xFFFFB300), Color(0xFFFF3D00),
            Color(0xFFFFFFFF), Color(0xFF00CFFF), Color(0xFFFF00FF)
        )
        List(70) {
            Particle(
                angle    = Random.nextFloat() * 2f * PI.toFloat(),
                speed    = Random.nextFloat() * 900f + 400f,
                radius   = Random.nextFloat() * 8f + 4f,
                color    = colors[Random.nextInt(colors.size)],
                rotSpeed = Random.nextFloat() * 4f - 2f
            )
        }
    }

    LaunchedEffect(trigger) {
        if (trigger) {
            progress.snapTo(0f)
            launch { progress.animateTo(1f, tween(2000)) }
        }
    }

    val p = progress.value
    if (p <= 0f || p >= 1f) return

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val gravity = 1200f

        particles.forEach { particle ->
            val x = cx + cos(particle.angle) * particle.speed * p
            val y = cy + sin(particle.angle) * particle.speed * p + gravity * p * p
            val alpha = (1f - p).coerceIn(0f, 1f)
            drawCircle(
                color  = particle.color,
                radius = particle.radius,
                center = Offset(x, y),
                alpha  = alpha
            )
        }
    }
}
