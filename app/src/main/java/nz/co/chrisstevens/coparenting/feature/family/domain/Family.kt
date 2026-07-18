package nz.co.chrisstevens.coparenting.feature.family.domain

data class Family(
    val id: String = "",
    val joinCode: String = "",
    val ownerUid: String = "",
    val memberUids: List<String> = emptyList(),
    val createdAt: Long = 0L
)
