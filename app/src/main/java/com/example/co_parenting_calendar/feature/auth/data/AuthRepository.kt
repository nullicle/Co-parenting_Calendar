package com.example.co_parenting_calendar.feature.auth.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.co_parenting_calendar.R
import com.example.co_parenting_calendar.core.firebase.awaitResult
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException

private const val TAG = "GoogleSignIn"

/**
 * Thin wrapper around FirebaseAuth. currentUser is exposed as Compose state via a plain
 * AuthStateListener - no ViewModel, this is constructed once in MainActivity like the other
 * repositories and just reflects whatever FirebaseAuth already tracks. Sign-in/out here never
 * touches the local JSON repositories - they stay completely independent for now.
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    var currentUser: FirebaseUser? by mutableStateOf(auth.currentUser)
        private set

    init {
        auth.addAuthStateListener { firebaseAuth -> currentUser = firebaseAuth.currentUser }
    }

    /**
     * Launches the system "Sign in with Google" picker via Credential Manager, then exchanges
     * the returned Google ID token for a Firebase credential. [context] must be an Activity
     * context - Credential Manager needs it to show the picker UI.
     */
    suspend fun signInWithGoogle(context: Context): Result<Unit> {
        return try {
            val option = GetSignInWithGoogleOption
                .Builder(context.getString(R.string.default_web_client_id))
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val response = CredentialManager.create(context).getCredential(context, request)
            val credential = response.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential).awaitResult()
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type returned by Credential Manager"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Google sign-in was cancelled or unavailable", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
