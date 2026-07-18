package com.example.co_parenting_calendar.feature.children.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.example.co_parenting_calendar.feature.children.domain.Child
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Stores children as a small JSON file in app-private storage, same pattern as ActivityRepository.
 */
class ChildRepository(context: Context) {

    val file = File(context.filesDir, "children.json")

    val children = mutableStateListOf<Child>().apply { addAll(readFromDisk()) }

    fun reload() {
        children.clear()
        children.addAll(readFromDisk())
    }

    /** Wipes every child, local only - used by the developer "reset local data" tools. */
    fun clear() {
        children.clear()
        file.delete()
    }

    fun addChild(child: Child) {
        children.add(child)
        writeToDisk()
    }

    fun updateChild(child: Child) {
        val index = children.indexOfFirst { it.id == child.id }
        if (index != -1) children[index] = child
        writeToDisk()
    }

    fun deleteChild(childId: String) {
        children.removeAll { it.id == childId }
        writeToDisk()
    }

    private fun readFromDisk(): List<Child> {
        if (!file.exists()) return emptyList()
        val json = JSONArray(file.readText())
        return (0 until json.length()).mapNotNull { index ->
            runCatching {
                val obj = json.getJSONObject(index)
                Child(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    colorArgb = obj.getLong("colorArgb")
                )
            }.getOrNull()
        }
    }

    private fun writeToDisk() {
        val json = JSONArray()
        children.forEach { child ->
            json.put(
                JSONObject().apply {
                    put("id", child.id)
                    put("name", child.name)
                    put("colorArgb", child.colorArgb)
                }
            )
        }
        file.writeText(json.toString())
    }
}
