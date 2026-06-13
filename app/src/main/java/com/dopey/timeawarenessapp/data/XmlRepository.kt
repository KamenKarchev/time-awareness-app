package com.dopey.timeawarenessapp.data

import android.content.Context
import android.util.Xml
import com.dopey.timeawarenessapp.model.TimeState
import java.io.File
import java.io.FileOutputStream

class XmlRepository(private val context: Context) {

    fun exportStateToXml(state: TimeState): File {
        val file = File(context.getExternalFilesDir(null), "time_accuracy_data.xml")
        val serializer = Xml.newSerializer()
        FileOutputStream(file).use { output ->
            serializer.setOutput(output, "UTF-8")
            serializer.startDocument("UTF-8", true)
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.startTag(null, "TimeAwarenessData")
            serializer.startTag(null, "Score")
            serializer.text(state.score.toString())
            serializer.endTag(null, "Score")
            serializer.startTag(null, "Range")
            serializer.attribute(null, "start", state.startHour.toString())
            serializer.attribute(null, "end", state.endHour.toString())
            serializer.endTag(null, "Range")
            state.logs.forEach { log ->
                serializer.startTag(null, "Log")
                serializer.attribute(null, "expectedHour", log.expectedHour.toString())
                serializer.attribute(null, "actual", log.actualTime.toString())
                serializer.attribute(null, "deviationMinutes", log.deviationMinutes.toString())
                serializer.endTag(null, "Log")
            }
            serializer.endTag(null, "TimeAwarenessData")
            serializer.endDocument()
        }
        return file
    }
}
