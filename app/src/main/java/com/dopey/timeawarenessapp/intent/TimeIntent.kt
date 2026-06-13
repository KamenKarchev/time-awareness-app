package com.dopey.timeawarenessapp.intent

sealed class TimeIntent {
    object LogCurrentTime : TimeIntent()
    object ExportData : TimeIntent()
}
