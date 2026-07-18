package nz.co.chrisstevens.coparenting.feature.calendar.ui

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nz.co.chrisstevens.coparenting.feature.activity.domain.Activity
import nz.co.chrisstevens.coparenting.feature.activity.ui.imageVector
import nz.co.chrisstevens.coparenting.feature.children.domain.Child
import nz.co.chrisstevens.coparenting.feature.parent.domain.Parent
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
    onDeleteActivity: (Activity) -> Unit,
    onAddActivityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Parent",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (parent != null) {
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(12.dp)
                            .background(Color(parent.colorArgb), CircleShape)
                    )
                    Text(
                        text = " ${parent.name}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = " Not set",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Activities",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
            )
            val sortedActivities = activities.sortedBy { it.startTime }
            if (sortedActivities.isEmpty()) {
                Text(
                    text = "No activities",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                sortedActivities.forEachIndexed { index, activity ->
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onActivityClick(activity) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = activity.icon.imageVector(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .weight(1f)
                        ) {
                            Text(
                                text = "$timeLabel  ${activity.title}",
                                style = MaterialTheme.typography.bodyLarge
                            )
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
                        IconButton(onClick = { onDeleteActivity(activity) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete activity",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (index != sortedActivities.lastIndex) HorizontalDivider()
                }
            }

            Button(
                onClick = onAddActivityClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Add Activity")
            }
        }
    }
}
