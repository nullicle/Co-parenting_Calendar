package nz.co.chrisstevens.coparenting.feature.calendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nz.co.chrisstevens.coparenting.core.util.enumSaver
import nz.co.chrisstevens.coparenting.feature.activity.data.ActivityRepository
import nz.co.chrisstevens.coparenting.feature.activity.domain.Activity
import nz.co.chrisstevens.coparenting.feature.activity.domain.activitiesOn
import nz.co.chrisstevens.coparenting.feature.activity.ui.ActivityDialog
import nz.co.chrisstevens.coparenting.feature.calendar.domain.generateMonthGrid
import nz.co.chrisstevens.coparenting.feature.children.data.ChildRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentAssignmentRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentRepository
import nz.co.chrisstevens.coparenting.feature.parent.domain.Parent
import nz.co.chrisstevens.coparenting.feature.parent.domain.ParentSlot
import nz.co.chrisstevens.coparenting.feature.parent.ui.ParentAssignmentPanel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

private val YearMonthSaver = Saver<YearMonth, List<Int>>(
    save = { listOf(it.year, it.monthValue) },
    restore = { YearMonth.of(it[0], it[1]) }
)

private val LocalDateSaver = Saver<LocalDate, List<Int>>(
    save = { listOf(it.year, it.monthValue, it.dayOfMonth) },
    restore = { LocalDate.of(it[0], it[1], it[2]) }
)

private val StringSetSaver = Saver<Set<String>, List<String>>(
    save = { it.toList() },
    restore = { it.toSet() }
)

private sealed class ActivityDialogState {
    object Adding : ActivityDialogState()
    data class Editing(val activity: Activity) : ActivityDialogState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    activityRepository: ActivityRepository,
    childRepository: ChildRepository,
    parentRepository: ParentRepository,
    parentAssignmentRepository: ParentAssignmentRepository,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by rememberSaveable(stateSaver = YearMonthSaver) { mutableStateOf(YearMonth.now()) }
    var selectedDate by rememberSaveable(stateSaver = LocalDateSaver) { mutableStateOf(LocalDate.now()) }
    var isAssigningParent by rememberSaveable { mutableStateOf(false) }
    var assigningSlot by rememberSaveable(stateSaver = enumSaver()) { mutableStateOf(ParentSlot.ONE) }
    var dialogState by remember { mutableStateOf<ActivityDialogState?>(null) }
    var showMonthYearPicker by remember { mutableStateOf(false) }

    var showParentAssignments by rememberSaveable { mutableStateOf(true) }
    var showActivities by rememberSaveable { mutableStateOf(true) }
    var hiddenChildIds by rememberSaveable(stateSaver = StringSetSaver) { mutableStateOf(emptySet()) }

    val firstDayOfWeek = remember { WeekFields.of(Locale.getDefault()).firstDayOfWeek }
    val today = remember { LocalDate.now() }
    val monthTitle = remember(currentMonth) {
        currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    }

    val allActivities = activityRepository.activities
    val children = childRepository.children
    val parents = parentRepository.parents
    val parentsBySlot = parents.associateBy { it.slot }
    val assignments = parentAssignmentRepository.assignments

    fun isVisible(activity: Activity): Boolean {
        if (!showActivities) return false
        if (activity.childIds.isEmpty()) return true
        return activity.childIds.any { it !in hiddenChildIds }
    }

    val activitiesForSelectedDay = activitiesOn(selectedDate, allActivities)
        .filter(::isVisible)
        .sortedBy { it.startTime }

    val visibleParentAssignments: Map<LocalDate, Parent> = if (showParentAssignments) {
        assignments.mapNotNull { (date, slot) -> parentsBySlot[slot]?.let { date to it } }.toMap()
    } else {
        emptyMap()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.clickable { showMonthYearPicker = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(monthTitle)
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Choose month and year",
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
                    }
                },
                actions = {
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ParentAssignmentPanel(
                isAssigning = isAssigningParent,
                onStartAssigning = { isAssigningParent = true },
                onStopAssigning = { isAssigningParent = false },
                parents = parents,
                selectedSlot = assigningSlot,
                onSelectSlot = { assigningSlot = it },
                modifier = Modifier.padding(12.dp)
            )
            CalendarFilters(
                showParentAssignments = showParentAssignments,
                onToggleParentAssignments = { showParentAssignments = !showParentAssignments },
                showActivities = showActivities,
                onToggleActivities = { showActivities = !showActivities },
                children = children,
                hiddenChildIds = hiddenChildIds,
                onToggleChild = { childId ->
                    hiddenChildIds = if (childId in hiddenChildIds) {
                        hiddenChildIds - childId
                    } else {
                        hiddenChildIds + childId
                    }
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AnimatedContent(
                targetState = currentMonth,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn())
                            .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn())
                            .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "month"
            ) { month ->
                val monthDays = remember(month, today, firstDayOfWeek) {
                    generateMonthGrid(month, today, firstDayOfWeek)
                }
                val monthActivitiesByDate = monthDays.associate { day ->
                    day.date to activitiesOn(day.date, allActivities).filter(::isVisible)
                }
                MonthGrid(
                    days = monthDays,
                    firstDayOfWeek = firstDayOfWeek,
                    selectedDate = selectedDate,
                    activitiesByDate = monthActivitiesByDate,
                    children = children,
                    parentAssignments = visibleParentAssignments,
                    onDayClick = { date ->
                        selectedDate = date
                        if (isAssigningParent) {
                            parentAssignmentRepository.assign(date, assigningSlot)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            DaySummarySection(
                date = selectedDate,
                parent = visibleParentAssignments[selectedDate],
                activities = activitiesForSelectedDay,
                children = children,
                onActivityClick = { dialogState = ActivityDialogState.Editing(it) },
                onDeleteActivity = { activityRepository.deleteActivity(it.id) },
                onAddActivityClick = { dialogState = ActivityDialogState.Adding }
            )
        }
    }

    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            initialMonth = currentMonth,
            onDismiss = { showMonthYearPicker = false },
            onMonthSelected = { newMonth ->
                currentMonth = newMonth
                showMonthYearPicker = false
            }
        )
    }

    when (val state = dialogState) {
        is ActivityDialogState.Adding -> {
            ActivityDialog(
                date = selectedDate,
                initialActivity = null,
                children = children,
                onDismiss = { dialogState = null },
                onSave = { activity ->
                    activityRepository.addActivity(activity)
                    dialogState = null
                }
            )
        }
        is ActivityDialogState.Editing -> {
            ActivityDialog(
                date = state.activity.date,
                initialActivity = state.activity,
                children = children,
                onDismiss = { dialogState = null },
                onSave = { activity ->
                    activityRepository.updateActivity(activity)
                    dialogState = null
                },
                onDelete = {
                    activityRepository.deleteActivity(state.activity.id)
                    dialogState = null
                }
            )
        }
        null -> Unit
    }
}
