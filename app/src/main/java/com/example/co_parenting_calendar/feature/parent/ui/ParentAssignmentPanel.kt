package com.example.co_parenting_calendar.feature.parent.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.co_parenting_calendar.core.designsystem.CheckableFilterChip
import com.example.co_parenting_calendar.core.designsystem.ColorDotLabel
import com.example.co_parenting_calendar.feature.parent.domain.Parent
import com.example.co_parenting_calendar.feature.parent.domain.ParentSlot

/**
 * Normal mode: just a button inviting you into Parent Assignment mode. Tapping calendar days
 * never reassigns a parent unless that mode is active - browsing and assigning are kept apart
 * on purpose so you can't accidentally repaint a day while just looking at the calendar.
 */
@Composable
fun ParentAssignmentPanel(
    isAssigning: Boolean,
    onStartAssigning: () -> Unit,
    onStopAssigning: () -> Unit,
    parents: List<Parent>,
    selectedSlot: ParentSlot,
    onSelectSlot: (ParentSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isAssigning) 2.dp else 0.dp),
        colors = if (isAssigning) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isAssigning) {
                Text(
                    text = "Parent Assignment Mode",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Tap a day to assign it to the selected parent.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    parents.forEach { parent ->
                        CheckableFilterChip(
                            selected = parent.slot == selectedSlot,
                            onClick = { onSelectSlot(parent.slot) },
                            label = { ColorDotLabel(parent.colorArgb, parent.name) }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onStopAssigning) { Text("Done") }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Browsing calendar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onStartAssigning) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Assign Parent")
                    }
                }
            }
        }
    }
}
