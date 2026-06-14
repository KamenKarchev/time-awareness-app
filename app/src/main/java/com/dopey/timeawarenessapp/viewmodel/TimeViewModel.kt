package com.dopey.timeawarenessapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dopey.timeawarenessapp.data.XmlRepository
import com.dopey.timeawarenessapp.domain.ClockEvent
import com.dopey.timeawarenessapp.domain.ScoreCalculator
import com.dopey.timeawarenessapp.domain.TargetResolver
import com.dopey.timeawarenessapp.domain.TargetStatus
import com.dopey.timeawarenessapp.intent.TimeIntent
import com.dopey.timeawarenessapp.model.AppUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class TimeViewModel(private val repo: XmlRepository) : ViewModel() {

    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        val today = LocalDate.now()
        val day = repo.loadDay(today)
        _state.update { it.copy(day = day) }
    }

    fun processIntent(intent: TimeIntent) {
        when (intent) {
            is TimeIntent.Tick                 -> tick()
            is TimeIntent.ClockIn              -> handleClockIn()
            is TimeIntent.ConfirmEarlyClockIn  -> commitClockIn(forceEarly = true)
            is TimeIntent.DismissEarlyDialog   -> _state.update { it.copy(showEarlyDialog = false) }
            is TimeIntent.DismissAllDoneDialog -> _state.update { it.copy(showAllDoneDialog = false) }
            is TimeIntent.SetTimeRange         -> setRange(intent.startHour, intent.endHour)
            is TimeIntent.ExportData           -> export()
            is TimeIntent.OpenMenu             -> _state.update { it.copy(isMenuOpen = true) }
            is TimeIntent.CloseMenu            -> _state.update { it.copy(isMenuOpen = false) }
            is TimeIntent.DebugSetTime         -> debugSetTime(intent.dateTime)
            is TimeIntent.DebugResetDay        -> debugResetDay()
        }
    }

    private fun tick() {
        val now = _state.value.debugTime ?: LocalDateTime.now()
        _state.update { s ->
            val clearPerfect = if (s.perfectHitHour != null &&
                ChronoUnit.SECONDS.between(s.now, now) >= 4) null else s.perfectHitHour
            s.copy(now = now, perfectHitHour = clearPerfect)
        }
        val today = LocalDate.now()
        if (_state.value.day.date != today) { rolloverDay(today); return }

        // Auto-mark any Pending target whose hour is strictly in the past as Missed
        val day = _state.value.day
        val missedAny = day.targets.any {
            it.status is TargetStatus.Pending && it.hour < now.hour
        }
        if (missedAny) {
            val updated = day.targets.map { t ->
                if (t.status is TargetStatus.Pending && t.hour < now.hour)
                    t.copy(status = TargetStatus.Missed)
                else t
            }
            val newDay = day.copy(targets = updated, score = ScoreCalculator.dayScore(updated))
            _state.update { it.copy(day = newDay) }
            viewModelScope.launch { repo.saveDay(newDay) }
        }
    }

    private fun handleClockIn() {
        val s = _state.value
        if (TargetResolver.allDone(s.day.targets)) {
            _state.update { it.copy(showAllDoneDialog = true) }; return
        }
        val nextPending = TargetResolver.nextPendingTarget(s.day.targets, s.now)
        if (nextPending == null) {
            _state.update { it.copy(showEarlyDialog = true) }; return
        }
        commitClockIn(forceEarly = false)
    }

    private fun commitClockIn(forceEarly: Boolean) {
        _state.update { it.copy(showEarlyDialog = false) }
        val s   = _state.value
        val day = s.day
        val now = s.now
        val targetTick = if (forceEarly)
            day.targets.firstOrNull { it.status is TargetStatus.Pending }
        else
            TargetResolver.nextPendingTarget(day.targets, now)
        ?: return

        val event     = ClockEvent(timestamp = now)
        val targetDt  = LocalDateTime.of(day.date, LocalTime.of(targetTick!!.hour, 0))
        val minutes   = ChronoUnit.MINUTES.between(targetDt, now)
        val accuracy  = ScoreCalculator.hitAccuracy(minutes)
        val isPerfect = now.minute == 0 && now.second < 5

        val totalHours = (day.endHour - day.startHour).toFloat().coerceAtLeast(1f)
        val pathFrac = ((now.hour - day.startHour) + now.minute / 60f) / totalHours

        val newMarker = com.dopey.timeawarenessapp.domain.PressMarker(
            event        = event,
            pathFraction = pathFrac.coerceIn(0f, 1f),
            accuracy     = accuracy,
            targetHour   = targetTick.hour
        )
        val updatedTargets = day.targets.map { t ->
            if (t.hour == targetTick.hour)
                t.copy(status = TargetStatus.Hit(event, accuracy, isPerfect))
            else t
        }
        val newDay = day.copy(
            rawEvents = day.rawEvents + event,
            markers   = day.markers + newMarker,
            targets   = updatedTargets,
            score     = ScoreCalculator.dayScore(updatedTargets)
        )
        _state.update { it.copy(
            day = newDay,
            perfectHitHour = if (isPerfect) targetTick!!.hour else it.perfectHitHour
        )}
        viewModelScope.launch { repo.saveDay(newDay) }
    }

    private fun setRange(start: Int, end: Int) {
        if (end <= start) return
        val day = _state.value.day
        val (resolvedTargets, resolvedMarkers) = TargetResolver.resolve(day.date, start, end, day.rawEvents)
        val newDay = day.copy(
            startHour = start, endHour = end,
            targets = resolvedTargets, markers = resolvedMarkers,
            score = ScoreCalculator.dayScore(resolvedTargets)
        )
        _state.update { it.copy(day = newDay, isMenuOpen = false) }
        viewModelScope.launch { repo.saveDay(newDay) }
    }

    private fun rolloverDay(today: LocalDate) {
        val old = _state.value.day
        val finalTargets = old.targets.map { t ->
            if (t.status is TargetStatus.Pending) t.copy(status = TargetStatus.Missed) else t
        }
        val finalDay = old.copy(targets = finalTargets, endScore = ScoreCalculator.dayScore(finalTargets))
        viewModelScope.launch { repo.saveDay(finalDay) }
        _state.update { it.copy(day = repo.loadDay(today)) }
    }

    private fun export() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            repo.exportToExternal()
            _state.update { it.copy(isExporting = false) }
        }
    }

    private fun debugSetTime(dateTime: LocalDateTime) {
        _state.update { it.copy(debugTime = dateTime, now = dateTime) }
    }

    private fun debugResetDay() {
        val day = _state.value.day
        val freshTargets = TargetResolver.buildTargets(day.date, day.startHour, day.endHour)
        val newDay = day.copy(rawEvents = emptyList(), markers = emptyList(),
            targets = freshTargets, score = 0, endScore = null)
        _state.update { it.copy(day = newDay) }
        viewModelScope.launch { repo.saveDay(newDay) }
    }
}

class TimeViewModelFactory(private val repo: XmlRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimeViewModel::class.java)) return TimeViewModel(repo) as T
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
