package com.example.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.example.data.entity.FacultyEntity
import com.example.data.entity.FacultyMember
import java.net.URLEncoder

object WhatsAppSender {

    const val DEFAULT_TEST_NUMBER = "918754254943"

    /**
     * Formats WhatsApp message according to the exact institutional specification.
     */
    fun formatWhatsAppMessage(staff: FacultyMember, fullDeptName: String): String {
        // Guarantee FULL department name resolution (e.g., "CO" -> "Commerce", NEVER "CO")
        val cleanDept = CanvasCardDrawer.getDepartmentCleanName(
            if (fullDeptName.isNotBlank() && !fullDeptName.equals("Department of ${staff.departmentCode}", ignoreCase = true)) {
                fullDeptName
            } else {
                staff.departmentCode
            }
        )
        val deptString = if (cleanDept.startsWith("Department of", ignoreCase = true) ||
            cleanDept.startsWith("Office of", ignoreCase = true) ||
            cleanDept.contains("Office", ignoreCase = true) ||
            cleanDept.contains("Cell", ignoreCase = true) ||
            cleanDept.contains("Centre", ignoreCase = true) ||
            cleanDept.contains("Center", ignoreCase = true) ||
            cleanDept.contains("Library", ignoreCase = true) ||
            cleanDept.contains("&", ignoreCase = true) ||
            cleanDept.contains("Support", ignoreCase = true) ||
            cleanDept.contains("Staff", ignoreCase = true)) {
            cleanDept
        } else {
            "Department of $cleanDept"
        }
        val cleanDesig = CanvasCardDrawer.cleanDesignation(staff.designation, cleanDept)
        val designationLine = if (cleanDesig.isNotBlank()) {
            "$cleanDesig, $deptString"
        } else {
            deptString
        }

        return """
🎓 ST. JOSEPH'S COLLEGE 🎓
🎂 JOS GREETINGS 🎂
💐 WARM  BIRTHDAY WISHES 💐
Dear ${staff.name},
$designationLine

May the Almighty shower His abundant blessings, vibrant health, enduring peace, and divine joy upon you as you continue your noble mission of forming young minds!

✨ With Prayers & Best Wishes from:✨
•  Rector: Rev. Dr. Pavulraj Michael SJ
•  Secretary: Rev. Dr. M. Arockiasamy Xavier SJ
•  Principal: Rev. Dr. K. Arockiam SJ
    & Standing Committee.
        """.trimIndent()
    }

    /**
     * Builds standard institutional birthday greeting message.
     */
    fun buildGreetingMessage(faculty: FacultyEntity, resolvedDept: String): String {
        return formatWhatsAppMessage(faculty, resolvedDept)
    }

    /**
     * Formats Indian mobile numbers to WhatsApp format (adds 91 if missing).
     */
    fun sanitizePhoneNumber(raw: String): String {
        val digits = raw.replace("[^0-9]".toRegex(), "")
        return when {
            digits.length == 10 -> "91$digits"
            digits.startsWith("91") && digits.length == 12 -> digits
            digits.startsWith("0") && digits.length == 11 -> "91" + digits.substring(1)
            else -> digits
        }
    }

    /**
     * Dispatches birthday gift card Bitmap Uri and greeting text via WhatsApp or system share chooser.
     */
    fun dispatchBirthdayGreeting(
        context: Context,
        imageUri: Uri,
        faculty: FacultyEntity,
        resolvedDept: String,
        isTestMode: Boolean,
        customTestNumber: String = DEFAULT_TEST_NUMBER
    ) {
        val targetPhone = if (isTestMode) {
            sanitizePhoneNumber(customTestNumber)
        } else {
            sanitizePhoneNumber(faculty.mobile)
        }

        if (!isTestMode && targetPhone.isBlank()) {
            Toast.makeText(
                context,
                "Mobile number for ${faculty.name} is missing. Please edit profile to add valid phone number.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val messageText = buildGreetingMessage(faculty, resolvedDept)
        val targetDesc = if (isTestMode) "Test Number: +$targetPhone" else "${faculty.name} (+${targetPhone})"
        Toast.makeText(context, "Opening WhatsApp for $targetDesc", Toast.LENGTH_SHORT).show()

        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, messageText)
                if (targetPhone.isNotBlank()) {
                    putExtra("jid", "$targetPhone@s.whatsapp.net")
                    putExtra(Intent.EXTRA_PHONE_NUMBER, targetPhone)
                    putExtra("address", targetPhone)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val packageManager = context.packageManager
            val isWhatsAppInstalled = isPackageInstalled("com.whatsapp", packageManager)
            val isWhatsAppBusinessInstalled = isPackageInstalled("com.whatsapp.w4b", packageManager)

            if (isWhatsAppInstalled) {
                shareIntent.setPackage("com.whatsapp")
                context.startActivity(shareIntent)
            } else if (isWhatsAppBusinessInstalled) {
                shareIntent.setPackage("com.whatsapp.w4b")
                context.startActivity(shareIntent)
            } else {
                // Direct WhatsApp Web / API fallback or general chooser
                try {
                    val encodedMsg = URLEncoder.encode(messageText, "UTF-8")
                    val webIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=$encodedMsg")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                } catch (webErr: Exception) {
                    val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        putExtra(Intent.EXTRA_TEXT, messageText)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(fallbackIntent, "Send Greeting to $targetDesc via")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                }
            }
        } catch (e: Exception) {
            // Direct WhatsApp Web / API fallback
            try {
                val encodedMsg = URLEncoder.encode(messageText, "UTF-8")
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=$encodedMsg")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (err: Exception) {
                Toast.makeText(
                    context,
                    "Unable to dispatch WhatsApp intent: ${err.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
