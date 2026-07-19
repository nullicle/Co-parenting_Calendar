package nz.co.chrisstevens.coparenting.feature.family.data

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import nz.co.chrisstevens.coparenting.core.firebase.awaitResult
import nz.co.chrisstevens.coparenting.feature.family.domain.Family
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

private const val TAG = "FamilyRepository"
private const val FAMILIES_COLLECTION = "families"
private const val USERS_COLLECTION = "users"
private const val JOIN_CODE_LENGTH = 8

/** Excludes 0, O, I, 1 - characters that are easy to mix up when read off a phone screen. */
private const val JOIN_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

/** Every subcollection that lives under families/{familyId} - cleaned up together when a family is deleted. */
private val FAMILY_SUBCOLLECTIONS = listOf("activities", "children", "parents", "parentAssignments")

class FamilyNotFoundException(message: String) : Exception(message)

/**
 * Talks to Firestore for family membership (create/join/leave) and, via [attach]/[detach],
 * keeps [currentFamily] live-updated so "family settings change" (join code, members, owner)
 * shows up immediately - e.g. Settings' member list updates the moment someone else joins.
 * Activities/children/parents/parentAssignments live in their own repositories, one Firestore
 * subcollection each under this same family document; this class only touches the family
 * document itself and the per-user family reference.
 */
class FamilyRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    private var familyId: String? = null

    var currentFamily: Family? by mutableStateOf(null)
        private set

    /** uid -> display name, for every current member of [currentFamily]. Refreshed whenever membership changes. */
    var memberDisplayNames: Map<String, String> by mutableStateOf(emptyMap())
        private set

    /** Starts (or restarts, if the family changed) the live listener for this family document. */
    fun attach(familyId: String) {
        if (this.familyId == familyId && listenerRegistration != null) return
        detach()
        this.familyId = familyId
        listenerRegistration = firestore.collection(FAMILIES_COLLECTION).document(familyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Family listener error for family $familyId", error)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val family = snapshot.toFamily()
                currentFamily = family
                refreshMemberNames(family.memberUids)
            }
    }

    /** Stops listening and clears local state - call when leaving the family/signing out. */
    fun detach() {
        listenerRegistration?.remove()
        listenerRegistration = null
        familyId = null
        currentFamily = null
        memberDisplayNames = emptyMap()
    }

    /**
     * Records this user's display name/email on their users/{uid} document so other family
     * members can resolve it (Firestore has no client-readable directory of other users'
     * Firebase Auth profiles - this is the only way another member's name becomes visible).
     * Call once per session as soon as a user is signed in; merged so it never clobbers
     * [saveFamilyReference]'s familyId field or vice versa.
     */
    fun syncUserProfile(uid: String, displayName: String?, email: String?) {
        val profile = buildMap {
            displayName?.takeIf { it.isNotBlank() }?.let { put("displayName", it) }
            email?.takeIf { it.isNotBlank() }?.let { put("email", it) }
        }
        if (profile.isEmpty()) return
        firestore.collection(USERS_COLLECTION).document(uid)
            .set(profile, SetOptions.merge())
            .addOnFailureListener { Log.e(TAG, "Failed to sync user profile for $uid", it) }
    }

    /** One-time lookup of every member's stored profile - fixed, small family sizes make a single whereIn cheap. */
    private fun refreshMemberNames(memberUids: List<String>) {
        if (memberUids.isEmpty()) {
            memberDisplayNames = emptyMap()
            return
        }
        firestore.collection(USERS_COLLECTION)
            .whereIn(FieldPath.documentId(), memberUids)
            .get()
            .addOnSuccessListener { snapshot ->
                memberDisplayNames = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("displayName")?.takeIf { it.isNotBlank() }
                        ?: doc.getString("email")?.takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null
                    doc.id to name
                }.toMap()
            }
            .addOnFailureListener { Log.e(TAG, "Failed to load member names", it) }
    }

    /** The family the given user already belongs to, or null if they don't have one yet. */
    suspend fun findFamilyForUser(uid: String): Family? {
        val userDoc = firestore.collection(USERS_COLLECTION).document(uid).get().awaitResult()
        val familyId = userDoc.getString("familyId") ?: return null
        val familyDoc = firestore.collection(FAMILIES_COLLECTION).document(familyId).get().awaitResult()
        return if (familyDoc.exists()) familyDoc.toFamily() else null
    }

    suspend fun createFamily(ownerUid: String): Result<Family> = runCatching {
        val familyRef = firestore.collection(FAMILIES_COLLECTION).document()
        val family = Family(
            id = familyRef.id,
            joinCode = generateJoinCode(),
            ownerUid = ownerUid,
            memberUids = listOf(ownerUid),
            createdAt = System.currentTimeMillis()
        )
        familyRef.set(family.toFirestoreMap()).awaitResult()
        saveFamilyReference(ownerUid, family.id)
        family
    }.onFailure { Log.e(TAG, "Failed to create family", it) }

    suspend fun joinFamily(joinCode: String, uid: String): Result<Family> = runCatching {
        val normalizedCode = joinCode.trim().uppercase()
        val matches = firestore.collection(FAMILIES_COLLECTION)
            .whereEqualTo("joinCode", normalizedCode)
            .limit(1)
            .get()
            .awaitResult()

        val familyDoc = matches.documents.firstOrNull()
            ?: throw FamilyNotFoundException("No family found with that invite code")

        familyDoc.reference.update("memberUids", FieldValue.arrayUnion(uid)).awaitResult()
        saveFamilyReference(uid, familyDoc.id)

        val family = familyDoc.toFamily()
        family.copy(memberUids = (family.memberUids + uid).distinct())
    }.onFailure { Log.e(TAG, "Failed to join family", it) }

    /**
     * Removes the current user from their family and deletes their own Firestore user document,
     * so the app routes them back into onboarding on next check. Reused as-is by account
     * deletion - it needs exactly this same cleanup as its first step.
     *
     * Family membership is updated inside a transaction (see [removeMemberFromFamily]) rather
     * than a plain read-then-write, so two members leaving at the same moment can't both read a
     * stale member list and reach the wrong conclusion about who's left or who owns it.
     */
    suspend fun leaveFamily(uid: String): Result<Unit> = runCatching {
        val userDoc = firestore.collection(USERS_COLLECTION).document(uid).get().awaitResult()
        val familyId = userDoc.getString("familyId")
        if (familyId != null) {
            runCatching { removeMemberFromFamily(familyId, uid) }
                .onFailure { Log.w(TAG, "Could not fully clean up family $familyId, clearing local reference anyway", it) }
        }
        firestore.collection(USERS_COLLECTION).document(uid).delete().awaitResult()
        Unit
    }.onFailure { Log.e(TAG, "Failed to leave family", it) }

    /**
     * Atomically removes [uid] from the family: if [uid] was the last member, the family
     * document is deleted (and its subcollections cleaned up afterwards); otherwise [uid] is
     * dropped from memberUids, reassigning ownership to another remaining member first if [uid]
     * was the owner, so the family is never left ownerless.
     */
    private suspend fun removeMemberFromFamily(familyId: String, uid: String) {
        val familyRef = firestore.collection(FAMILIES_COLLECTION).document(familyId)

        val deletedEmptyFamily = firestore.runTransaction { transaction ->
            val snapshot = transaction.get(familyRef)
            if (!snapshot.exists()) return@runTransaction false

            val members = (snapshot.get("memberUids") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val remaining = members - uid

            if (remaining.isEmpty()) {
                transaction.delete(familyRef)
                true
            } else {
                val updates = mutableMapOf<String, Any>("memberUids" to remaining)
                if (snapshot.getString("ownerUid") == uid) {
                    updates["ownerUid"] = remaining.first()
                }
                transaction.update(familyRef, updates)
                false
            }
        }.awaitResult()

        if (deletedEmptyFamily) {
            deleteFamilySubcollections(familyId)
        }
    }

    private suspend fun deleteFamilySubcollections(familyId: String) {
        val familyRef = firestore.collection(FAMILIES_COLLECTION).document(familyId)
        FAMILY_SUBCOLLECTIONS.forEach { subcollection ->
            deleteAllDocumentsIn(familyRef.collection(subcollection))
        }
    }

    /** Batches the delete so it's one round-trip rather than one per document. */
    private suspend fun deleteAllDocumentsIn(collectionRef: CollectionReference) {
        val snapshot = collectionRef.get().awaitResult()
        if (snapshot.isEmpty) return
        val batch = firestore.batch()
        snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
        batch.commit().awaitResult()
    }

    private suspend fun saveFamilyReference(uid: String, familyId: String) {
        firestore.collection(USERS_COLLECTION).document(uid)
            .set(mapOf("familyId" to familyId), SetOptions.merge())
            .awaitResult()
    }

    private fun generateJoinCode(): String =
        (1..JOIN_CODE_LENGTH).map { JOIN_CODE_ALPHABET.random() }.joinToString("")
}

private fun DocumentSnapshot.toFamily(): Family = Family(
    id = id,
    joinCode = getString("joinCode").orEmpty(),
    ownerUid = getString("ownerUid").orEmpty(),
    memberUids = (get("memberUids") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    createdAt = getLong("createdAt") ?: 0L
)

private fun Family.toFirestoreMap(): Map<String, Any> = mapOf(
    "joinCode" to joinCode,
    "ownerUid" to ownerUid,
    "memberUids" to memberUids,
    "createdAt" to createdAt
)

fun Throwable.toFamilyErrorMessage(): String = when (this) {
    is FamilyNotFoundException -> "No family found with that invite code. Double-check and try again."
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Can't reach the server right now. Check your connection and try again."
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "You don't have permission to do that."
        else -> "Something went wrong. Please try again."
    }
    else -> "Something went wrong. Please try again."
}
