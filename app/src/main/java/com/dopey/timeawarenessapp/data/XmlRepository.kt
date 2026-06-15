package com.dopey.timeawarenessapp.data

import android.content.Context
import android.util.Xml
import com.dopey.timeawarenessapp.domain.ClockEvent
import com.dopey.timeawarenessapp.domain.DayState
import com.dopey.timeawarenessapp.domain.ScoreCalculator
import com.dopey.timeawarenessapp.domain.TargetResolver
import com.dopey.timeawarenessapp.domain.TargetStatus
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime

class XmlRepository(private val context: Context) {

    private val file: File
        get() = File(context.filesDir, "time_awareness.xml")

    fun saveDay(day: DayState) {
        val all = loadAllDays().toMutableMap()
        all[day.date] = day
        writeAll(all)
    }

    fun loadDay(date: LocalDate): DayState =
        loadAllDays()[date] ?: DayState(
            date = date,
            targets = TargetResolver.buildTargets(date, 9, 19)
        )

    /** Map of date → final score for every persisted day. */
    fun loadDayScores(): Map<LocalDate, Int> =
        loadAllDays().mapValues { (_, day) ->
            day.endScore ?: ScoreCalculator.dayScore(day.targets)
        }

    /** Average over all days that have at least one resolved target. */
    fun overallAverage(): Int {
        val scores = loadDayScores().values.toList()
        if (scores.isEmpty()) return 0
        return scores.average().toInt()
    }

    private fun writeAll(days: Map<LocalDate, DayState>) {
        val serializer = Xml.newSerializer()
        FileOutputStream(file).use { out ->
            serializer.setOutput(out, "UTF-8")
            serializer.startDocument("UTF-8", true)
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.startTag(null, "timeAwareness")
            for ((_, day) in days) {
                serializer.startTag(null, "day")
                serializer.attribute(null, "date",      day.date.toString())
                serializer.attribute(null, "startHour", day.startHour.toString())
                serializer.attribute(null, "endHour",   day.endHour.toString())
                if (day.endScore != null)
                    serializer.attribute(null, "endScore", day.endScore.toString())
                for (e in day.rawEvents) {
                    serializer.startTag(null, "clockEvent")
                    serializer.attribute(null, "ts", e.timestamp.toString())
                    serializer.endTag(null, "clockEvent")
                }
                for (t in day.targets) {
                    serializer.startTag(null, "resolved")
                    serializer.attribute(null, "hour", t.hour.toString())
                    when (val s = t.status) {
                        is TargetStatus.Hit -> {
                            serializer.attribute(null, "status",   "hit")
                            serializer.attribute(null, "accuracy", s.accuracy.toString())
                            serializer.attribute(null, "ts",       s.event.timestamp.toString())
                            serializer.attribute(null, "perfect",  s.isPerfect.toString())
                        }
                        is TargetStatus.Missed  -> serializer.attribute(null, "status", "missed")
                        is TargetStatus.Pending -> serializer.attribute(null, "status", "pending")
                    }
                    serializer.endTag(null, "resolved")
                }
                serializer.endTag(null, "day")
            }
            serializer.endTag(null, "timeAwareness")
            serializer.endDocument()
        }
    }

    private fun loadAllDays(): Map<LocalDate, DayState> {
        if (!file.exists()) return emptyMap()
        val result = mutableMapOf<LocalDate, DayState>()
        val parser = Xml.newPullParser()
        FileInputStream(file).use { inp ->
            parser.setInput(inp, "UTF-8")
            var date: LocalDate? = null
            var startHour = 9; var endHour = 19; var endScore: Int? = null
            val events = mutableListOf<ClockEvent>()
            var eventType = parser.next()
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "day" -> {
                            date      = LocalDate.parse(parser.getAttributeValue(null, "date"))
                            startHour = parser.getAttributeValue(null, "startHour").toInt()
                            endHour   = parser.getAttributeValue(null, "endHour").toInt()
                            endScore  = parser.getAttributeValue(null, "endScore")?.toIntOrNull()
                            events.clear()
                        }
                        "clockEvent" ->
                            events.add(ClockEvent(LocalDateTime.parse(parser.getAttributeValue(null, "ts"))))
                    }
                } else if (eventType == XmlPullParser.END_TAG && parser.name == "day" && date != null) {
                    val (targets, markers) = TargetResolver.resolve(date, startHour, endHour, events.toList())
                    result[date] = DayState(
                        date      = date,
                        startHour = startHour,
                        endHour   = endHour,
                        targets   = targets,
                        rawEvents = events.toList(),
                        markers   = markers,
                        score     = ScoreCalculator.dayScore(targets),
                        endScore  = endScore
                    )
                    date = null
                }
                eventType = parser.next()
            }
        }
        return result
    }

    fun exportToExternal(): File {
        val dest = File(context.getExternalFilesDir(null), "time_awareness_export.xml")
        file.copyTo(dest, overwrite = true)
        return dest
    }
}
