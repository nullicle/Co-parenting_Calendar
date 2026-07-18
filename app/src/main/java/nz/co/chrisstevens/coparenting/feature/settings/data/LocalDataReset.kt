package nz.co.chrisstevens.coparenting.feature.settings.data

import nz.co.chrisstevens.coparenting.feature.activity.data.ActivityRepository
import nz.co.chrisstevens.coparenting.feature.children.data.ChildRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentAssignmentRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentRepository

/**
 * Wipes every local JSON repository (parents reset to their two defaults, since the app assumes
 * exactly two always exist). Only ever called from the developer tools in Settings - never part
 * of normal app flow, and never touches Firebase/Firestore.
 */
fun clearAllLocalData(
    activityRepository: ActivityRepository,
    childRepository: ChildRepository,
    parentRepository: ParentRepository,
    parentAssignmentRepository: ParentAssignmentRepository
) {
    activityRepository.clear()
    childRepository.clear()
    parentAssignmentRepository.clear()
    parentRepository.resetToDefaults()
}
