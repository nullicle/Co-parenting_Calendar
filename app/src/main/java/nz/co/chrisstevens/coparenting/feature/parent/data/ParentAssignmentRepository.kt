package nz.co.chrisstevens.coparenting.feature.parent.data

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import nz.co.chrisstevens.coparenting.feature.parent.domain.ParentSlot
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

/**
 * Stores which parent slot "owns" each date, as a small JSON file - same pattern as the other
 * repositories.
 */
class ParentAssignmentRepository(context: Context) {

    val file = File(context.filesDir, "parent_assignments.json")

    val assignments = mutableStateMapOf<LocalDate, ParentSlot>().apply { putAll(readFromDisk()) }

    fun reload() {
        assignments.clear()
        assignments.putAll(readFromDisk())
    }

    /** Wipes every assignment, local only - used by the developer "reset local data" tools. */
    fun clear() {
        assignments.clear()
        file.delete()
    }

    fun assign(date: LocalDate, slot: ParentSlot) {
        assignments[date] = slot
        writeToDisk()
    }

    private fun readFromDisk(): Map<LocalDate, ParentSlot> {
        if (!file.exists()) return emptyMap()
        val json = JSONArray(file.readText())
        val result = mutableMapOf<LocalDate, ParentSlot>()
        for (index in 0 until json.length()) {
            runCatching {
                val obj = json.getJSONObject(index)
                result[LocalDate.parse(obj.getString("date"))] = ParentSlot.valueOf(obj.getString("parent"))
            }
        }
        return result
    }

    private fun writeToDisk() {
        val json = JSONArray()
        assignments.forEach { (date, slot) ->
            json.put(
                JSONObject().apply {
                    put("date", date.toString())
                    put("parent", slot.name)
                }
            )
        }
        file.writeText(json.toString())
    }
}
