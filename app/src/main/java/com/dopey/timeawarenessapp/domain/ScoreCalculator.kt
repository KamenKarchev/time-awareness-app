package com.dopey.timeawarenessapp.domain

import kotlin.math.abs
import kotlin.math.roundToInt

object ScoreCalculator {

    /** accuracy for a single hit: minutes from target hour, 0-100 */
    fun hitAccuracy(minutesFromTarget: Long): Int {
        val clamped = abs(minutesFromTarget).coerceAtMost(59L)
        return (100 - (100.0 * clamped / 59.0)).roundToInt().coerceIn(0, 100)
    }

    /** day score = average of per-target accuracies (missed = 0) */
    fun dayScore(targets: List<TargetTick>): Int {
        if (targets.isEmpty()) return 0
        val total = targets.sumOf { t ->
            when (val s = t.status) {
                is TargetStatus.Hit    -> s.accuracy
                is TargetStatus.Missed -> 0
                is TargetStatus.Pending -> 0
            }
        }
        return (total.toDouble() / targets.size).roundToInt()
    }

    /** colour fraction 0f=red 1f=green for a 0-100 accuracy value */
    fun scoreFraction(accuracy: Int): Float = (accuracy / 100f).coerceIn(0f, 1f)
}
