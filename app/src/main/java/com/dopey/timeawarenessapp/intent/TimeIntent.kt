package com.dopey.timeawarenessapp.intent

sealed class TimeIntent {
    object LogCurrentTime : TimeIntent()
    object ExportData : TimeIntent()
    data class SetTimeRange(val startHour: Int, val endHour: Int) : TimeIntent()
}
