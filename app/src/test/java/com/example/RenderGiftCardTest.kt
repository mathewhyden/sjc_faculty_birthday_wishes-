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
        println("CARD_RENDERED_PATH:" + outFile.absolutePath)
    }
}
