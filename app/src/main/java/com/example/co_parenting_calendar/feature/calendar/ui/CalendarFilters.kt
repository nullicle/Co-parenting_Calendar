package com.example.co_parenting_calendar.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.co_parenting_calendar.feature.children.domain.Child

/**
 * Filter chips above the calendar. Each is independent: hiding "Activities" hides all of them
 * regardless of child filters; hiding a child only hides activities that have no other visible
 * child attached.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarFilters(
    showParentAssignments: Boolean,
    onToggleParentAssignments: () -> Unit,
    showActivities: Boolean,
    onToggleActivities: () -> Unit,
    children: List<Child>,
    hiddenChildIds: Set<String>,
    onToggleChild: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        item {
            FilterChip(
                selected = showParentAssignments,
                onClick = onToggleParentAssignments,
                label = { Text("Parent assignments") }
            )
        }
        item {
            FilterChip(
                selected = showActivities,
                onClick = onToggleActivities,
                label = { Text("Activities") }
            )
        }
        items(children, key = { it.id }) { child ->
            FilterChip(
                selected = child.id !in hiddenChildIds,
                onClick = { onToggleChild(child.id) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(child.colorArgb), CircleShape)
                        )
                        Box(modifier = Modifier.width(6.dp))
                        Text(child.name)
                    }
                }
            )
        }
    }
}
