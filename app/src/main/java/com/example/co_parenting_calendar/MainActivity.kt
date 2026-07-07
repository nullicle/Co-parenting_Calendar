package com.example.co_parenting_calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.co_parenting_calendar.core.designsystem.theme.Coparenting_CalendarTheme
import com.example.co_parenting_calendar.feature.activity.data.ActivityRepository
import com.example.co_parenting_calendar.feature.children.data.ChildRepository
import com.example.co_parenting_calendar.feature.parent.data.ParentAssignmentRepository
import com.example.co_parenting_calendar.feature.parent.data.ParentRepository

class MainActivity : ComponentActivity() {

    private val activityRepository by lazy { ActivityRepository(applicationContext) }
    private val childRepository by lazy { ChildRepository(applicationContext) }
    private val parentRepository by lazy { ParentRepository(applicationContext) }
    private val parentAssignmentRepository by lazy { ParentAssignmentRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Coparenting_CalendarTheme {
                CoParentingCalendarApp(
                    activityRepository = activityRepository,
                    childRepository = childRepository,
                    parentRepository = parentRepository,
                    parentAssignmentRepository = parentAssignmentRepository
                )
            }
        }
    }
}
