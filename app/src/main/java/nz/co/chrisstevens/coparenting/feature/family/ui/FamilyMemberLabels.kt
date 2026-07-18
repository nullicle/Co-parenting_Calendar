package nz.co.chrisstevens.coparenting.feature.family.ui

import com.google.firebase.auth.FirebaseUser

/**
 * TODO: We don't yet store other members' profile info (display name/email) anywhere
 * client-accessible - Firestore only has their uid, and looking up another user's Firebase Auth
 * profile isn't possible from the client. Once member profiles are written to users/{uid} at
 * sign-in/join time, resolve every member's real name here instead of the "Member N" fallback.
 */
fun familyMemberLabels(memberUids: List<String>, currentUser: FirebaseUser?): List<String> =
    memberUids.mapIndexed { index, uid ->
        if (currentUser != null && uid == currentUser.uid) {
            currentUser.displayName?.takeIf { it.isNotBlank() }
                ?: currentUser.email?.takeIf { it.isNotBlank() }
                ?: "Member ${index + 1}"
        } else {
            "Member ${index + 1}"
        }
    }
