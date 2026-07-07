package com.example.co_parenting_calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.co_parenting_calendar.core.designsystem.theme.Coparenting_CalendarTheme
import com.example.co_parenting_calendar.feature.calendar.data.EventRepository
import com.example.co_parenting_calendar.feature.calendar.ui.CalendarScreen

class MainActivity : ComponentActivity() {

    private val eventRepository by lazy { EventRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Coparenting_CalendarTheme {
                CalendarScreen(eventRepository = eventRepository, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
