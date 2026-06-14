package com.dopey.timeawarenessapp.debug

import java.time.LocalDateTime

data class DebugTimeOverride(val time: LocalDateTime)

object DebugTimeProvider {
    var override: DebugTimeOverride? = null
    fun now(): LocalDateTime = override?.time ?: LocalDateTime.now()
}
