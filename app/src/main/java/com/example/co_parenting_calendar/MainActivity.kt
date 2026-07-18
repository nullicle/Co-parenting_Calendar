package com.example.co_parenting_calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.co_parenting_calendar.core.designsystem.theme.Coparenting_CalendarTheme
import com.example.co_parenting_calendar.feature.activity.data.ActivityRepository
import com.example.co_parenting_calendar.feature.auth.data.AuthRepository
import com.example.co_parenting_calendar.feature.children.data.ChildRepository
import com.example.co_parenting_calendar.feature.family.data.FamilyRepository
import com.example.co_parenting_calendar.feature.parent.data.ParentAssignmentRepository
import com.example.co_parenting_calendar.feature.parent.data.ParentRepository
import com.example.co_parenting_calendar.feature.settings.data.DataBackupManager
import com.example.co_parenting_calendar.feature.settings.data.ThemePreference
import com.example.co_parenting_calendar.feature.settings.data.ThemePreferenceRepository

class MainActivity : ComponentActivity() {

    private val activityRepository by lazy { ActivityRepository(applicationContext) }
    private val childRepository by lazy { ChildRepository(applicationContext) }
    private val parentRepository by lazy { ParentRepository(applicationContext) }
    private val parentAssignmentRepository by lazy { ParentAssignmentRepository(applicationContext) }
    private val themePreferenceRepository by lazy { ThemePreferenceRepository(applicationContext) }
    private val authRepository by lazy { AuthRepository() }
    private val familyRepository by lazy { FamilyRepository() }
    private val dataBackupManager by lazy {
        DataBackupManager(
            context = applicationContext,
            activityRepository = activityRepository,
            childRepository = childRepository,
            parentRepository = parentRepository,
            parentAssignmentRepository = parentAssignmentRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val useDarkTheme = when (themePreferenceRepository.theme) {
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }
            Coparenting_CalendarTheme(darkTheme = useDarkTheme) {
                CoParentingCalendarApp(
                    activityRepository = activityRepository,
                    childRepository = childRepository,
                    parentRepository = parentRepository,
                    parentAssignmentRepository = parentAssignmentRepository,
                    themePreferenceRepository = themePreferenceRepository,
                    dataBackupManager = dataBackupManager,
                    authRepository = authRepository,
                    familyRepository = familyRepository
                )
            }
        }
    }
}
