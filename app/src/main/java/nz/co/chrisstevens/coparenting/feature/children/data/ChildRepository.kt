package nz.co.chrisstevens.coparenting.feature.children.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import nz.co.chrisstevens.coparenting.feature.children.domain.Child

private const val TAG = "ChildRepository"
private const val FAMILIES_COLLECTION = "families"
private const val CHILDREN_SUBCOLLECTION = "children"

/**
 * Firestore-backed: children live at families/{familyId}/children, one document per child keyed
 * by its own id. Same attach/detach/single-source-of-truth pattern as ActivityRepository - see
 * that class for the full explanation.
 */
class ChildRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    private var familyId: String? = null

    val children = mutableStateListOf<Child>()

    /** Starts (or restarts, if the family changed) the live listener for this family. */
    fun attach(familyId: String) {
        if (this.familyId == familyId && listenerRegistration != null) return
        detach()
        this.familyId = familyId
        listenerRegistration = childrenCollection(familyId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Children listener error for family $familyId", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            children.clear()
            children.addAll(snapshot.documents.mapNotNull { it.toChildOrNull() })
        }
    }

    /** Stops listening and clears local state - call when leaving the family/signing out. */
    fun detach() {
        listenerRegistration?.remove()
        listenerRegistration = null
        familyId = null
        children.clear()
    }

    fun addChild(child: Child) = writeChild(child)

    fun updateChild(child: Child) = writeChild(child)

    fun deleteChild(childId: String) {
        val familyId = familyId ?: return
        childrenCollection(familyId).document(childId).delete()
            .addOnFailureListener { Log.e(TAG, "Failed to delete child $childId", it) }
    }

    private fun writeChild(child: Child) {
        val familyId = familyId ?: return
        childrenCollection(familyId).document(child.id).set(child.toFirestoreMap())
            .addOnFailureListener { Log.e(TAG, "Failed to save child ${child.id}", it) }
    }

    private fun childrenCollection(familyId: String): CollectionReference =
        firestore.collection(FAMILIES_COLLECTION).document(familyId).collection(CHILDREN_SUBCOLLECTION)
}

private fun DocumentSnapshot.toChildOrNull(): Child? = runCatching {
    Child(
        id = id,
        name = getString("name").orEmpty(),
        colorArgb = getLong("colorArgb") ?: 0xFF000000
    )
}.getOrElse {
    Log.w(TAG, "Skipping malformed child document $id", it)
    null
}

private fun Child.toFirestoreMap(): Map<String, Any> = mapOf(
    "name" to name,
    "colorArgb" to colorArgb
)
