package com.kamenkarchev.timeawareness.intent

sealed class TimeIntent {
    object LogCurrentTime : TimeIntent()
    object ExportData : TimeIntent()
}
