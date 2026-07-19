package nz.co.chrisstevens.coparenting

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import nz.co.chrisstevens.coparenting.core.designsystem.theme.Coparenting_CalendarTheme
import nz.co.chrisstevens.coparenting.feature.activity.data.ActivityRepository
import nz.co.chrisstevens.coparenting.feature.calendar.ui.CalendarScreen
import nz.co.chrisstevens.coparenting.feature.children.data.ChildRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentAssignmentRepository
import nz.co.chrisstevens.coparenting.feature.parent.data.ParentRepository
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
        val activityRepository = ActivityRepository()
        val childRepository = ChildRepository()
        val parentRepository = ParentRepository()
        val parentAssignmentRepository = ParentAssignmentRepository()

        composeTestRule.setContent {
            Coparenting_CalendarTheme {
                CalendarScreen(
                    activityRepository = activityRepository,
                    childRepository = childRepository,
                    parentRepository = parentRepository,
                    parentAssignmentRepository = parentAssignmentRepository,
                    onOpenSettings = {}
                )
            }
        }

        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        val currentMonth = YearMonth.now()
        composeTestRule.onNodeWithText(currentMonth.format(formatter)).assertExists()

        composeTestRule.onNodeWithContentDescription("Next month").performClick()

        composeTestRule.onNodeWithText(currentMonth.plusMonths(1).format(formatter)).assertExists()
    }
}
