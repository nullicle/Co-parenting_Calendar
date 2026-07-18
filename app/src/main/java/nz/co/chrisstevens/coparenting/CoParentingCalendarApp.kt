package nz.co.chrisstevens.coparenting

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import nz.co.chrisstevens.coparenting.core.designsystem.ErrorScreen
import nz.co.chrisstevens.coparenting.core.designsystem.LoadingScreen
import nz.co.chrisstevens.coparenting.core.util.enumSaver
import nz.co.chrisstevens.coparenting.feature.activity.data.ActivityRepository
import nz.co.chrisstevens.coparenting.feature.auth.data.AuthRepository
import nz.co.chrisstevens.coparenting.feature.auth.ui.SignInScreen
import nz.co.chrisstevens.coparenting.feature.calendar.ui.CalendarScreen
import nz.co.chrisstevens.coparenting.feature.children.data.ChildRepository
import nz.co.chrisstevens.coparenting.feature.children.ui.ChildrenScreen
import nz.co.chrisstevens.coparenting.feature.family.data.FamilyRepository
import nz.co.chrisstevens.coparenting.feature.family.data.toFamilyErrorMessage
import nz.co.chrisstevens.coparenting.feature.family.domain.Family
import nz.co.chrisstevens.coparenting.feature.family.ui.FamilyOnboardingFlow
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentAssignmentRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentRepository
import nz.co.chrisstevens.coparenting.feature.settings.data.DataBackupManager
import nz.co.chrisstevens.coparenting.feature.settings.data.ThemePreferenceRepository
import nz.co.chrisstevens.coparenting.feature.settings.ui.SettingsScreen

private enum class AppScreen { CALENDAR, SETTINGS, CHILDREN }

private sealed class FamilyCheckStatus {
    object Loading : FamilyCheckStatus()
    object NeedsOnboarding : FamilyCheckStatus()
    data class Ready(val family: Family) : FamilyCheckStatus()
    data class Error(val message: String) : FamilyCheckStatus()
}

/**
 * Gated twice: no signed-in Firebase user means SignInScreen, full stop. Once signed in, we
 * check Firestore for existing family membership before letting anyone into the app - a new
 * user (or one who left/reset last time) is routed into FamilyOnboardingFlow instead. Neither
 * gate touches the local JSON repositories.
 */
@Composable
fun CoParentingCalendarApp(
    activityRepository: ActivityRepository,
    childRepository: ChildRepository,
    parentRepository: ParentRepository,
    parentAssignmentRepository: ParentAssignmentRepository,
    themePreferenceRepository: ThemePreferenceRepository,
    dataBackupManager: DataBackupManager,
    authRepository: AuthRepository,
    familyRepository: FamilyRepository
) {
    val user = authRepository.currentUser
    if (user == null) {
        SignInScreen(authRepository = authRepository, modifier = Modifier.fillMaxSize())
        return
    }

    var familyStatus by remember(user.uid) { mutableStateOf<FamilyCheckStatus>(FamilyCheckStatus.Loading) }
    var retryTrigger by remember(user.uid) { mutableIntStateOf(0) }

    LaunchedEffect(user.uid, retryTrigger) {
        familyStatus = try {
            val family = familyRepository.findFamilyForUser(user.uid)
            if (family != null) FamilyCheckStatus.Ready(family) else FamilyCheckStatus.NeedsOnboarding
        } catch (e: Exception) {
            FamilyCheckStatus.Error(e.toFamilyErrorMessage())
        }
    }

    when (val status = familyStatus) {
        FamilyCheckStatus.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
        is FamilyCheckStatus.Error -> ErrorScreen(
            message = status.message,
            onRetry = { retryTrigger++ },
            modifier = Modifier.fillMaxSize()
        )
        FamilyCheckStatus.NeedsOnboarding -> FamilyOnboardingFlow(
            uid = user.uid,
            familyRepository = familyRepository,
            onFamilyReady = { family -> familyStatus = FamilyCheckStatus.Ready(family) },
            modifier = Modifier.fillMaxSize()
        )
        is FamilyCheckStatus.Ready -> {
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
                    activityRepository = activityRepository,
                    childRepository = childRepository,
                    parentRepository = parentRepository,
                    parentAssignmentRepository = parentAssignmentRepository,
                    themePreferenceRepository = themePreferenceRepository,
                    dataBackupManager = dataBackupManager,
                    authRepository = authRepository,
                    familyRepository = familyRepository,
                    family = status.family,
                    onBack = { screen = AppScreen.CALENDAR },
                    onOpenChildren = { screen = AppScreen.CHILDREN },
                    onFamilyDeleted = { familyStatus = FamilyCheckStatus.NeedsOnboarding },
                    modifier = Modifier.fillMaxSize()
                )
                AppScreen.CHILDREN -> ChildrenScreen(
                    childRepository = childRepository,
                    onBack = { screen = AppScreen.SETTINGS },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
