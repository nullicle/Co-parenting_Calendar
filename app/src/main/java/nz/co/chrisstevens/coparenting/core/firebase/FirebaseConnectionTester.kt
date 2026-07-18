package nz.co.chrisstevens.coparenting.core.firebase

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "FirebaseConnectionTest"
private const val TEST_COLLECTION = "connection_test"
private const val TEST_DOCUMENT = "ping"

enum class FirestoreTestResult { NOT_RUN, RUNNING, PASS, FAIL }

/**
 * True once a FirebaseApp has been initialised - only possible if google-services.json was
 * present and valid at build time. Not a network check, just "is Firebase configured at all".
 */
fun isFirebaseAppInitialized(): Boolean =
    try {
        FirebaseApp.getInstance()
        true
    } catch (e: IllegalStateException) {
        false
    }

/**
 * Development-only helper: writes a small test document to Firestore and reads it straight back
 * to confirm the app can actually talk to the project, not just that it's configured. No
 * ViewModel, no DI - a plain class instantiated where it's used, same as the rest of the app.
 * Safe to delete this whole class (and its Settings section) once real syncing lands.
 */
class FirebaseConnectionTester {

    fun testConnection(onResult: (FirestoreTestResult) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection(TEST_COLLECTION).document(TEST_DOCUMENT)
        val payload = mapOf(
            "message" to "Hello from CoParenting Calendar",
            "writtenAtMillis" to System.currentTimeMillis()
        )

        docRef.set(payload)
            .addOnSuccessListener {
                docRef.get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists() && snapshot.getString("message") == payload["message"]) {
                            onResult(FirestoreTestResult.PASS)
                        } else {
                            Log.e(TAG, "Test document was written but did not read back as expected")
                            onResult(FirestoreTestResult.FAIL)
                        }
                    }
                    .addOnFailureListener { error ->
                        Log.e(TAG, "Failed to read back Firestore test document", error)
                        onResult(FirestoreTestResult.FAIL)
                    }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to write Firestore test document", error)
                onResult(FirestoreTestResult.FAIL)
            }
    }
}
