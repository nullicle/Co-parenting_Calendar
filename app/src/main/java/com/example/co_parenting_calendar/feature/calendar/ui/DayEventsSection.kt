package com.example.co_parenting_calendar.feature.calendar.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.co_parenting_calendar.feature.calendar.domain.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DayEventsSection(
    date: LocalDate,
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        if (events.isEmpty()) {
            Text(
                text = "No events",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            events.forEach { event ->
                ListItem(
                    headlineContent = { Text(event.title) },
                    supportingContent = { if (event.notes.isNotBlank()) Text(event.notes) },
                    modifier = Modifier.clickable { onEventClick(event) }
                )
                HorizontalDivider()
            }
        }
    }
}
