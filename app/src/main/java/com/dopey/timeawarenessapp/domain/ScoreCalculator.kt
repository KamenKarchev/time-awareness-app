package com.dopey.timeawarenessapp.domain

import kotlin.math.abs
import kotlin.math.roundToInt

object ScoreCalculator {

    fun hitAccuracy(minutesFromTarget: Long): Int {
        val clamped = abs(minutesFromTarget).coerceAtMost(59L)
        return (100 - (100.0 * clamped / 59.0)).roundToInt().coerceIn(0, 100)
    }

    /** Average accuracy of Hit targets only. 0 if nothing hit yet. */
    fun dayScore(targets: List<TargetTick>): Int {
        val hits = targets.filter { it.status is TargetStatus.Hit }
        if (hits.isEmpty()) return 0
        val total = hits.sumOf { (it.status as TargetStatus.Hit).accuracy }
        return (total.toDouble() / hits.size).roundToInt()
    }

    fun scoreFraction(accuracy: Int): Float = (accuracy / 100f).coerceIn(0f, 1f)
}
