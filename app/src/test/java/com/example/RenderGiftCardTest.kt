package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.entity.FacultyEntity
import com.example.utils.CanvasCardDrawer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class RenderGiftCardTest {

    @Test
    fun renderAndExportCard() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sampleFaculty = FacultyEntity(
            id = 1,
            staffId = "22CPH52",
            name = "Dr. G. GENIFER SILVENA",
            departmentCode = "PH",
            dob = "23-09-1985",
            mobile = "9442255661",
            category = "TEACHING",
            designation = "Assistant Professor"
        )
        val bitmap = CanvasCardDrawer.generateGreetingCardBitmap(
            context = context,
            faculty = sampleFaculty,
            resolvedDeptName = "Department of Physics"
        )
        val outDir = File("build/card_output")
        outDir.mkdirs()
        val outFile = File(outDir, "current_card.png")
        FileOutputStream(outFile).use { fos ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
        }

        val stellaFaculty = FacultyEntity(
            id = 505,
            staffId = "23FCO09",
            name = "Mrs. B. MARY STELLA",
            departmentCode = "CO",
            dob = "24-09-1988",
            mobile = "9894455667",
            category = "TEACHING",
            designation = "Assistant Professor of Commerce"
        )
        val stellaBitmap = CanvasCardDrawer.generateGreetingCardBitmap(
            context = context,
            faculty = stellaFaculty,
            resolvedDeptName = "CO"
        )
        val stellaFile = File(outDir, "stella_card.png")
        FileOutputStream(stellaFile).use { fos ->
            stellaBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
        }

        val whatsappMsg = com.example.utils.WhatsAppSender.formatWhatsAppMessage(stellaFaculty, "CO")
        println("STELLA_WHATSAPP_MSG:\n$whatsappMsg")
        assert(whatsappMsg.contains("Assistant Professor, Department of Commerce\n\nMay the Almighty shower")) {
            "Expected blank line between designation and blessing!"
        }
        assert(whatsappMsg.contains("forming young minds!\n\n✨ With Prayers & Best Wishes from:✨")) {
            "Expected blank line before prayers and sparkling stars on both sides!"
        }
        assert(whatsappMsg.contains("    & Standing Committee.")) {
            "Expected 4-space indentation for & Standing Committee.!"
        }
        assert(!whatsappMsg.contains("Department of CO")) {
            "Should not contain 'Department of CO'!"
        }
        println("CARD_RENDERED_PATH:" + outFile.absolutePath)
    }
}
