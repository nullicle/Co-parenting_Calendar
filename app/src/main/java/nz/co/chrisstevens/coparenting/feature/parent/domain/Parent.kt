package nz.co.chrisstevens.coparenting.feature.parent.domain

/** There are always exactly two parents, identified by a fixed slot - never added to or removed. */
enum class ParentSlot { ONE, TWO }

data class Parent(
    val slot: ParentSlot,
    val name: String,
    val colorArgb: Long
)
