package com.dopey.timeawarenessapp.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object TargetResolver {

    /** Build an initial target list for a day */
    fun buildTargets(date: LocalDate, startHour: Int, endHour: Int): List<TargetTick> =
        (startHour until endHour).map { TargetTick(hour = it) }

    /**
     * Resolve raw events onto targets (greedy: first unresolved target gets the next press).
     * Returns updated target list + press markers.
     */
    fun resolve(
        date: LocalDate,
        startHour: Int,
        endHour: Int,
        events: List<ClockEvent>
    ): Pair<List<TargetTick>, List<PressMarker>> {
        val totalHours = (endHour - startHour).toFloat().coerceAtLeast(1f)
        val targets = buildTargets(date, startHour, endHour).toMutableList()
        val markers = mutableListOf<PressMarker>()
        var targetIdx = 0

        for (event in events.sortedBy { it.timestamp }) {
            if (targetIdx >= targets.size) break
            val target = targets[targetIdx]
            val targetDt = LocalDateTime.of(date, java.time.LocalTime.of(target.hour, 0))
            val minutes = ChronoUnit.MINUTES.between(targetDt, event.timestamp)
            val accuracy = ScoreCalculator.hitAccuracy(minutes)
            val isPerfect = event.timestamp.minute == 0 && event.timestamp.second < 5

            targets[targetIdx] = target.copy(
                status = TargetStatus.Hit(event, accuracy, isPerfect)
            )

            // path fraction: (hour - startHour + minuteFraction) / totalHours
            val pressHourFrac = (event.timestamp.hour - startHour +
                    event.timestamp.minute / 60f +
                    event.timestamp.second / 3600f) / totalHours

            markers.add(
                PressMarker(
                    event = event,
                    pathFraction = pressHourFrac.coerceIn(0f, 1f),
                    accuracy = accuracy,
                    targetHour = target.hour
                )
            )
            targetIdx++
        }

        return targets to markers
    }

    /** Which target is currently open (Pending and hour <= now)?  */
    fun nextPendingTarget(targets: List<TargetTick>, now: LocalDateTime): TargetTick? {
        val passed = targets.filter { it.status is TargetStatus.Pending && it.hour <= now.hour }
        return passed.firstOrNull()
    }

    /** Are all targets in range already resolved? */
    fun allDone(targets: List<TargetTick>): Boolean =
        targets.none { it.status is TargetStatus.Pending }
}
