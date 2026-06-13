package com.dopey.timeawarenessapp.model

data class TimeState(
    val score: Long = 100,
    val logs: List<TimeLog> = emptyList(),
    val isExporting: Boolean = false
)
