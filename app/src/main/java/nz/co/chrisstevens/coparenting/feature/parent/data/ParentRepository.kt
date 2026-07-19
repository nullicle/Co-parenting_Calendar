package nz.co.chrisstevens.coparenting.feature.parent.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import nz.co.chrisstevens.coparenting.core.firebase.lastModifiedByField
import nz.co.chrisstevens.coparenting.feature.parent.domain.Parent
import nz.co.chrisstevens.coparenting.feature.parent.domain.ParentSlot

private const val TAG = "ParentRepository"
private const val FAMILIES_COLLECTION = "families"
private const val PARENTS_SUBCOLLECTION = "parents"

/**
 * Firestore-backed: the two parents live at families/{familyId}/parents/ONE and .../TWO. There
 * are always exactly two - identified by a fixed [ParentSlot] used as the document id - so this
 * only ever updates, never adds or removes. If a family is ever found with no parent documents
 * (a brand new family, or an older one from before this seeding existed), [attach]'s listener
 * seeds the two defaults itself the moment it sees an empty collection - self-healing rather
 * than relying on family creation to have done it, so there's exactly one place responsible for
 * "every family always has two parents."
 *
 * Same attach/detach/single-source-of-truth pattern as ActivityRepository - see that class for
 * the full explanation.
 */
class ParentRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    private var familyId: String? = null

    val parents = mutableStateListOf<Parent>()

    /** Starts (or restarts, if the family changed) the live listener for this family. */
    fun attach(familyId: String) {
        if (this.familyId == familyId && listenerRegistration != null) return
        detach()
        this.familyId = familyId
        listenerRegistration = parentsCollection(familyId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Parents listener error for family $familyId", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            if (snapshot.isEmpty) {
                seedDefaultParents(familyId)
                return@addSnapshotListener
            }
            parents.clear()
            parents.addAll(snapshot.documents.mapNotNull { it.toParentOrNull() })
        }
    }

    /** Fixed document ids, so this is safe to run redundantly (e.g. both devices seeing the family empty at once). */
    private fun seedDefaultParents(familyId: String) {
        val collection = parentsCollection(familyId)
        val batch = firestore.batch()
        batch.set(collection.document("ONE"), mapOf("name" to "Parent 1", "colorArgb" to 0xFF2196F3L))
        batch.set(collection.document("TWO"), mapOf("name" to "Parent 2", "colorArgb" to 0xFF4CAF50L))
        batch.commit().addOnFailureListener { Log.e(TAG, "Failed to seed default parents for family $familyId", it) }
    }

    /** Stops listening and clears local state - call when leaving the family/signing out. */
    fun detach() {
        listenerRegistration?.remove()
        listenerRegistration = null
        familyId = null
        parents.clear()
    }

    fun updateParent(parent: Parent) {
        val familyId = familyId ?: return
        val data = parent.toFirestoreMap() + lastModifiedByField()
        parentsCollection(familyId).document(parent.slot.name).set(data)
            .addOnFailureListener { Log.e(TAG, "Failed to save parent ${parent.slot}", it) }
    }

    private fun parentsCollection(familyId: String): CollectionReference =
        firestore.collection(FAMILIES_COLLECTION).document(familyId).collection(PARENTS_SUBCOLLECTION)
}

private fun DocumentSnapshot.toParentOrNull(): Parent? = runCatching {
    Parent(
        slot = ParentSlot.valueOf(id),
        name = getString("name").orEmpty(),
        colorArgb = getLong("colorArgb") ?: 0xFF000000
    )
}.getOrElse {
    Log.w(TAG, "Skipping malformed parent document $id", it)
    null
}

private fun Parent.toFirestoreMap(): Map<String, Any> = mapOf(
    "name" to name,
    "colorArgb" to colorArgb
)
