package com.example.co_parenting_calendar.feature.children.domain

import java.util.UUID

data class Child(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorArgb: Long
)
