package com.example.co_parenting_calendar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.co_parenting_calendar.core.util.enumSaver
import com.example.co_parenting_calendar.feature.activity.data.ActivityRepository
import com.example.co_parenting_calendar.feature.calendar.ui.CalendarScreen
import com.example.co_parenting_calendar.feature.children.data.ChildRepository
import com.example.co_parenting_calendar.feature.children.ui.ChildrenScreen
import com.example.co_parenting_calendar.feature.parent.data.ParentAssignmentRepository
import com.example.co_parenting_calendar.feature.parent.data.ParentRepository
import com.example.co_parenting_calendar.feature.parent.ui.ParentManagementScreen
import com.example.co_parenting_calendar.feature.settings.ui.SettingsScreen

private enum class AppScreen { CALENDAR, SETTINGS, CHILDREN, PARENTS }

/**
 * Only four screens, each reachable from at most one other, so this is a hand-rolled
 * "back stack of depth 2" instead of pulling in Navigation Compose.
 */
@Composable
fun CoParentingCalendarApp(
    activityRepository: ActivityRepository,
    childRepository: ChildRepository,
    parentRepository: ParentRepository,
    parentAssignmentRepository: ParentAssignmentRepository
) {
    var screen by rememberSaveable(stateSaver = enumSaver()) { mutableStateOf(AppScreen.CALENDAR) }

    when (screen) {
        AppScreen.CALENDAR -> CalendarScreen(
            activityRepository = activityRepository,
            childRepository = childRepository,
            parentRepository = parentRepository,
            parentAssignmentRepository = parentAssignmentRepository,
            onOpenSettings = { screen = AppScreen.SETTINGS },
            modifier = Modifier.fillMaxSize()
        )
        AppScreen.SETTINGS -> SettingsScreen(
            onBack = { screen = AppScreen.CALENDAR },
            onOpenChildren = { screen = AppScreen.CHILDREN },
            onOpenParents = { screen = AppScreen.PARENTS },
            modifier = Modifier.fillMaxSize()
        )
        AppScreen.CHILDREN -> ChildrenScreen(
            childRepository = childRepository,
            onBack = { screen = AppScreen.SETTINGS },
            modifier = Modifier.fillMaxSize()
        )
        AppScreen.PARENTS -> ParentManagementScreen(
            parentRepository = parentRepository,
            onBack = { screen = AppScreen.SETTINGS },
            modifier = Modifier.fillMaxSize()
        )
    }
}
