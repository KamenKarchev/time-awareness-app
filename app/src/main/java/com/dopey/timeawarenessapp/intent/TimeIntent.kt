package com.dopey.timeawarenessapp.intent

import java.time.LocalDateTime

sealed class TimeIntent {
    object ClockIn : TimeIntent()
    object ConfirmEarlyClockIn : TimeIntent()
    object DismissEarlyDialog : TimeIntent()
    object DismissAllDoneDialog : TimeIntent()
    object Tick : TimeIntent()
    data class SetTimeRange(val startHour: Int, val endHour: Int) : TimeIntent()
    object ExportData : TimeIntent()
    object OpenMenu : TimeIntent()
    object CloseMenu : TimeIntent()
    data class DebugSetTime(val dateTime: LocalDateTime) : TimeIntent()
    object DebugResetDay : TimeIntent()
}
