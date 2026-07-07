package com.example.co_parenting_calendar.feature.calendar.domain

import java.time.LocalDate
import java.util.UUID

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val title: String,
    val notes: String = ""
)
