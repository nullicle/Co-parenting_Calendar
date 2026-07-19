package nz.co.chrisstevens.coparenting.core.firebase

import com.google.firebase.auth.FirebaseAuth

/**
 * Stamped onto every write to a family-shared document. Firestore triggers have no built-in way
 * to know which user made a write (unlike security rules' request.auth) - the backend reads this
 * field back to know whose change a notification should skip notifying.
 */
fun lastModifiedByField(): Map<String, Any> =
    mapOf("lastModifiedByUid" to (FirebaseAuth.getInstance().currentUser?.uid ?: ""))
