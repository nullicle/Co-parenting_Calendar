package com.example.co_parenting_calendar.feature.family.data

import android.util.Log
import com.example.co_parenting_calendar.core.firebase.awaitResult
import com.example.co_parenting_calendar.feature.family.domain.Family
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

private const val TAG = "FamilyRepository"
private const val FAMILIES_COLLECTION = "families"
private const val USERS_COLLECTION = "users"
private const val JOIN_CODE_LENGTH = 8

/** Excludes 0, O, I, 1 - characters that are easy to mix up when read off a phone screen. */
private const val JOIN_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

class FamilyNotFoundException(message: String) : Exception(message)

/**
 * Talks to Firestore directly for the one thing that's genuinely shared right now - family
 * membership. It never touches activities, children, or parent assignments; those stay in the
 * local JSON repositories exactly as before. No syncing happens here, just membership.
 */
class FamilyRepository {

    private val firestore = FirebaseFirestore.getInstance()

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
     * Removes the current user from their family (deleting the family document entirely if that
     * was the last member) and clears their own family reference, so the app routes them back
     * into onboarding on next check. Never touches local repositories - callers handle that
     * separately if they want to (e.g. the "leave and reset" developer tool).
     */
    suspend fun leaveFamily(uid: String): Result<Unit> = runCatching {
        val userDoc = firestore.collection(USERS_COLLECTION).document(uid).get().awaitResult()
        val familyId = userDoc.getString("familyId")
        if (familyId != null) {
            runCatching {
                val familyRef = firestore.collection(FAMILIES_COLLECTION).document(familyId)
                familyRef.update("memberUids", FieldValue.arrayRemove(uid)).awaitResult()

                val updatedDoc = familyRef.get().awaitResult()
                val remainingMembers = (updatedDoc.get("memberUids") as? List<*>)?.size ?: 0
                if (remainingMembers == 0) {
                    familyRef.delete().awaitResult()
                }
            }.onFailure { Log.w(TAG, "Could not fully clean up family $familyId, clearing local reference anyway", it) }
        }
        firestore.collection(USERS_COLLECTION).document(uid).delete().awaitResult()
        Unit
    }.onFailure { Log.e(TAG, "Failed to leave family", it) }

    private suspend fun saveFamilyReference(uid: String, familyId: String) {
        firestore.collection(USERS_COLLECTION).document(uid)
            .set(mapOf("familyId" to familyId))
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
