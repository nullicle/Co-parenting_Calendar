package nz.co.chrisstevens.coparenting.feature.family.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import nz.co.chrisstevens.coparenting.feature.family.data.FamilyRepository
import nz.co.chrisstevens.coparenting.feature.family.domain.Family

private enum class OnboardingMode { CHOICE, CREATE, JOIN }

/**
 * Shown to a signed-in user who doesn't belong to a family yet. Three simple states hand-rolled
 * with an enum - same "no Navigation Compose" pattern as the rest of the app.
 */
@Composable
fun FamilyOnboardingFlow(
    uid: String,
    familyRepository: FamilyRepository,
    onFamilyReady: (Family) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(OnboardingMode.CHOICE) }

    when (mode) {
        OnboardingMode.CHOICE -> FamilyChoiceScreen(
            onCreateClick = { mode = OnboardingMode.CREATE },
            onJoinClick = { mode = OnboardingMode.JOIN },
            modifier = modifier
        )
        OnboardingMode.CREATE -> CreateFamilyScreen(
            uid = uid,
            familyRepository = familyRepository,
            onBack = { mode = OnboardingMode.CHOICE },
            onContinue = onFamilyReady,
            modifier = modifier
        )
        OnboardingMode.JOIN -> JoinFamilyScreen(
            uid = uid,
            familyRepository = familyRepository,
            onBack = { mode = OnboardingMode.CHOICE },
            onFamilyJoined = onFamilyReady,
            modifier = modifier
        )
    }
}
