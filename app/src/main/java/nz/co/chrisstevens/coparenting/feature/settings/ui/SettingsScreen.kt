package nz.co.chrisstevens.coparenting.feature.settings.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.co.chrisstevens.coparenting.core.designsystem.ColorDotLabel
import nz.co.chrisstevens.coparenting.core.designsystem.SettingsSection
import nz.co.chrisstevens.coparenting.core.firebase.FirebaseConnectionTester
import nz.co.chrisstevens.coparenting.core.firebase.FirestoreTestResult
import nz.co.chrisstevens.coparenting.core.firebase.isFirebaseAppInitialized
import nz.co.chrisstevens.coparenting.feature.activity.data.ActivityRepository
import nz.co.chrisstevens.coparenting.feature.auth.data.AuthRepository
import nz.co.chrisstevens.coparenting.feature.auth.ui.GoogleSignInButton
import nz.co.chrisstevens.coparenting.feature.children.data.ChildRepository
import nz.co.chrisstevens.coparenting.feature.family.data.FamilyRepository
import nz.co.chrisstevens.coparenting.feature.family.data.toFamilyErrorMessage
import nz.co.chrisstevens.coparenting.feature.family.domain.Family
import nz.co.chrisstevens.coparenting.feature.family.ui.familyMemberLabels
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentAssignmentRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentRepository
import nz.co.chrisstevens.coparenting.feature.parent.domain.Parent
import nz.co.chrisstevens.coparenting.feature.parent.ui.ParentDialog
import nz.co.chrisstevens.coparenting.feature.settings.data.DataBackupManager
import nz.co.chrisstevens.coparenting.feature.settings.data.ThemePreference
import nz.co.chrisstevens.coparenting.feature.settings.data.ThemePreferenceRepository
import nz.co.chrisstevens.coparenting.feature.settings.data.clearAllLocalData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    activityRepository: ActivityRepository,
    childRepository: ChildRepository,
    parentRepository: ParentRepository,
    parentAssignmentRepository: ParentAssignmentRepository,
    themePreferenceRepository: ThemePreferenceRepository,
    dataBackupManager: DataBackupManager,
    authRepository: AuthRepository,
    familyRepository: FamilyRepository,
    family: Family,
    onBack: () -> Unit,
    onOpenChildren: () -> Unit,
    onFamilyDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var editingParent by remember { mutableStateOf<Parent?>(null) }
    var isLeavingFamily by remember { mutableStateOf(false) }
    var isResettingEverything by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching { dataBackupManager.exportTo(uri) }
                .onSuccess { Toast.makeText(context, "Data exported", Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show() }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching { dataBackupManager.importFrom(uri) }
                .onSuccess { Toast.makeText(context, "Data imported", Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show() }
        }
    }

    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "unknown"
    }

    val firebaseConnectionTester = remember { FirebaseConnectionTester() }
    val isFirebaseConnected = remember { isFirebaseAppInitialized() }
    var firestoreTestResult by remember { mutableStateOf(FirestoreTestResult.NOT_RUN) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsSection(title = "Account") {
                Column(modifier = Modifier.padding(16.dp)) {
                    val user = authRepository.currentUser
                    if (user != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (user.photoUrl != null) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Person, contentDescription = null)
                                }
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = user.displayName ?: "Signed in",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = user.email ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { authRepository.signOut() },
                            modifier = Modifier.padding(top = 12.dp)
                        ) { Text("Sign Out") }
                        OutlinedButton(
                            onClick = { showDeleteAccountDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(" Delete Account")
                        }
                    } else {
                        Text(
                            text = "Not signed in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        GoogleSignInButton(
                            authRepository = authRepository,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }

            SettingsSection(title = "Children & Parents") {
                ListItem(
                    headlineContent = { Text("Children") },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable(onClick = onOpenChildren)
                )
                HorizontalDivider()
                parentRepository.parents.forEachIndexed { index, parent ->
                    ListItem(
                        headlineContent = { ColorDotLabel(parent.colorArgb, parent.name) },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        },
                        modifier = Modifier.clickable { editingParent = parent }
                    )
                    if (index != parentRepository.parents.lastIndex) HorizontalDivider()
                }
            }

            SettingsSection(title = "Family") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Shared Calendar", style = MaterialTheme.typography.titleMedium)

                    Text(
                        text = "Invite code",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = family.joinCode,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(family.joinCode))
                            scope.launch { snackbarHostState.showSnackbar("Invite code copied.") }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(" Copy Invite Code")
                    }

                    Text(
                        text = "Members",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                    val memberLabels = familyMemberLabels(family.memberUids, authRepository.currentUser)
                    memberLabels.forEach { label ->
                        ListItem(
                            leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) },
                            headlineContent = { Text(label) }
                        )
                    }

                    OutlinedButton(
                        enabled = !isLeavingFamily,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.padding(top = 12.dp),
                        onClick = {
                            val uid = authRepository.currentUser?.uid ?: return@OutlinedButton
                            isLeavingFamily = true
                            scope.launch {
                                val result = familyRepository.leaveFamily(uid)
                                isLeavingFamily = false
                                result.onSuccess { onFamilyDeleted() }
                                    .onFailure {
                                        snackbarHostState.showSnackbar(it.toFamilyErrorMessage())
                                    }
                            }
                        }
                    ) {
                        if (isLeavingFamily) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Leave Family")
                        }
                    }
                }
            }

            SettingsSection(title = "Appearance") {
                ThemeOptionRow(
                    label = "Follow system",
                    selected = themePreferenceRepository.theme == ThemePreference.SYSTEM,
                    onClick = { themePreferenceRepository.setTheme(ThemePreference.SYSTEM) }
                )
                ThemeOptionRow(
                    label = "Light",
                    selected = themePreferenceRepository.theme == ThemePreference.LIGHT,
                    onClick = { themePreferenceRepository.setTheme(ThemePreference.LIGHT) }
                )
                ThemeOptionRow(
                    label = "Dark",
                    selected = themePreferenceRepository.theme == ThemePreference.DARK,
                    onClick = { themePreferenceRepository.setTheme(ThemePreference.DARK) }
                )
            }

            SettingsSection(title = "Data") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Save your activities, children, and parents to a file, or restore from one.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { exportLauncher.launch("coparenting-calendar-backup.json") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Export data") }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Import data") }
                    }
                }
            }

            SettingsSection(title = "About") {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CoParenting Calendar", style = MaterialTheme.typography.titleMedium)
                    Text("Version $versionName", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "A simple, focused calendar for separated parents to share their children's schedule.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Built with Jetpack Compose and Material 3.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            SettingsSection(title = "Firebase Status") {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FirebaseStatusRow(
                        label = "Firebase Connected",
                        value = if (isFirebaseConnected) "✅" else "❌"
                    )
                    FirebaseStatusRow(
                        label = "Signed In",
                        value = if (authRepository.currentUser != null) "✅" else "❌"
                    )
                    FirebaseStatusRow(
                        label = "Firestore Test",
                        value = when (firestoreTestResult) {
                            FirestoreTestResult.NOT_RUN -> "Not run"
                            FirestoreTestResult.RUNNING -> "Testing…"
                            FirestoreTestResult.PASS -> "Pass"
                            FirestoreTestResult.FAIL -> "Fail"
                        }
                    )
                    Button(
                        onClick = {
                            firestoreTestResult = FirestoreTestResult.RUNNING
                            firebaseConnectionTester.testConnection { result -> firestoreTestResult = result }
                        },
                        enabled = firestoreTestResult != FirestoreTestResult.RUNNING,
                        modifier = Modifier.padding(top = 8.dp)
                    ) { Text("Test Firebase Connection") }
                    Text(
                        text = "Development only - writes and reads back a test document. " +
                            "Remove this section once real syncing is implemented.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            SettingsSection(title = "Developer Tools") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "This section is temporary and intended only during development.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Wipes every local activity, child, parent, and parent assignment. " +
                            "You stay signed in and stay in the same family.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            clearAllLocalData(
                                activityRepository = activityRepository,
                                childRepository = childRepository,
                                parentRepository = parentRepository,
                                parentAssignmentRepository = parentAssignmentRepository
                            )
                            scope.launch { snackbarHostState.showSnackbar("Local data cleared.") }
                        }
                    ) { Text("Reset Local Data") }

                    Text(
                        text = "Leaves the current family and wipes all local data - as if the app " +
                            "were freshly installed, while staying signed in.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                    )
                    Button(
                        enabled = !isResettingEverything,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            val uid = authRepository.currentUser?.uid ?: return@Button
                            isResettingEverything = true
                            scope.launch {
                                val result = familyRepository.leaveFamily(uid)
                                isResettingEverything = false
                                result.onSuccess {
                                    clearAllLocalData(
                                        activityRepository = activityRepository,
                                        childRepository = childRepository,
                                        parentRepository = parentRepository,
                                        parentAssignmentRepository = parentAssignmentRepository
                                    )
                                    onFamilyDeleted()
                                }.onFailure {
                                    snackbarHostState.showSnackbar(it.toFamilyErrorMessage())
                                }
                            }
                        }
                    ) {
                        if (isResettingEverything) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onError
                            )
                        } else {
                            Text("Leave Family and Reset Everything")
                        }
                    }
                }
            }
        }
    }

    editingParent?.let { parent ->
        ParentDialog(
            parent = parent,
            onDismiss = { editingParent = null },
            onSave = { updated ->
                parentRepository.updateParent(updated)
                editingParent = null
            }
        )
    }

    if (showDeleteAccountDialog) {
        val uid = authRepository.currentUser?.uid
        if (uid != null) {
            DeleteAccountDialog(
                uid = uid,
                authRepository = authRepository,
                familyRepository = familyRepository,
                activityRepository = activityRepository,
                childRepository = childRepository,
                parentRepository = parentRepository,
                parentAssignmentRepository = parentAssignmentRepository,
                onDismiss = { showDeleteAccountDialog = false }
            )
        }
    }
}

@Composable
private fun FirebaseStatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ThemeOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
