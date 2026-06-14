package com.dopey.timeawarenessapp.domain

import java.time.LocalDate
import java.time.LocalDateTime

// ── Raw press event ──────────────────────────────────────────────────────────
data class ClockEvent(
    val timestamp: LocalDateTime
)

// ── Status of one hourly target ───────────────────────────────────────────────
sealed class TargetStatus {
    object Pending : TargetStatus()
    object Missed  : TargetStatus()
    data class Hit(
        val event: ClockEvent,
        /** 0-100: 100 = exactly on the hour, 0 = 59 min away */
        val accuracy: Int,
        val isPerfect: Boolean
    ) : TargetStatus()
}

// ── One hour target slot ──────────────────────────────────────────────────────
data class TargetTick(
    val hour: Int,               // 9, 10, 11 …
    val status: TargetStatus = TargetStatus.Pending
)

// ── A press placed on the pill path ──────────────────────────────────────────
data class PressMarker(
    val event: ClockEvent,
    /** 0f-1f fraction of total path length, computed from event timestamp vs day range */
    val pathFraction: Float,
    /** colour of the dot on the path */
    val accuracy: Int,           // 0-100
    val targetHour: Int          // which target this press was assigned to
)

// ── Full day state ────────────────────────────────────────────────────────────
data class DayState(
    val date: LocalDate,
    val startHour: Int = 9,
    val endHour: Int = 19,
    val targets: List<TargetTick> = emptyList(),
    val rawEvents: List<ClockEvent> = emptyList(),
    val markers: List<PressMarker> = emptyList(),
    val score: Int = 0,
    val endScore: Int? = null
)
