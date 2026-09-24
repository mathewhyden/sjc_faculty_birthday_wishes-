package com.example.utils

import android.content.Context
import android.content.Intent
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
        val cleanDept = fullDeptName.replace("^Department of\\s+".toRegex(RegexOption.IGNORE_CASE), "").trim()
        val designationLine = if (staff.designation.isNotBlank()) {
            "${staff.designation} Department of $cleanDept"
        } else {
            "Department of $cleanDept"
        }

        return """
🎓 ST. JOSEPH'S COLLEGE (AUTONOMOUS)
🎂 SJC BIRTHDAY WISHES
💐 WARMEST GREETINGS! 💐
Dear ${staff.name},
$designationLine

May the Almighty shower His abundant blessings, vibrant health, enduring peace, and divine joy upon you as you continue your noble mission of forming young minds!

✨ With Prayers & Best Wishes from:
   Rector: Rev. Dr. Pavulraj Michael SJ
• Secretary: Rev. Dr. M. Arockiasamy Xavier SJ
• Principal: Rev. Dr. K. Arockiam SJ
and the entire St. Joseph's College (Autonomous) Fraternity.
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

        val messageText = buildGreetingMessage(faculty, resolvedDept)

        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, messageText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Set WhatsApp package if available
                setPackage("com.whatsapp")
            }

            // Verify if WhatsApp is installed
            val packageManager = context.packageManager
            if (shareIntent.resolveActivity(packageManager) != null) {
                // If a phone number is specified, add JID extra for direct chat in WhatsApp
                if (targetPhone.isNotBlank()) {
                    shareIntent.putExtra("jid", "$targetPhone@s.whatsapp.net")
                }
                context.startActivity(shareIntent)
            } else {
                // Fallback to general chooser
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, messageText)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(fallbackIntent, "Send SJC Birthday Greeting via")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                Toast.makeText(
                    context,
                    "WhatsApp not found. Opening general sharing.",
                    Toast.LENGTH_SHORT
                ).show()
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
}
