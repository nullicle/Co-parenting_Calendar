package nz.co.chrisstevens.coparenting.feature.children.domain

import java.util.UUID

data class Child(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorArgb: Long
)
