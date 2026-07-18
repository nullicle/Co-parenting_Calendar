package nz.co.chrisstevens.coparenting.feature.calendar.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class MonthGridCalculatorTest {

    @Test
    fun `grid always contains 42 days`() {
        val days = generateMonthGrid(
            yearMonth = YearMonth.of(2026, 7),
            today = LocalDate.of(2026, 7, 7),
            firstDayOfWeek = DayOfWeek.MONDAY
        )
        assertEquals(42, days.size)
    }

    @Test
    fun `first day of month lands in the correct grid column`() {
        // July 1 2026 is a Wednesday; a Monday-start grid places it at index 2.
        val days = generateMonthGrid(
            yearMonth = YearMonth.of(2026, 7),
            today = LocalDate.of(2026, 7, 7),
            firstDayOfWeek = DayOfWeek.MONDAY
        )
        assertEquals(DayOfWeek.WEDNESDAY, LocalDate.of(2026, 7, 1).dayOfWeek)
        assertEquals(2, days.indexOfFirst { it.date == LocalDate.of(2026, 7, 1) })
    }

    @Test
    fun `leap year february 2024 marks all 29 days as current month`() {
        val days = generateMonthGrid(
            yearMonth = YearMonth.of(2024, 2),
            today = LocalDate.of(2024, 2, 1),
            firstDayOfWeek = DayOfWeek.MONDAY
        )
        assertEquals(29, days.count { it.isCurrentMonth })
    }

    @Test
    fun `today flag is set on exactly one matching cell`() {
        val today = LocalDate.of(2026, 7, 7)
        val days = generateMonthGrid(
            yearMonth = YearMonth.of(2026, 7),
            today = today,
            firstDayOfWeek = DayOfWeek.MONDAY
        )
        val todayCells = days.filter { it.isToday }
        assertEquals(1, todayCells.size)
        assertEquals(today, todayCells.first().date)
    }

    @Test
    fun `leading days from the previous month are marked as not current month`() {
        val days = generateMonthGrid(
            yearMonth = YearMonth.of(2026, 7),
            today = LocalDate.of(2026, 7, 7),
            firstDayOfWeek = DayOfWeek.MONDAY
        )
        val leading = days.take(2)
        assertTrue(leading.all { !it.isCurrentMonth })
        assertEquals(LocalDate.of(2026, 6, 29), leading[0].date)
        assertEquals(LocalDate.of(2026, 6, 30), leading[1].date)
    }

    @Test
    fun `weekdayOrder starting sunday returns sunday through saturday`() {
        assertEquals(
            listOf(
                DayOfWeek.SUNDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
            ),
            weekdayOrder(DayOfWeek.SUNDAY)
        )
    }
}
