package nz.co.chrisstevens.coparenting.feature.activity.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsRugby
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.ui.graphics.vector.ImageVector
import nz.co.chrisstevens.coparenting.feature.activity.domain.ActivityIconType

val activityIconTypes: List<ActivityIconType> = ActivityIconType.entries

fun ActivityIconType.imageVector(): ImageVector = when (this) {
    ActivityIconType.SCHOOL -> Icons.Filled.School
    ActivityIconType.FOOTBALL -> Icons.Filled.SportsSoccer
    ActivityIconType.RUGBY -> Icons.Filled.SportsRugby
    ActivityIconType.BASKETBALL -> Icons.Filled.SportsBasketball
    ActivityIconType.SWIMMING -> Icons.Filled.Pool
    ActivityIconType.MUSIC -> Icons.Filled.MusicNote
    ActivityIconType.DANCE -> Icons.Filled.Theaters
    ActivityIconType.BIRTHDAY -> Icons.Filled.Cake
    ActivityIconType.DOCTOR -> Icons.Filled.LocalHospital
    ActivityIconType.DENTIST -> Icons.Filled.Healing
    ActivityIconType.HOLIDAY -> Icons.Filled.BeachAccess
    ActivityIconType.TRAVEL -> Icons.Filled.Flight
    ActivityIconType.HOME -> Icons.Filled.Home
    ActivityIconType.FAMILY -> Icons.Filled.Group
    ActivityIconType.OTHER -> Icons.Filled.MoreHoriz
}

fun ActivityIconType.displayName(): String = when (this) {
    ActivityIconType.SCHOOL -> "School"
    ActivityIconType.FOOTBALL -> "Football"
    ActivityIconType.RUGBY -> "Rugby"
    ActivityIconType.BASKETBALL -> "Basketball"
    ActivityIconType.SWIMMING -> "Swimming"
    ActivityIconType.MUSIC -> "Music"
    ActivityIconType.DANCE -> "Dance"
    ActivityIconType.BIRTHDAY -> "Birthday"
    ActivityIconType.DOCTOR -> "Doctor"
    ActivityIconType.DENTIST -> "Dentist"
    ActivityIconType.HOLIDAY -> "Holiday"
    ActivityIconType.TRAVEL -> "Travel"
    ActivityIconType.HOME -> "Home"
    ActivityIconType.FAMILY -> "Family"
    ActivityIconType.OTHER -> "Other"
}
