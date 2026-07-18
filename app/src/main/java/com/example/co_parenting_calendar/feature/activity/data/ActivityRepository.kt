package com.example.co_parenting_calendar.feature.activity.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.example.co_parenting_calendar.feature.activity.domain.Activity
import com.example.co_parenting_calendar.feature.activity.domain.ActivityIconType
import com.example.co_parenting_calendar.feature.activity.domain.RepeatRule
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

/**
 * Stores activities as a small JSON file in app-private storage. No database, no DI framework -
 * just one class the UI reads from and writes through directly. [file] is exposed (read-only)
 * and [reload] is public so the Settings export/import feature can move raw JSON in and out
 * without this class needing to know anything about Storage Access Framework.
 */
class ActivityRepository(context: Context) {

    val file = File(context.filesDir, "activities.json")

    val activities = mutableStateListOf<Activity>().apply { addAll(readFromDisk()) }

    fun reload() {
        activities.clear()
        activities.addAll(readFromDisk())
    }

    /** Wipes every activity, local only - used by the developer "reset local data" tools. */
    fun clear() {
        activities.clear()
        file.delete()
    }

    fun addActivity(activity: Activity) {
        activities.add(activity)
        writeToDisk()
    }

    fun updateActivity(activity: Activity) {
        val index = activities.indexOfFirst { it.id == activity.id }
        if (index != -1) activities[index] = activity
        writeToDisk()
    }

    fun deleteActivity(activityId: String) {
        activities.removeAll { it.id == activityId }
        writeToDisk()
    }

    private fun readFromDisk(): List<Activity> {
        if (!file.exists()) return emptyList()
        val json = JSONArray(file.readText())
        return (0 until json.length()).mapNotNull { index ->
            runCatching { parseActivity(json.getJSONObject(index)) }.getOrNull()
        }
    }

    private fun parseActivity(obj: JSONObject): Activity {
        val childIdsJson = obj.optJSONArray("childIds")
        val childIds = if (childIdsJson != null) {
            (0 until childIdsJson.length()).map { childIdsJson.getString(it) }
        } else {
            emptyList()
        }
        val date = LocalDate.parse(obj.getString("date"))
        return Activity(
            id = obj.getString("id"),
            date = date,
            endDate = if (obj.has("endDate")) LocalDate.parse(obj.getString("endDate")) else date,
            startTime = LocalTime.parse(obj.getString("startTime")),
            endTime = obj.optString("endTime", "").takeIf { it.isNotBlank() }?.let(LocalTime::parse),
            title = obj.getString("title"),
            location = obj.optString("location", ""),
            notes = obj.optString("notes", ""),
            childIds = childIds,
            repeat = runCatching { RepeatRule.valueOf(obj.optString("repeat", "NEVER")) }
                .getOrDefault(RepeatRule.NEVER),
            icon = runCatching { ActivityIconType.valueOf(obj.optString("icon", "OTHER")) }
                .getOrDefault(ActivityIconType.OTHER)
        )
    }

    private fun writeToDisk() {
        val json = JSONArray()
        activities.forEach { activity ->
            json.put(
                JSONObject().apply {
                    put("id", activity.id)
                    put("date", activity.date.toString())
                    put("endDate", activity.endDate.toString())
                    put("startTime", activity.startTime.toString())
                    put("endTime", activity.endTime?.toString() ?: "")
                    put("title", activity.title)
                    put("location", activity.location)
                    put("notes", activity.notes)
                    put("childIds", JSONArray(activity.childIds))
                    put("repeat", activity.repeat.name)
                    put("icon", activity.icon.name)
                }
            )
        }
        file.writeText(json.toString())
    }
}
