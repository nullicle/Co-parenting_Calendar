package com.example.co_parenting_calendar.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.co_parenting_calendar.feature.activity.domain.Activity
import com.example.co_parenting_calendar.feature.activity.ui.imageVector
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
    activitiesByDate: Map<LocalDate, List<Activity>>,
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
                        activities = activitiesByDate[day.date].orEmpty(),
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
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    activities: List<Activity>,
    owningParent: Parent?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parentTint = owningParent?.let { Color(it.colorArgb) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = parentTint?.copy(alpha = 0.14f) ?: Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val circleModifier = when {
                day.isToday -> Modifier
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                isSelected -> Modifier
                    .size(34.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else -> Modifier.size(34.dp)
            }
            Box(modifier = circleModifier, contentAlignment = Alignment.Center) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = when {
                        day.isToday -> MaterialTheme.colorScheme.onPrimary
                        day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                    fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            ActivityIndicator(
                activities = activities,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        if (parentTint != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(parentTint, RoundedCornerShape(2.dp))
            )
        }
    }
}

/** One activity shows its own icon; two or more show a small dot cluster instead - full icons at this size get cluttered. */
@Composable
private fun ActivityIndicator(activities: List<Activity>, modifier: Modifier = Modifier) {
    when {
        activities.isEmpty() -> Box(modifier = modifier.size(10.dp))
        activities.size == 1 -> Icon(
            imageVector = activities.first().icon.imageVector(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = modifier.size(12.dp)
        )
        else -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(minOf(activities.size, 3)) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                )
            }
        }
    }
}
