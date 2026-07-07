package com.example.co_parenting_calendar.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.co_parenting_calendar.feature.activity.domain.Activity
import com.example.co_parenting_calendar.feature.children.domain.Child
import com.example.co_parenting_calendar.feature.parent.domain.Parent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm")

@Composable
fun DaySummarySection(
    date: LocalDate,
    parent: Parent?,
    activities: List<Activity>,
    children: List<Child>,
    onActivityClick: (Activity) -> Unit,
    onAddActivityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Parent: ", style = MaterialTheme.typography.bodyMedium)
                if (parent != null) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(parent.colorArgb), CircleShape)
                    )
                    Text(text = " ${parent.name}", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(
                        text = "Not set",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Activities",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            val sortedActivities = activities.sortedBy { it.startTime }
            if (sortedActivities.isEmpty()) {
                Text(
                    text = "No activities",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sortedActivities.forEach { activity ->
                    val timeLabel = if (activity.endTime != null) {
                        "${activity.startTime.format(TIME_FORMATTER)}–${activity.endTime.format(TIME_FORMATTER)}"
                    } else {
                        activity.startTime.format(TIME_FORMATTER)
                    }
                    val childNames = children.filter { it.id in activity.childIds }.map { it.name }
                    val childrenLabel = when {
                        childNames.isEmpty() -> null
                        childNames.size == 1 -> "Child: ${childNames.first()}"
                        else -> "Children: ${childNames.joinToString(", ")}"
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onActivityClick(activity) }
                            .padding(vertical = 8.dp)
                    ) {
                        Row {
                            Text(
                                text = timeLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(activity.title, style = MaterialTheme.typography.bodyLarge)
                        }
                        if (childrenLabel != null) {
                            Text(
                                text = childrenLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (activity.location.isNotBlank()) {
                            Text(
                                text = activity.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            TextButton(
                onClick = onAddActivityClick,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Add Activity")
            }
        }
    }
}
