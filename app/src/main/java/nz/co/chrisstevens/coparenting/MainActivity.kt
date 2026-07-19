package nz.co.chrisstevens.coparenting

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import nz.co.chrisstevens.coparenting.core.designsystem.theme.Coparenting_CalendarTheme
import nz.co.chrisstevens.coparenting.core.notifications.NotificationTokenRepository
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
    private val notificationTokenRepository by lazy { NotificationTokenRepository() }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
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
                    familyRepository = familyRepository,
                    notificationTokenRepository = notificationTokenRepository
                )
            }
        }
    }

    /** POST_NOTIFICATIONS only exists as a runtime permission from Android 13 (API 33) onward. */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
