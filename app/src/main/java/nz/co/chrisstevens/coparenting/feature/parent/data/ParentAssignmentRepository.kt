package nz.co.chrisstevens.coparenting.feature.parent.data

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import nz.co.chrisstevens.coparenting.core.firebase.lastModifiedByField
import nz.co.chrisstevens.coparenting.feature.parent.domain.ParentSlot
import java.time.LocalDate

private const val TAG = "ParentAssignmentRepository"
private const val FAMILIES_COLLECTION = "families"
private const val PARENT_ASSIGNMENTS_SUBCOLLECTION = "parentAssignments"

/**
 * Firestore-backed: which parent "owns" each date lives at
 * families/{familyId}/parentAssignments/{yyyy-MM-dd}, one document per date. Same attach/detach/
 * single-source-of-truth pattern as ActivityRepository - see that class for the full
 * explanation, just Map-shaped here instead of List-shaped.
 */
class ParentAssignmentRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    private var familyId: String? = null

    val assignments = mutableStateMapOf<LocalDate, ParentSlot>()

    /** Starts (or restarts, if the family changed) the live listener for this family. */
    fun attach(familyId: String) {
        if (this.familyId == familyId && listenerRegistration != null) return
        detach()
        this.familyId = familyId
        listenerRegistration = assignmentsCollection(familyId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Parent assignments listener error for family $familyId", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            assignments.clear()
            snapshot.documents.forEach { doc ->
                doc.toAssignmentOrNull()?.let { (date, slot) -> assignments[date] = slot }
            }
        }
    }

    /** Stops listening and clears local state - call when leaving the family/signing out. */
    fun detach() {
        listenerRegistration?.remove()
        listenerRegistration = null
        familyId = null
        assignments.clear()
    }

    fun assign(date: LocalDate, slot: ParentSlot) {
        val familyId = familyId ?: return
        val data = mapOf("parent" to slot.name) + lastModifiedByField()
        assignmentsCollection(familyId).document(date.toString())
            .set(data)
            .addOnFailureListener { Log.e(TAG, "Failed to save assignment for $date", it) }
    }

    private fun assignmentsCollection(familyId: String): CollectionReference =
        firestore.collection(FAMILIES_COLLECTION).document(familyId).collection(PARENT_ASSIGNMENTS_SUBCOLLECTION)
}

private fun DocumentSnapshot.toAssignmentOrNull(): Pair<LocalDate, ParentSlot>? = runCatching {
    LocalDate.parse(id) to ParentSlot.valueOf(getString("parent")!!)
}.getOrElse {
    Log.w(TAG, "Skipping malformed parent assignment document $id", it)
    null
}
