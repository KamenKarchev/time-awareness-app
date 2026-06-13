package com.dopey.timeawarenessapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dopey.timeawarenessapp.data.XmlRepository
import com.dopey.timeawarenessapp.intent.TimeIntent
import com.dopey.timeawarenessapp.model.TimeLog
import com.dopey.timeawarenessapp.model.TimeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class TimeViewModel(private val repository: XmlRepository) : ViewModel() {

    private val _state = MutableStateFlow(TimeState())
    val state: StateFlow<TimeState> = _state.asStateFlow()

    fun processIntent(intent: TimeIntent) {
        when (intent) {
            is TimeIntent.LogCurrentTime -> logTime()
            is TimeIntent.ExportData -> exportXml()
            is TimeIntent.SetTimeRange -> setRange(intent.startHour, intent.endHour)
        }
    }

    private fun logTime() {
        val now = LocalDateTime.now()
        val expectedHour = if (now.minute >= 30) now.hour + 1 else now.hour
        val expected = now.truncatedTo(ChronoUnit.HOURS)
            .let { if (now.minute >= 30) it.plusHours(1) else it }
        val deviation = abs(ChronoUnit.MINUTES.between(expected, now))

        // Only allow logging if expectedHour is within the configured range
        val current = _state.value
        if (expectedHour !in current.startHour until current.endHour) return
        // Prevent duplicate log for same hour
        if (current.logs.any { it.expectedHour == expectedHour }) return

        _state.update { s ->
            s.copy(
                score = (s.score - deviation).coerceAtLeast(0),
                logs = s.logs + TimeLog(
                    expectedHour = expectedHour,
                    actualTime = now,
                    deviationMinutes = deviation
                )
            )
        }
    }

    private fun exportXml() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            repository.exportStateToXml(_state.value)
            _state.update { it.copy(isExporting = false) }
        }
    }

    private fun setRange(start: Int, end: Int) {
        if (end > start) {
            _state.update { it.copy(startHour = start, endHour = end, logs = emptyList(), score = 100) }
        }
    }
}

class TimeViewModelFactory(private val repository: XmlRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
