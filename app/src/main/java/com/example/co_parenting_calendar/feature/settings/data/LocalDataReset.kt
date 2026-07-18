package com.example.co_parenting_calendar.feature.settings.data

import com.example.co_parenting_calendar.feature.activity.data.ActivityRepository
import com.example.co_parenting_calendar.feature.children.data.ChildRepository
import com.example.co_parenting_calendar.feature.parent.data.ParentAssignmentRepository
import com.example.co_parenting_calendar.feature.parent.data.ParentRepository

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
