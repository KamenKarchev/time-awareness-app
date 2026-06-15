package com.dopey.timeawarenessapp.domain

import kotlin.math.abs
import kotlin.math.roundToInt

object ScoreCalculator {

    fun hitAccuracy(minutesFromTarget: Long): Int {
        val clamped = abs(minutesFromTarget).coerceAtMost(59L)
        return (100 - (100.0 * clamped / 59.0)).roundToInt().coerceIn(0, 100)
    }

    /**
     * Average of all resolved targets (Hit = accuracy, Missed = 0).
     * Pending targets are excluded until they resolve.
     */
    fun dayScore(targets: List<TargetTick>): Int {
        val resolved = targets.filter { it.status !is TargetStatus.Pending }
        if (resolved.isEmpty()) return 0
        val total = resolved.sumOf { t ->
            when (val s = t.status) {
                is TargetStatus.Hit    -> s.accuracy
                is TargetStatus.Missed -> 0
                else                   -> 0
            }
        }
        return (total.toDouble() / resolved.size).roundToInt()
    }

    fun scoreFraction(accuracy: Int): Float = (accuracy / 100f).coerceIn(0f, 1f)
}
