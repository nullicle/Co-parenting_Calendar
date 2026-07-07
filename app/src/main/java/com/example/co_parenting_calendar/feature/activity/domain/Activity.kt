package com.example.co_parenting_calendar.feature.activity.domain

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

enum class RepeatRule { NEVER, WEEKLY, FORTNIGHTLY }

data class Activity(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val endDate: LocalDate = date,
    val startTime: LocalTime,
    val endTime: LocalTime? = null,
    val title: String,
    val location: String = "",
    val notes: String = "",
    val childIds: List<String> = emptyList(),
    val repeat: RepeatRule = RepeatRule.NEVER
)
