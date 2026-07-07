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
import com.example.co_parenting_calendar.feature.auth.data.AuthRepository
import com.example.co_parenting_calendar.feature.auth.ui.SignInScreen
import com.example.co_parenting_calendar.feature.calendar.ui.CalendarScreen
import com.example.co_parenting_calendar.feature.children.data.ChildRepository
import com.example.co_parenting_calendar.feature.children.ui.ChildrenScreen
import com.example.co_parenting_calendar.feature.parent.data.ParentAssignmentRepository
import com.example.co_parenting_calendar.feature.parent.data.ParentRepository
import com.example.co_parenting_calendar.feature.settings.data.DataBackupManager
import com.example.co_parenting_calendar.feature.settings.data.ThemePreferenceRepository
import com.example.co_parenting_calendar.feature.settings.ui.SettingsScreen

private enum class AppScreen { CALENDAR, SETTINGS, CHILDREN }

/**
 * Gated on sign-in first: no signed-in Firebase user means SignInScreen, full stop. Once
 * authRepository.currentUser flips to non-null (its AuthStateListener fires), this recomposes
 * straight into the normal three-screen app - no navigation call needed for that transition.
 */
@Composable
fun CoParentingCalendarApp(
    activityRepository: ActivityRepository,
    childRepository: ChildRepository,
    parentRepository: ParentRepository,
    parentAssignmentRepository: ParentAssignmentRepository,
    themePreferenceRepository: ThemePreferenceRepository,
    dataBackupManager: DataBackupManager,
    authRepository: AuthRepository
) {
    if (authRepository.currentUser == null) {
        SignInScreen(authRepository = authRepository, modifier = Modifier.fillMaxSize())
        return
    }

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
            parentRepository = parentRepository,
            themePreferenceRepository = themePreferenceRepository,
            dataBackupManager = dataBackupManager,
            authRepository = authRepository,
            onBack = { screen = AppScreen.CALENDAR },
            onOpenChildren = { screen = AppScreen.CHILDREN },
            modifier = Modifier.fillMaxSize()
        )
        AppScreen.CHILDREN -> ChildrenScreen(
            childRepository = childRepository,
            onBack = { screen = AppScreen.SETTINGS },
            modifier = Modifier.fillMaxSize()
        )
    }
}
