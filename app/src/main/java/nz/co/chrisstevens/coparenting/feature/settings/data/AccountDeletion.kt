package nz.co.chrisstevens.coparenting.feature.settings.data

import nz.co.chrisstevens.coparenting.feature.auth.data.AuthRepository
import nz.co.chrisstevens.coparenting.feature.family.data.FamilyRepository

/**
 * The full account deletion workflow, run in this order for one specific reason: Firestore
 * security rules require an authenticated request (request.auth != null), and that becomes null
 * the instant step 2 succeeds - so every Firestore write has to happen *before* the Firebase Auth
 * account itself is deleted, never after.
 *
 *  1. Leave the family in Firestore - removes this user from memberUids (reassigning ownership
 *     if they were the owner, or deleting the family entirely if they were the last member) and
 *     deletes their own users/{uid} document. This is exactly [FamilyRepository.leaveFamily] -
 *     the "Leave Family" button needs identical cleanup, so it's reused rather than duplicated.
 *  2. Delete the Firebase Authentication account. This is the step that can fail with
 *     FirebaseAuthRecentLoginRequiredException if the sign-in isn't recent; the caller (the
 *     Settings delete-account dialog) is expected to catch that, prompt reauthentication, then
 *     call this function again from the top.
 *  3. Explicitly sign out. delete() already invalidates the session, but this makes sure local
 *     auth state is cleared regardless of SDK version behaviour - this also detaches every
 *     repository's live Firestore listener via the app's top-level DisposableEffect.
 *
 * Retrying after a failure at step 2 is safe: step 1 finding no family reference (because it was
 * already cleared) is a no-op.
 */
suspend fun deleteAccount(
    uid: String,
    authRepository: AuthRepository,
    familyRepository: FamilyRepository
): Result<Unit> = runCatching {
    familyRepository.leaveFamily(uid).getOrThrow()

    authRepository.deleteFirebaseAccount().getOrThrow()

    authRepository.signOut()
}
