package com.dopey.timeawarenessapp.model

import com.dopey.timeawarenessapp.domain.DayState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

data class AppUiState(
    val now: LocalDateTime          = LocalDateTime.now(),
    val day: DayState               = DayState(date = LocalDate.now()),
    val showEarlyDialog: Boolean    = false,
    val showAllDoneDialog: Boolean  = false,
    val isExporting: Boolean        = false,
    val perfectHitHour: Int?        = null,
    val debugEnabled: Boolean       = false,
    val debugTime: LocalDateTime?   = null,
    val historyMonth: YearMonth     = YearMonth.now(),
    // date → score for every saved day (used by history calendar)
    val allDayScores: Map<LocalDate, Int> = emptyMap(),
    val overallAverage: Int         = 0
)
