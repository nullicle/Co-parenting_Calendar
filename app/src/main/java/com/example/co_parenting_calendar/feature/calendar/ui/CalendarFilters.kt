package com.example.co_parenting_calendar.feature.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.co_parenting_calendar.core.designsystem.CheckableFilterChip
import com.example.co_parenting_calendar.core.designsystem.ColorDotLabel
import com.example.co_parenting_calendar.feature.children.domain.Child

/**
 * Filter chips above the calendar. Each is independent: hiding "Activities" hides all of them
 * regardless of child filters; hiding a child only hides activities that have no other visible
 * child attached.
 */
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
            CheckableFilterChip(
                selected = showParentAssignments,
                onClick = onToggleParentAssignments,
                label = { Text("Parent assignments") }
            )
        }
        item {
            CheckableFilterChip(
                selected = showActivities,
                onClick = onToggleActivities,
                label = { Text("Activities") }
            )
        }
        items(children, key = { it.id }) { child ->
            CheckableFilterChip(
                selected = child.id !in hiddenChildIds,
                onClick = { onToggleChild(child.id) },
                label = { ColorDotLabel(child.colorArgb, child.name) }
            )
        }
    }
}
