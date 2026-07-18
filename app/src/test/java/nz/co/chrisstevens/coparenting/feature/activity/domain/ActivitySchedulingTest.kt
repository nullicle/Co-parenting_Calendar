package nz.co.chrisstevens.coparenting.feature.activity.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ActivitySchedulingTest {

    private fun activity(
        date: LocalDate,
        endDate: LocalDate = date,
        repeat: RepeatRule = RepeatRule.NEVER
    ) = Activity(
        date = date,
        endDate = endDate,
        startTime = LocalTime.of(9, 0),
        title = "Test",
        repeat = repeat
    )

    @Test
    fun `single day non-repeating activity occurs only on its date`() {
        val start = LocalDate.of(2026, 7, 6)
        val template = activity(date = start)

        assertTrue(activitiesOn(start, listOf(template)).isNotEmpty())
        assertTrue(activitiesOn(start.minusDays(1), listOf(template)).isEmpty())
        assertTrue(activitiesOn(start.plusDays(1), listOf(template)).isEmpty())
    }

    @Test
    fun `multi-day non-repeating activity occurs on every day of its span`() {
        val start = LocalDate.of(2026, 7, 6)
        val end = start.plusDays(2)
        val template = activity(date = start, endDate = end)

        assertTrue(activitiesOn(start, listOf(template)).isNotEmpty())
        assertTrue(activitiesOn(start.plusDays(1), listOf(template)).isNotEmpty())
        assertTrue(activitiesOn(end, listOf(template)).isNotEmpty())
        assertTrue(activitiesOn(start.minusDays(1), listOf(template)).isEmpty())
        assertTrue(activitiesOn(end.plusDays(1), listOf(template)).isEmpty())
    }

    @Test
    fun `weekly repeat occurs every 7 days indefinitely`() {
        val start = LocalDate.of(2026, 7, 6)
        val template = activity(date = start, repeat = RepeatRule.WEEKLY)

        assertTrue(activitiesOn(start, listOf(template)).isNotEmpty())
        assertTrue(activitiesOn(start.plusDays(7), listOf(template)).isNotEmpty())
        assertTrue(activitiesOn(start.plusDays(14), listOf(template)).isNotEmpty())
        assertFalse(activitiesOn(start.plusDays(3), listOf(template)).isNotEmpty())
    }

    @Test
    fun `fortnightly repeat occurs every 14 days`() {
        val start = LocalDate.of(2026, 7, 6)
        val template = activity(date = start, repeat = RepeatRule.FORTNIGHTLY)

        assertTrue(activitiesOn(start, listOf(template)).isNotEmpty())
        assertTrue(activitiesOn(start.plusDays(14), listOf(template)).isNotEmpty())
        assertFalse(activitiesOn(start.plusDays(7), listOf(template)).isNotEmpty())
    }

    @Test
    fun `repeating activity does not occur before its start date`() {
        val start = LocalDate.of(2026, 7, 6)
        val template = activity(date = start, repeat = RepeatRule.WEEKLY)

        assertFalse(activitiesOn(start.minusDays(7), listOf(template)).isNotEmpty())
        assertFalse(activitiesOn(start.minusDays(1), listOf(template)).isNotEmpty())
    }

    @Test
    fun `multi-day activity repeating weekly covers each occurrence span but not the gap`() {
        val start = LocalDate.of(2026, 7, 6)
        val end = start.plusDays(2) // 3-day span
        val template = activity(date = start, endDate = end, repeat = RepeatRule.WEEKLY)

        // second occurrence, middle day
        assertTrue(activitiesOn(start.plusDays(8), listOf(template)).isNotEmpty())
        // gap day between the first span's end and the second occurrence's start
        assertFalse(activitiesOn(start.plusDays(4), listOf(template)).isNotEmpty())
    }

    @Test
    fun `activitiesOn returns every template that occurs on the date`() {
        val date = LocalDate.of(2026, 7, 6)
        val first = activity(date = date)
        val second = activity(date = date)

        assertEquals(2, activitiesOn(date, listOf(first, second)).size)
    }
}
