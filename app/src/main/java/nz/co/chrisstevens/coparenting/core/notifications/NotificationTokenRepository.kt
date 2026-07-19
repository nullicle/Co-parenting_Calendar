package nz.co.chrisstevens.coparenting.core.notifications

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.co.chrisstevens.coparenting.core.firebase.awaitResult

private const val TAG = "NotificationTokenRepo"
private const val USERS_COLLECTION = "users"
private const val FCM_TOKENS_SUBCOLLECTION = "fcmTokens"

/**
 * Registers this device's FCM token under users/{uid}/fcmTokens/{token} so the backend
 * (Cloud Functions) knows where to send push notifications for this user. Keyed by the token
 * itself, not an auto-id, so re-registering the same token is a harmless idempotent upsert and
 * a token FCM later reports as invalid can be deleted by that same id.
 */
class NotificationTokenRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)

    /** Fetches the current FCM token and upserts it - call once per session, and on token refresh. */
    fun syncToken(uid: String) {
        scope.launch {
            runCatching {
                val token = FirebaseMessaging.getInstance().token.awaitResult()
                writeToken(uid, token)
            }.onFailure { Log.e(TAG, "Failed to fetch FCM token", it) }
        }
    }

    fun writeToken(uid: String, token: String) {
        firestore.collection(USERS_COLLECTION).document(uid)
            .collection(FCM_TOKENS_SUBCOLLECTION).document(token)
            .set(mapOf("createdAt" to System.currentTimeMillis(), "platform" to "android"))
            .addOnFailureListener { Log.e(TAG, "Failed to save FCM token", it) }
    }
}
