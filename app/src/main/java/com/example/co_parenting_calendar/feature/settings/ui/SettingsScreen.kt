package com.example.co_parenting_calendar.feature.settings.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.co_parenting_calendar.core.designsystem.ColorDotLabel
import com.example.co_parenting_calendar.core.designsystem.SettingsSection
import com.example.co_parenting_calendar.feature.parent.data.ParentRepository
import com.example.co_parenting_calendar.feature.parent.domain.Parent
import com.example.co_parenting_calendar.feature.parent.ui.ParentDialog
import com.example.co_parenting_calendar.feature.settings.data.DataBackupManager
import com.example.co_parenting_calendar.feature.settings.data.ThemePreference
import com.example.co_parenting_calendar.feature.settings.data.ThemePreferenceRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    parentRepository: ParentRepository,
    themePreferenceRepository: ThemePreferenceRepository,
    dataBackupManager: DataBackupManager,
    onBack: () -> Unit,
    onOpenChildren: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var editingParent by remember { mutableStateOf<Parent?>(null) }

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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsSection(title = "Family") {
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
