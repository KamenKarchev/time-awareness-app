package com.dopey.timeawarenessapp.model

import com.dopey.timeawarenessapp.domain.DayState
import java.time.LocalDate
import java.time.LocalDateTime

data class AppUiState(
    val now: LocalDateTime = LocalDateTime.now(),
    val day: DayState = DayState(date = LocalDate.now()),
    val isMenuOpen: Boolean = false,
    val showEarlyDialog: Boolean = false,
    val showAllDoneDialog: Boolean = false,
    val isExporting: Boolean = false,
    val perfectHitHour: Int? = null,
    val debugTime: LocalDateTime? = null
)
