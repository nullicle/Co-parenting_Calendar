package nz.co.chrisstevens.coparenting.feature.family.ui

import com.google.firebase.auth.FirebaseUser

/**
 * Resolves each member's display name: for the signed-in user, straight from their live
 * FirebaseUser profile; for everyone else, from [memberDisplayNames] (FamilyRepository's
 * lookup of each member's users/{uid} document, the only client-readable copy of another
 * member's profile info). Falls back to "Member N" if a name genuinely isn't available yet.
 */
fun familyMemberLabels(
    memberUids: List<String>,
    currentUser: FirebaseUser?,
    memberDisplayNames: Map<String, String>
): List<String> =
    memberUids.mapIndexed { index, uid ->
        if (currentUser != null && uid == currentUser.uid) {
            currentUser.displayName?.takeIf { it.isNotBlank() }
                ?: currentUser.email?.takeIf { it.isNotBlank() }
                ?: "Member ${index + 1}"
        } else {
            memberDisplayNames[uid]?.takeIf { it.isNotBlank() } ?: "Member ${index + 1}"
        }
    }
