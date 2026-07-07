package com.example.co_parenting_calendar.feature.calendar.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import com.example.co_parenting_calendar.feature.calendar.data.EventRepository
import com.example.co_parenting_calendar.feature.calendar.domain.Event
import com.example.co_parenting_calendar.feature.calendar.domain.generateMonthGrid
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

private sealed class EventDialogState {
    object Adding : EventDialogState()
    data class Editing(val event: Event) : EventDialogState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(eventRepository: EventRepository, modifier: Modifier = Modifier) {
    var currentMonth by rememberSaveable(stateSaver = YearMonthSaver) { mutableStateOf(YearMonth.now()) }
    var selectedDate by rememberSaveable(stateSaver = LocalDateSaver) { mutableStateOf(LocalDate.now()) }
    var dialogState by remember { mutableStateOf<EventDialogState?>(null) }

    val firstDayOfWeek = remember { WeekFields.of(Locale.getDefault()).firstDayOfWeek }
    val today = remember { LocalDate.now() }
    val days = remember(currentMonth) { generateMonthGrid(currentMonth, today, firstDayOfWeek) }
    val monthTitle = remember(currentMonth) {
        currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    }

    val events = eventRepository.events
    val datesWithEvents = events.map { it.date }.toSet()
    val eventsForSelectedDay = events.filter { it.date == selectedDate }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(monthTitle) },
                navigationIcon = {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
                    }
                },
                actions = {
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { dialogState = EventDialogState.Adding }) {
                Icon(Icons.Filled.Add, contentDescription = "Add event")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MonthGrid(
                days = days,
                firstDayOfWeek = firstDayOfWeek,
                selectedDate = selectedDate,
                datesWithEvents = datesWithEvents,
                onDayClick = { selectedDate = it }
            )
            HorizontalDivider()
            DayEventsSection(
                date = selectedDate,
                events = eventsForSelectedDay,
                onEventClick = { dialogState = EventDialogState.Editing(it) }
            )
        }
    }

    when (val state = dialogState) {
        is EventDialogState.Adding -> {
            EventDialog(
                date = selectedDate,
                initialEvent = null,
                onDismiss = { dialogState = null },
                onSave = { event ->
                    eventRepository.addEvent(event)
                    dialogState = null
                }
            )
        }
        is EventDialogState.Editing -> {
            EventDialog(
                date = state.event.date,
                initialEvent = state.event,
                onDismiss = { dialogState = null },
                onSave = { event ->
                    eventRepository.updateEvent(event)
                    dialogState = null
                },
                onDelete = {
                    eventRepository.deleteEvent(state.event.id)
                    dialogState = null
                }
            )
        }
        null -> Unit
    }
}
