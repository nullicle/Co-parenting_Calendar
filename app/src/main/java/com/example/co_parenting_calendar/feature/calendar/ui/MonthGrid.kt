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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import com.example.co_parenting_calendar.feature.calendar.domain.CalendarDay
import com.example.co_parenting_calendar.feature.calendar.domain.weekdayOrder
import com.example.co_parenting_calendar.feature.children.domain.Child
import com.example.co_parenting_calendar.feature.parent.domain.Parent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val DAYS_IN_WEEK = 7
private const val MAX_STARS = 2

@Composable
fun MonthGrid(
    days: List<CalendarDay>,
    firstDayOfWeek: DayOfWeek,
    selectedDate: LocalDate,
    activitiesByDate: Map<LocalDate, List<Activity>>,
    children: List<Child>,
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
                        children = children,
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
    children: List<Child>,
    owningParent: Parent?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parentTint = owningParent?.let { Color(it.colorArgb) }
    val starColors = distinctChildColors(activities, children)

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

        if (starColors.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 3.dp, bottom = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                starColors.forEach { colorArgb ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(colorArgb),
                        modifier = Modifier.size(9.dp)
                    )
                }
            }
        }
    }
}

/**
 * One star per distinct child with an activity that day, coloured with that child's colour,
 * capped at [MAX_STARS] - drawn after (so on top of) the parent tint/bar above.
 */
private fun distinctChildColors(activities: List<Activity>, children: List<Child>): List<Long> {
    val orderedChildIds = LinkedHashSet<String>()
    activities.forEach { activity -> orderedChildIds.addAll(activity.childIds) }
    return orderedChildIds
        .mapNotNull { childId -> children.find { it.id == childId }?.colorArgb }
        .take(MAX_STARS)
}
