package nz.co.chrisstevens.coparenting.feature.activity.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import nz.co.chrisstevens.coparenting.core.firebase.lastModifiedByField
import nz.co.chrisstevens.coparenting.feature.activity.domain.Activity
import nz.co.chrisstevens.coparenting.feature.activity.domain.ActivityIconType
import nz.co.chrisstevens.coparenting.feature.activity.domain.RepeatRule
import java.time.LocalDate
import java.time.LocalTime

private const val TAG = "ActivityRepository"
private const val FAMILIES_COLLECTION = "families"
private const val ACTIVITIES_SUBCOLLECTION = "activities"

/**
 * Firestore-backed: activities live at families/{familyId}/activities, one document per
 * activity keyed by its own id (the same client-generated UUID the domain model already used).
 *
 * [attach] starts a live snapshot listener that keeps [activities] in sync with every family
 * member's changes in real time; [detach] stops it and clears local state. The listener is the
 * *only* thing that ever mutates [activities] - the CRUD methods below just write to Firestore
 * and let the listener reflect the change back, so there's a single source of truth and no risk
 * of local state drifting from what's actually stored (Firestore's offline persistence means the
 * listener still fires almost instantly with the optimistic local write, even before the server
 * round-trip completes).
 */
class ActivityRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    private var familyId: String? = null

    val activities = mutableStateListOf<Activity>()

    /** Starts (or restarts, if the family changed) the live listener for this family. */
    fun attach(familyId: String) {
        if (this.familyId == familyId && listenerRegistration != null) return
        detach()
        this.familyId = familyId
        listenerRegistration = activitiesCollection(familyId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Leave existing state as-is rather than clearing it - a transient network blip
                // shouldn't flash the calendar empty.
                Log.e(TAG, "Activities listener error for family $familyId", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            activities.clear()
            activities.addAll(snapshot.documents.mapNotNull { it.toActivityOrNull() })
        }
    }

    /** Stops listening and clears local state - call when leaving the family/signing out. */
    fun detach() {
        listenerRegistration?.remove()
        listenerRegistration = null
        familyId = null
        activities.clear()
    }

    fun addActivity(activity: Activity) = writeActivity(activity)

    fun updateActivity(activity: Activity) = writeActivity(activity)

    fun deleteActivity(activityId: String) {
        val familyId = familyId ?: return
        activitiesCollection(familyId).document(activityId).delete()
            .addOnFailureListener { Log.e(TAG, "Failed to delete activity $activityId", it) }
    }

    private fun writeActivity(activity: Activity) {
        val familyId = familyId ?: return
        val data = activity.toFirestoreMap() + lastModifiedByField()
        activitiesCollection(familyId).document(activity.id).set(data)
            .addOnFailureListener { Log.e(TAG, "Failed to save activity ${activity.id}", it) }
    }

    private fun activitiesCollection(familyId: String): CollectionReference =
        firestore.collection(FAMILIES_COLLECTION).document(familyId).collection(ACTIVITIES_SUBCOLLECTION)
}

private fun DocumentSnapshot.toActivityOrNull(): Activity? = runCatching {
    val childIds = (get("childIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val date = LocalDate.parse(getString("date"))
    Activity(
        id = id,
        date = date,
        endDate = getString("endDate")?.let(LocalDate::parse) ?: date,
        startTime = LocalTime.parse(getString("startTime")),
        endTime = getString("endTime")?.takeIf { it.isNotBlank() }?.let(LocalTime::parse),
        title = getString("title").orEmpty(),
        location = getString("location").orEmpty(),
        notes = getString("notes").orEmpty(),
        childIds = childIds,
        repeat = getString("repeat")?.let(RepeatRule::valueOf) ?: RepeatRule.NEVER,
        icon = getString("icon")?.let(ActivityIconType::valueOf) ?: ActivityIconType.OTHER
    )
}.getOrElse {
    Log.w(TAG, "Skipping malformed activity document $id", it)
    null
}

private fun Activity.toFirestoreMap(): Map<String, Any> = mapOf(
    "date" to date.toString(),
    "endDate" to endDate.toString(),
    "startTime" to startTime.toString(),
    "endTime" to (endTime?.toString() ?: ""),
    "title" to title,
    "location" to location,
    "notes" to notes,
    "childIds" to childIds,
    "repeat" to repeat.name,
    "icon" to icon.name
)
