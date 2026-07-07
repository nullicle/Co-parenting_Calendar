package com.example.co_parenting_calendar.feature.activity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.co_parenting_calendar.core.designsystem.AppTimePickerDialog
import com.example.co_parenting_calendar.core.designsystem.CheckableFilterChip
import com.example.co_parenting_calendar.feature.activity.domain.Activity
import com.example.co_parenting_calendar.feature.activity.domain.ActivityIconType
import com.example.co_parenting_calendar.feature.activity.domain.RepeatRule
import com.example.co_parenting_calendar.feature.children.domain.Child
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

@Composable
fun ActivityDialog(
    date: LocalDate,
    initialActivity: Activity?,
    children: List<Child>,
    onDismiss: () -> Unit,
    onSave: (Activity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialActivity?.title ?: "") }
    var location by remember { mutableStateOf(initialActivity?.location ?: "") }
    var notes by remember { mutableStateOf(initialActivity?.notes ?: "") }
    var startTime by remember { mutableStateOf(initialActivity?.startTime ?: LocalTime.of(9, 0)) }
    var endTime by remember { mutableStateOf(initialActivity?.endTime) }
    var icon by remember { mutableStateOf(initialActivity?.icon ?: ActivityIconType.OTHER) }
    var spanDays by remember {
        mutableStateOf(
            initialActivity?.let { ChronoUnit.DAYS.between(it.date, it.endDate).toInt() + 1 } ?: 1
        )
    }
    var repeat by remember { mutableStateOf(initialActivity?.repeat ?: RepeatRule.NEVER) }
    val selectedChildIds = remember {
        mutableStateListOf<String>().apply { addAll(initialActivity?.childIds ?: emptyList()) }
    }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialActivity == null) "Add activity" else "Edit activity") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )

                ActivityIconPicker(
                    selectedIcon = icon,
                    onIconSelected = { icon = it },
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { showStartTimePicker = true }) {
                        Text("Start: ${startTime.format(timeFormatter)}")
                    }
                    if (endTime != null) {
                        TextButton(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("End: ${endTime!!.format(timeFormatter)}")
                        }
                        IconButton(onClick = { endTime = null }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear end time")
                        }
                    } else {
                        TextButton(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("+ End time")
                        }
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Spans multiple days", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = spanDays > 1,
                        onCheckedChange = { spanning -> spanDays = if (spanning) 2 else 1 }
                    )
                }
                if (spanDays > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Number of days", style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { if (spanDays > 2) spanDays -= 1 }) { Text("−") }
                            Text("$spanDays", modifier = Modifier.padding(horizontal = 4.dp))
                            TextButton(onClick = { spanDays += 1 }) { Text("+") }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Repeat",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RepeatRule.entries.forEach { option ->
                        CheckableFilterChip(
                            selected = repeat == option,
                            onClick = { repeat = option },
                            label = { Text(option.displayName()) }
                        )
                    }
                }

                if (children.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Text("Children", modifier = Modifier.padding(bottom = 4.dp))
                    children.forEach { child ->
                        val checked = selectedChildIds.contains(child.id)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) selectedChildIds.add(child.id)
                                    else selectedChildIds.remove(child.id)
                                }
                            )
                            Text(child.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(
                        Activity(
                            id = initialActivity?.id ?: UUID.randomUUID().toString(),
                            date = date,
                            endDate = date.plusDays((spanDays - 1).toLong()),
                            startTime = startTime,
                            endTime = endTime,
                            title = title.trim(),
                            location = location.trim(),
                            notes = notes.trim(),
                            childIds = selectedChildIds.toList(),
                            repeat = repeat,
                            icon = icon
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )

    if (showStartTimePicker) {
        AppTimePickerDialog(
            initialTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                startTime = it
                showStartTimePicker = false
            }
        )
    }
    if (showEndTimePicker) {
        AppTimePickerDialog(
            initialTime = endTime ?: startTime,
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                endTime = it
                showEndTimePicker = false
            }
        )
    }
}

private fun RepeatRule.displayName(): String = when (this) {
    RepeatRule.NEVER -> "Never"
    RepeatRule.WEEKLY -> "Weekly"
    RepeatRule.FORTNIGHTLY -> "Fortnightly"
}
