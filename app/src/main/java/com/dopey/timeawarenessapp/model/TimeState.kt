package com.dopey.timeawarenessapp.model

import java.time.LocalDateTime

data class TimeLog(
    val expectedHour: Int,
    val actualTime: LocalDateTime,
    val deviationMinutes: Long
)

data class TimeState(
    val score: Long = 100,
    val startHour: Int = 9,
    val endHour: Int = 17,
    val logs: List<TimeLog> = emptyList(),
    val isExporting: Boolean = false
) {
    val hourNodes: List<Int>
        get() = (startHour until endHour).toList()
}
