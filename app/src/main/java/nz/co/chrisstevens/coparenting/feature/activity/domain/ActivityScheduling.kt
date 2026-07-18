package nz.co.chrisstevens.coparenting.feature.activity.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Expands repeating/multi-day activity templates into the activities visible on [date].
 * Repeats continue indefinitely and there's no per-occurrence editing yet, so every occurrence
 * on a given day is just the stored template itself.
 */
fun activitiesOn(date: LocalDate, templates: List<Activity>): List<Activity> =
    templates.filter { it.occursOn(date) }

private fun Activity.occursOn(date: LocalDate): Boolean {
    if (repeat == RepeatRule.NEVER) {
        return !date.isBefore(this.date) && !date.isAfter(endDate)
    }

    val period = if (repeat == RepeatRule.WEEKLY) 7L else 14L
    val spanDays = ChronoUnit.DAYS.between(this.date, endDate)
    val deltaDays = ChronoUnit.DAYS.between(this.date, date)
    if (deltaDays < 0) return false

    // A span can overlap into the next period, so check every candidate occurrence index k
    // whose range [k*period, k*period+spanDays] could possibly contain deltaDays.
    val kFrom = maxOf(0L, Math.floorDiv(deltaDays - spanDays, period))
    val kTo = Math.floorDiv(deltaDays, period)
    for (k in kFrom..kTo) {
        val occurrenceStart = k * period
        val occurrenceEnd = occurrenceStart + spanDays
        if (deltaDays in occurrenceStart..occurrenceEnd) return true
    }
    return false
}
