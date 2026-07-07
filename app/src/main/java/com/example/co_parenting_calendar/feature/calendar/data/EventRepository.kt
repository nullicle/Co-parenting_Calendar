package com.example.co_parenting_calendar.feature.calendar.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.example.co_parenting_calendar.feature.calendar.domain.Event
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

/**
 * Stores events as a small JSON file in app-private storage. No database, no DI framework -
 * just one class the UI reads from and writes through directly.
 */
class EventRepository(context: Context) {

    private val file = File(context.filesDir, "events.json")

    val events = mutableStateListOf<Event>().apply { addAll(readFromDisk()) }

    fun addEvent(event: Event) {
        events.add(event)
        writeToDisk()
    }

    fun updateEvent(event: Event) {
        val index = events.indexOfFirst { it.id == event.id }
        if (index != -1) events[index] = event
        writeToDisk()
    }

    fun deleteEvent(eventId: String) {
        events.removeAll { it.id == eventId }
        writeToDisk()
    }

    private fun readFromDisk(): List<Event> {
        if (!file.exists()) return emptyList()
        val json = JSONArray(file.readText())
        return (0 until json.length()).map { index ->
            val obj = json.getJSONObject(index)
            Event(
                id = obj.getString("id"),
                date = LocalDate.parse(obj.getString("date")),
                title = obj.getString("title"),
                notes = obj.optString("notes", "")
            )
        }
    }

    private fun writeToDisk() {
        val json = JSONArray()
        events.forEach { event ->
            json.put(
                JSONObject().apply {
                    put("id", event.id)
                    put("date", event.date.toString())
                    put("title", event.title)
                    put("notes", event.notes)
                }
            )
        }
        file.writeText(json.toString())
    }
}
