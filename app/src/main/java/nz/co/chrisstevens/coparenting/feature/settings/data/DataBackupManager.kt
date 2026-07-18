package nz.co.chrisstevens.coparenting.feature.settings.data

import android.content.Context
import android.net.Uri
import nz.co.chrisstevens.coparenting.feature.activity.data.ActivityRepository
import nz.co.chrisstevens.coparenting.feature.children.data.ChildRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentAssignmentRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private const val KEY_ACTIVITIES = "activities"
private const val KEY_CHILDREN = "children"
private const val KEY_PARENTS = "parents"
private const val KEY_PARENT_ASSIGNMENTS = "parentAssignments"

/**
 * Bundles the four JSON-backed repository files into one document for Storage Access Framework
 * export/import. No server, no new schema - it just moves the same JSON these repositories
 * already write into one file and back out again.
 */
class DataBackupManager(
    private val context: Context,
    private val activityRepository: ActivityRepository,
    private val childRepository: ChildRepository,
    private val parentRepository: ParentRepository,
    private val parentAssignmentRepository: ParentAssignmentRepository
) {

    fun exportTo(uri: Uri) {
        val bundle = JSONObject().apply {
            put(KEY_ACTIVITIES, readFileOrEmptyArray(activityRepository.file))
            put(KEY_CHILDREN, readFileOrEmptyArray(childRepository.file))
            put(KEY_PARENTS, readFileOrEmptyArray(parentRepository.file))
            put(KEY_PARENT_ASSIGNMENTS, readFileOrEmptyArray(parentAssignmentRepository.file))
        }
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(bundle.toString(2).toByteArray())
        }
    }

    fun importFrom(uri: Uri) {
        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            ?: return
        val bundle = JSONObject(text)

        bundle.optJSONArray(KEY_ACTIVITIES)?.let { activityRepository.file.writeText(it.toString()) }
        bundle.optJSONArray(KEY_CHILDREN)?.let { childRepository.file.writeText(it.toString()) }
        bundle.optJSONArray(KEY_PARENTS)?.let { parentRepository.file.writeText(it.toString()) }
        bundle.optJSONArray(KEY_PARENT_ASSIGNMENTS)?.let { parentAssignmentRepository.file.writeText(it.toString()) }

        activityRepository.reload()
        childRepository.reload()
        parentRepository.reload()
        parentAssignmentRepository.reload()
    }

    private fun readFileOrEmptyArray(file: File): JSONArray =
        if (file.exists()) JSONArray(file.readText()) else JSONArray()
}
