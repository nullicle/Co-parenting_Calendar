package com.example.co_parenting_calendar.feature.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.co_parenting_calendar.feature.auth.data.AuthRepository
import kotlinx.coroutines.launch

/**
 * Self-contained "Sign in with Google" button - owns its own loading/error state so both the
 * SignInScreen and the Settings "not signed in" state can just drop it in without repeating
 * the Credential Manager plumbing.
 */
@Composable
fun GoogleSignInButton(authRepository: AuthRepository, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (errorMessage != null) {
            Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Button(
            enabled = !isLoading,
            onClick = {
                isLoading = true
                errorMessage = null
                scope.launch {
                    val result = authRepository.signInWithGoogle(context)
                    isLoading = false
                    result.onFailure {
                        errorMessage = it.message ?: "Sign-in failed. Please try again."
                    }
                }
            }
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign in with Google")
            }
        }
    }
}
