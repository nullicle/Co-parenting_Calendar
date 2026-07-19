package nz.co.chrisstevens.coparenting.feature.settings.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestoreException
import nz.co.chrisstevens.coparenting.feature.auth.data.AuthRepository
import nz.co.chrisstevens.coparenting.feature.family.data.FamilyRepository
import nz.co.chrisstevens.coparenting.feature.settings.data.deleteAccount
import kotlinx.coroutines.launch

/**
 * Every step of deleting an account, as one small state machine: an explicit confirmation
 * (permanent, can't be undone), a progress state while it runs, a dedicated "please sign in
 * again" step for Firebase's recent-login requirement, and a plain error state for anything
 * else. Success has no explicit state here - deleteAccount()'s last step signs the user out,
 * which flips authRepository.currentUser to null, and CoParentingCalendarApp's top-level gate
 * takes it from there (this dialog just unmounts along with the rest of Settings).
 */
private sealed class DeleteAccountStep {
    object Confirming : DeleteAccountStep()
    object Deleting : DeleteAccountStep()
    object NeedsReauth : DeleteAccountStep()
    object Reauthenticating : DeleteAccountStep()
    data class Failed(val message: String) : DeleteAccountStep()
}

@Composable
fun DeleteAccountDialog(
    uid: String,
    authRepository: AuthRepository,
    familyRepository: FamilyRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf<DeleteAccountStep>(DeleteAccountStep.Confirming) }

    fun runDeletion() {
        step = DeleteAccountStep.Deleting
        scope.launch {
            val result = deleteAccount(
                uid = uid,
                authRepository = authRepository,
                familyRepository = familyRepository
            )
            result.onFailure { error ->
                step = if (error is FirebaseAuthRecentLoginRequiredException) {
                    DeleteAccountStep.NeedsReauth
                } else {
                    DeleteAccountStep.Failed(error.toDeleteAccountErrorMessage())
                }
            }
        }
    }

    when (val current = step) {
        DeleteAccountStep.Confirming -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Delete account?") },
            text = {
                Text(
                    "This permanently deletes your account and sign-in. If you're the only " +
                        "member of your family, the shared family and its calendar will be " +
                        "deleted too. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { runDeletion() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete Account") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        )

        DeleteAccountStep.Deleting, DeleteAccountStep.Reauthenticating -> AlertDialog(
            onDismissRequest = {},
            title = { Text(if (current is DeleteAccountStep.Reauthenticating) "Signing in…" else "Deleting account…") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("  Please wait.")
                }
            },
            confirmButton = {}
        )

        DeleteAccountStep.NeedsReauth -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Please sign in again") },
            text = {
                Text(
                    "For your security, deleting your account requires a recent sign-in. " +
                        "Sign in again to continue."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        step = DeleteAccountStep.Reauthenticating
                        scope.launch {
                            val result = authRepository.reauthenticateWithGoogle(context)
                            result.onSuccess { runDeletion() }
                                .onFailure { step = DeleteAccountStep.Failed(it.toDeleteAccountErrorMessage()) }
                        }
                    }
                ) { Text("Sign In Again") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        )

        is DeleteAccountStep.Failed -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Couldn't delete account") },
            text = { Text(current.message) },
            confirmButton = {
                TextButton(onClick = { step = DeleteAccountStep.Confirming }) { Text("Try Again") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        )
    }
}

private fun Throwable.toDeleteAccountErrorMessage(): String = when (this) {
    is FirebaseAuthRecentLoginRequiredException -> "Please sign in again to continue."
    is FirebaseNetworkException -> "Can't reach the server right now. Check your connection and try again."
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Can't reach the server right now. Check your connection and try again."
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "You don't have permission to do that."
        else -> "Something went wrong. Please try again."
    }
    else -> message ?: "Something went wrong. Please try again."
}
