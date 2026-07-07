package com.example.co_parenting_calendar.feature.calendar.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean
)

private const val WEEKS_IN_GRID = 6
private const val DAYS_IN_WEEK = 7

/**
 * Fixed 6-week grid covering [yearMonth], padded with leading/trailing days
 * from adjacent months so the grid height never changes between months.
 */
fun generateMonthGrid(
    yearMonth: YearMonth,
    today: LocalDate = LocalDate.now(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
): List<CalendarDay> {
    val firstOfMonth = yearMonth.atDay(1)
    val leadingDays = (firstOfMonth.dayOfWeek.value - firstDayOfWeek.value + DAYS_IN_WEEK) % DAYS_IN_WEEK
    val gridStart = firstOfMonth.minusDays(leadingDays.toLong())
    return (0 until WEEKS_IN_GRID * DAYS_IN_WEEK).map { offset ->
        val date = gridStart.plusDays(offset.toLong())
        CalendarDay(
            date = date,
            isCurrentMonth = YearMonth.from(date) == yearMonth,
            isToday = date == today
        )
    }
}

/** The 7 days of the week in display order, starting at [firstDayOfWeek]. */
fun weekdayOrder(firstDayOfWeek: DayOfWeek): List<DayOfWeek> =
    (0 until DAYS_IN_WEEK).map { offset ->
        DayOfWeek.of((firstDayOfWeek.value - 1 + offset) % DAYS_IN_WEEK + 1)
    }
