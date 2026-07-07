package com.example.co_parenting_calendar.feature.children.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.co_parenting_calendar.core.designsystem.ColorSwatchPicker
import com.example.co_parenting_calendar.core.designsystem.colorPalette
import com.example.co_parenting_calendar.feature.children.domain.Child
import java.util.UUID

@Composable
fun ChildDialog(
    initialChild: Child?,
    onDismiss: () -> Unit,
    onSave: (Child) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialChild?.name ?: "") }
    var selectedColor by remember { mutableStateOf(initialChild?.colorArgb ?: colorPalette.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialChild == null) "Add child" else "Edit child") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Colour",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
                ColorSwatchPicker(selectedColor = selectedColor, onColorSelected = { selectedColor = it })
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        Child(
                            id = initialChild?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            colorArgb = selectedColor
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Delete") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
