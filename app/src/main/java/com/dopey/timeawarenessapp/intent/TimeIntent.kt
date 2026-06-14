package com.dopey.timeawarenessapp.intent

sealed class TimeIntent {
    /** User pressed the clock-in button */
    object ClockIn : TimeIntent()
    /** User confirmed early clock-in after popup */
    object ConfirmEarlyClockIn : TimeIntent()
    /** User dismissed early-clock popup (was a mistake) */
    object DismissEarlyDialog : TimeIntent()
    /** Dismiss the "all targets done" info dialog */
    object DismissAllDoneDialog : TimeIntent()
    /** Called every second from the UI */
    object Tick : TimeIntent()
    /** Range changed from the side drawer */
    data class SetTimeRange(val startHour: Int, val endHour: Int) : TimeIntent()
    object ExportData : TimeIntent()
    object OpenMenu : TimeIntent()
    object CloseMenu : TimeIntent()
}
