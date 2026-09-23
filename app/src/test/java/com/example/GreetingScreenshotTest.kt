package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleFaculty = com.example.data.entity.FacultyEntity(
      id = 1,
      staffId = "26CAI51",
      name = "Dr. R. ARUN PRASATH, Ph.D.",
      departmentCode = "CS",
      dob = "20-07-1984",
      mobile = "9876543210",
      category = "TEACHING",
      designation = "Associate Professor & Head"
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.components.TodayBirthdayCard(
          faculty = sampleFaculty,
          resolvedDeptName = "Department of Computer Science",
          isGenerating = false,
          isTestMode = true,
          onSendGreetingClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
