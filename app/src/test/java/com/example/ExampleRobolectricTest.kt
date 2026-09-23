package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SJC Faculty Directory", appName)
  }

  @Test
  fun `verify phone number sanitization`() {
    val sanitized = com.example.utils.WhatsAppSender.sanitizePhoneNumber("9876543210")
    assertEquals("919876543210", sanitized)
    assertEquals("918754254943", com.example.utils.WhatsAppSender.DEFAULT_TEST_NUMBER)
  }

  @Test
  fun `verify csv parser handles header and row formats`() {
    val csv = """
      Staff ID,Name,Department,Designation,DOB,Mobile,Category
      SJC999,Dr. Test Professor,CS,Associate Professor,16-09-1980,8754254943,Teaching
    """.trimIndent()
    val list = com.example.utils.CsvImporter.parseCsv(csv)
    assertEquals(1, list.size)
    assertEquals("SJC999", list[0].staffId)
    assertEquals("Dr. Test Professor", list[0].name)
    assertEquals("CS", list[0].departmentCode)
    assertEquals("16-09-1980", list[0].dob)
    assertEquals("8754254943", list[0].mobile)
  }

  @Test
  fun `verify screen bottomNavItems are initialized`() {
    val items = com.example.navigation.Screen.bottomNavItems
    assertEquals(4, items.size)
    items.forEach { screen ->
      org.junit.Assert.assertNotNull("Screen must not be null", screen)
      org.junit.Assert.assertNotNull("Screen route must not be null", screen.route)
    }
  }
}
