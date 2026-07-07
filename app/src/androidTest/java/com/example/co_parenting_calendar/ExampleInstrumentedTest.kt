package com.example.co_parenting_calendar

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.co_parenting_calendar.core.designsystem.theme.Coparenting_CalendarTheme
import com.example.co_parenting_calendar.feature.calendar.ui.CalendarScreen
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun calendarScreen_rendersTitleAndPlaceholder() {
        composeTestRule.setContent {
            Coparenting_CalendarTheme {
                CalendarScreen()
            }
        }

        composeTestRule.onNodeWithText("Calendar").assertExists()
        composeTestRule.onNodeWithText("Calendar coming soon").assertExists()
    }
}
