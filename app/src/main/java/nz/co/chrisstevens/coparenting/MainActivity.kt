package nz.co.chrisstevens.coparenting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import nz.co.chrisstevens.coparenting.core.designsystem.theme.Coparenting_CalendarTheme
import nz.co.chrisstevens.coparenting.feature.activity.data.ActivityRepository
import nz.co.chrisstevens.coparenting.feature.auth.data.AuthRepository
import nz.co.chrisstevens.coparenting.feature.children.data.ChildRepository
import nz.co.chrisstevens.coparenting.feature.family.data.FamilyRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentAssignmentRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentRepository
import nz.co.chrisstevens.coparenting.feature.settings.data.ThemePreference
import nz.co.chrisstevens.coparenting.feature.settings.data.ThemePreferenceRepository

class MainActivity : ComponentActivity() {

    private val activityRepository by lazy { ActivityRepository() }
    private val childRepository by lazy { ChildRepository() }
    private val parentRepository by lazy { ParentRepository() }
    private val parentAssignmentRepository by lazy { ParentAssignmentRepository() }
    private val themePreferenceRepository by lazy { ThemePreferenceRepository(applicationContext) }
    private val authRepository by lazy { AuthRepository() }
    private val familyRepository by lazy { FamilyRepository() }

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
                    authRepository = authRepository,
                    familyRepository = familyRepository
                )
            }
        }
    }
}
