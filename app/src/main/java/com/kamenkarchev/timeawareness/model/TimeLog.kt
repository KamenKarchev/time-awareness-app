package com.kamenkarchev.timeawareness.model

import java.time.LocalDateTime

data class TimeLog(
    val expectedTime: LocalDateTime,
    val actualTime: LocalDateTime,
    val deviationMinutes: Long
)
