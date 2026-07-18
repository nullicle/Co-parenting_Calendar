package nz.co.chrisstevens.coparenting.feature.parent.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import nz.co.chrisstevens.coparenting.core.designsystem.ColorSwatchPicker
import nz.co.chrisstevens.coparenting.feature.parent.domain.Parent

@Composable
fun ParentDialog(
    parent: Parent,
    onDismiss: () -> Unit,
    onSave: (Parent) -> Unit
) {
    var name by remember { mutableStateOf(parent.name) }
    var selectedColor by remember { mutableStateOf(parent.colorArgb) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit parent") },
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
            Button(
                enabled = name.isNotBlank(),
                onClick = { onSave(parent.copy(name = name.trim(), colorArgb = selectedColor)) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
