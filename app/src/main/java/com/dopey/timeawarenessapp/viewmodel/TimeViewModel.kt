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
        }
    }

    private fun logTime() {
        val now = LocalDateTime.now()
        val expected = if (now.minute >= 30) {
            now.plusHours(1).truncatedTo(ChronoUnit.HOURS)
        } else {
            now.truncatedTo(ChronoUnit.HOURS)
        }
        val deviation = abs(ChronoUnit.MINUTES.between(expected, now))

        _state.update { current ->
            current.copy(
                score = (current.score - deviation).coerceAtLeast(0),
                logs = current.logs + TimeLog(
                    expectedTime = expected,
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
