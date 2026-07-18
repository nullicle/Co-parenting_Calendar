package com.example.co_parenting_calendar.feature.family.domain

data class Family(
    val id: String = "",
    val joinCode: String = "",
    val ownerUid: String = "",
    val memberUids: List<String> = emptyList(),
    val createdAt: Long = 0L
)
