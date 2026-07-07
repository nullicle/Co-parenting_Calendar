package com.example.co_parenting_calendar.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.co_parenting_calendar.feature.calendar.domain.CalendarDay
import com.example.co_parenting_calendar.feature.calendar.domain.weekdayOrder
import com.example.co_parenting_calendar.feature.parent.domain.Parent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val DAYS_IN_WEEK = 7

@Composable
fun MonthGrid(
    days: List<CalendarDay>,
    firstDayOfWeek: DayOfWeek,
    selectedDate: LocalDate,
    datesWithActivities: Set<LocalDate>,
    parentAssignments: Map<LocalDate, Parent>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        WeekdayHeader(firstDayOfWeek)
        days.chunked(DAYS_IN_WEEK).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        isSelected = day.date == selectedDate,
                        hasActivity = datesWithActivities.contains(day.date),
                        owningParent = parentAssignments[day.date],
                        onClick = { onDayClick(day.date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeader(firstDayOfWeek: DayOfWeek, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        weekdayOrder(firstDayOfWeek).forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    hasActivity: Boolean,
    owningParent: Parent?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = owningParent?.let { Color(it.colorArgb) }?.copy(alpha = 0.18f) ?: Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val circleModifier = when {
                day.isToday -> Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                isSelected -> Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                else -> Modifier.size(32.dp)
            }
            Box(modifier = circleModifier, contentAlignment = Alignment.Center) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = when {
                        day.isToday -> MaterialTheme.colorScheme.onPrimary
                        day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(6.dp)
                    .background(
                        color = if (hasActivity) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                        shape = CircleShape
                    )
            )
        }
    }
}
