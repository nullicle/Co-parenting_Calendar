package com.example.co_parenting_calendar

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.co_parenting_calendar.core.designsystem.theme.Coparenting_CalendarTheme
import com.example.co_parenting_calendar.feature.calendar.ui.CalendarScreen
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun calendarScreen_showsCurrentMonthAndNavigatesToNextMonth() {
        composeTestRule.setContent {
            Coparenting_CalendarTheme {
                CalendarScreen()
            }
        }

        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        val currentMonth = YearMonth.now()
        composeTestRule.onNodeWithText(currentMonth.format(formatter)).assertExists()

        composeTestRule.onNodeWithContentDescription("Next month").performClick()

        composeTestRule.onNodeWithText(currentMonth.plusMonths(1).format(formatter)).assertExists()
    }
}
