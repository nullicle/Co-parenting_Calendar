package com.example.co_parenting_calendar.feature.parent.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.example.co_parenting_calendar.feature.parent.domain.Parent
import com.example.co_parenting_calendar.feature.parent.domain.ParentSlot
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Stores the two parents' editable name/colour. There are always exactly two - identified by a
 * fixed [ParentSlot] - so this only ever updates, never adds or removes.
 */
class ParentRepository(context: Context) {

    val file = File(context.filesDir, "parents.json")

    val parents = mutableStateListOf<Parent>().apply { addAll(loadParents()) }

    fun reload() {
        parents.clear()
        parents.addAll(loadParents())
    }

    fun updateParent(parent: Parent) {
        val index = parents.indexOfFirst { it.slot == parent.slot }
        if (index != -1) parents[index] = parent
        writeToDisk()
    }

    private fun loadParents(): List<Parent> {
        val stored = readFromDisk()
        return defaultParents().map { default -> stored.find { it.slot == default.slot } ?: default }
    }

    private fun defaultParents(): List<Parent> = listOf(
        Parent(ParentSlot.ONE, "Parent 1", 0xFF2196F3),
        Parent(ParentSlot.TWO, "Parent 2", 0xFF4CAF50)
    )

    private fun readFromDisk(): List<Parent> {
        if (!file.exists()) return emptyList()
        val json = JSONArray(file.readText())
        return (0 until json.length()).mapNotNull { index ->
            runCatching {
                val obj = json.getJSONObject(index)
                Parent(
                    slot = ParentSlot.valueOf(obj.getString("slot")),
                    name = obj.getString("name"),
                    colorArgb = obj.getLong("colorArgb")
                )
            }.getOrNull()
        }
    }

    private fun writeToDisk() {
        val json = JSONArray()
        parents.forEach { parent ->
            json.put(
                JSONObject().apply {
                    put("slot", parent.slot.name)
                    put("name", parent.name)
                    put("colorArgb", parent.colorArgb)
                }
            )
        }
        file.writeText(json.toString())
    }
}
