package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.database.AppDatabase
import com.example.data.entity.FacultyEntity
import java.io.File
import java.io.FileOutputStream

object CanvasCardDrawer {

    private const val CARD_WIDTH = 1200
    private const val CARD_HEIGHT = 675

    /**
     * Expands department code to its official full name, e.g. "PH" -> "Department of Physics".
     */
    fun getDepartmentFullName(deptCode: String): String {
        val clean = deptCode.trim().uppercase()
        return AppDatabase.DEPARTMENT_MAPPINGS[clean] ?: "Department of $clean"
    }

    /**
     * Extracts clean department subject name (stripping leading "Department of " if present).
     */
    fun getDepartmentCleanName(deptCode: String): String {
        val clean = deptCode.trim().uppercase()
        val full = AppDatabase.DEPARTMENT_MAPPINGS[clean] ?: clean
        return full.replace("^Department of\\s+".toRegex(RegexOption.IGNORE_CASE), "").trim()
    }

    /**
     * Generates a high-definition 1200 x 675 Bitmap greeting card with exact alignment rules:
     * 1. Top Header: Line 1 "ST. JOSEPH'S COLLEGE TIRUCHIRAPPALLI", Line 2 Motto, Line 3 "★ JOS BIRTHDAY WISHES ★".
     * 2. Faculty Avatar: Locked to far right end (Fixed X = 980px, Y = 180px, Diameter = 150px, Gold border #D4AF37).
     * 3. Professor Details: Left Margin X = 80px, Badge Y = 180px, Full Name Y = 245px, Single Line Title Y = 280px ("${faculty.designation}, Department of ${fullDeptName}").
     * 4. Quote Box: X = 60px, Y = 345px, Width = 1080px, Height = 120px.
     * 5. Bottom-Left Principal: Photo Circle at Center X = 110px, Y = 555px, Radius = 40px; Labels: Line 1 "Rev. Dr. K. Arockiam SJ" (Gold), Line 2 "Principal, Academic Head, SJC" (White).
     * 6. Bottom-Right Leadership: With Prayers & Best Wishes from Rector, Secretary & Principal, & Standing Committee.
     */
    fun generateGreetingCardBitmap(
        context: Context,
        faculty: FacultyEntity,
        resolvedDeptName: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Royal Navy Gradient Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
                intArrayOf(
                    Color.parseColor("#002B49"), // Royal Navy Blue
                    Color.parseColor("#001B30"), // Deep Navy
                    Color.parseColor("#0A1118")  // Midnight Slate
                ),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), bgPaint)

        // 2. Decorative Gold Framing & Border
        val goldFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37") // Metallic Gold
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val innerFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#66D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        val outerMargin = 28f
        canvas.drawRoundRect(
            RectF(outerMargin, outerMargin, CARD_WIDTH - outerMargin, CARD_HEIGHT - outerMargin),
            16f, 16f, goldFramePaint
        )
        val innerMargin = 38f
        canvas.drawRoundRect(
            RectF(innerMargin, innerMargin, CARD_WIDTH - innerMargin, CARD_HEIGHT - innerMargin),
            12f, 12f, innerFramePaint
        )

        // Corner Flourishes
        drawCornerAccents(canvas, outerMargin)

        // =========================================================================
        // TOP HEADER
        // =========================================================================
        drawHeader(canvas, context)

        // =========================================================================
        // 1 & 2: PROFESSOR DETAILS & LOCKED RIGHT AVATAR
        // =========================================================================
        drawFacultyProfile(canvas, faculty)

        // =========================================================================
        // 3: MIDDLE GREETING QUOTE BOX
        // =========================================================================
        drawQuoteBox(canvas)

        // =========================================================================
        // 4 & 5: FOOTER (SIDE-BY-SIDE PRINCIPAL + RIGHT LEADERSHIP CONTAINER)
        // =========================================================================
        drawFooter(canvas, context)

        return bitmap
    }

    private fun drawCornerAccents(canvas: Canvas, outer: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(outer + 20f, outer + 20f, 4f, paint)
        canvas.drawCircle(CARD_WIDTH - outer - 20f, outer + 20f, 4f, paint)
        canvas.drawCircle(outer + 20f, CARD_HEIGHT - outer - 20f, 4f, paint)
        canvas.drawCircle(CARD_WIDTH - outer - 20f, CARD_HEIGHT - outer - 20f, 4f, paint)
    }

    private fun drawHeader(canvas: Canvas, context: Context) {
        // Official SJC Crest Emblem
        val emblemSize = 115f
        val emblemLeft = 52f
        val emblemTop = 36f

        val crestBitmap = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.drawable.ic_sjc_crest)
        if (crestBitmap != null) {
            val dstRect = RectF(emblemLeft, emblemTop, emblemLeft + emblemSize, emblemTop + emblemSize)
            val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(crestBitmap, null, dstRect, bmpPaint)
        } else {
            val crestDrawable = androidx.core.content.ContextCompat.getDrawable(context, com.example.R.drawable.ic_sjc_crest)
            if (crestDrawable != null) {
                crestDrawable.setBounds(emblemLeft.toInt(), emblemTop.toInt(), (emblemLeft + emblemSize).toInt(), (emblemTop + emblemSize).toInt())
                crestDrawable.draw(canvas)
            }
        }

        val headerCenterX = CARD_WIDTH / 2f

        // Line 1 (SINGLE LINE): "ST. JOSEPH'S COLLEGE TIRUCHIRAPPALLI"
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 30f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.06f
        }
        canvas.drawText("ST. JOSEPH'S COLLEGE TIRUCHIRAPPALLI", headerCenterX, 72f, titlePaint)

        // Line 2 (Motto): "Motto: \"Pro Bono Et Vero\" (For the Good and the True)"
        val mottoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            textSize = 15.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Motto: \"Pro Bono Et Vero\" (For the Good and the True)", headerCenterX, 104f, mottoPaint)

        // Line 3 (Header Badge): "★ JOS BIRTHDAY WISHES ★"
        val birthdayTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.12f
        }
        canvas.drawText("★ JOS BIRTHDAY WISHES ★", headerCenterX, 140f, birthdayTitlePaint)

        // Header Divider Line
        val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#50D4AF37")
            strokeWidth = 1.8f
        }
        canvas.drawLine(CARD_WIDTH * 0.08f, 162f, CARD_WIDTH * 0.92f, 162f, divPaint)
    }

    private fun drawFacultyProfile(
        canvas: Canvas,
        faculty: FacultyEntity
    ) {
        // =========================================================================
        // 2. FACULTY AVATAR & DETAILS
        // - Avatar Circle: Positioned fixed on the far RIGHT side (X = 980px, Y = 180px, Diameter = 150px with Gold border)
        //   Top-Left (980, 180), Center (1055, 255), Radius = 75px
        // =========================================================================
        val avatarLeft = 980f
        val avatarTop = 180f
        val avatarDiameter = 150f
        val avatarRadius = avatarDiameter / 2f // 75px
        val avatarCenterX = avatarLeft + avatarRadius // 1055px
        val avatarCenterY = avatarTop + avatarRadius // 255px

        // Gold Border (#D4AF37)
        val outerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius + 2f, outerRingPaint)

        val facultyPhotoBitmap = if (faculty.photoUrl.isNotBlank()) {
            loadFacultyPhotoBitmap(faculty.photoUrl)
        } else null

        if (facultyPhotoBitmap != null) {
            val photoPath = Path().apply {
                addCircle(avatarCenterX, avatarCenterY, avatarRadius, Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(photoPath)
            val destRect = RectF(avatarLeft, avatarTop, avatarLeft + avatarDiameter, avatarTop + avatarDiameter)
            val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(facultyPhotoBitmap, null, destRect, bmpPaint)
            canvas.restore()
        } else {
            // Circular Fill with Crimson / Burgundy gradient & Monogram Initials
            val avatarFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    avatarLeft, avatarTop,
                    avatarLeft + avatarDiameter, avatarTop + avatarDiameter,
                    intArrayOf(
                        Color.parseColor("#800000"), // Crimson Red
                        Color.parseColor("#4A0000")  // Deep Burgundy
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, avatarFillPaint)

            val initials = getInitials(faculty.name)
            val initialsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D4AF37")
                textSize = 50f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val textBounds = Rect()
            initialsPaint.getTextBounds(initials, 0, initials.length, textBounds)
            val initialsY = avatarCenterY + (textBounds.height() / 2f)
            canvas.drawText(initials, avatarCenterX, initialsY, initialsPaint)
        }

        // =========================================================================
        // DETAILS (LEFT SIDE AT X = 80px)
        // * Badge: "★ HAPPY BIRTHDAY PROFESSOR ★"
        // * Name: Bold Serif Full Name.
        // * Title (SINGLE LINE): "${faculty.designation}, Department of ${fullDeptName}"
        //   (e.g., "Assistant Professor, Department of English" - Staff ID is REMOVED).
        // =========================================================================
        val leftMarginX = 80f
        val maxTextWidth = 870f // Leaves clearance before avatar starting at X = 980px

        // Badge: Y = 180px, Height = 34px, Width = 300px, Pill-shaped
        val pillTop = 180f
        val pillHeight = 34f
        val pillWidth = 300f
        val pillRect = RectF(leftMarginX, pillTop, leftMarginX + pillWidth, pillTop + pillHeight)

        val pillFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#800000")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(pillRect, pillHeight / 2f, pillHeight / 2f, pillFillPaint)

        val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(pillRect, pillHeight / 2f, pillHeight / 2f, pillBorderPaint)

        val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFDF73")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
            textAlign = Paint.Align.CENTER
        }
        val fm = pillTextPaint.fontMetrics
        val pillTextY = pillRect.centerY() - (fm.ascent + fm.descent) / 2f
        canvas.drawText("★ HAPPY BIRTHDAY PROFESSOR ★", pillRect.centerX(), pillTextY, pillTextPaint)

        // Name: Bold Serif Full Name at X = 80px, baseline at 252px (breathing space from badge)
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 32f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        var nameSize = 32f
        while (namePaint.measureText(faculty.name) > maxTextWidth && nameSize > 20f) {
            nameSize -= 1f
            namePaint.textSize = nameSize
        }
        canvas.drawText(faculty.name, leftMarginX, 252f, namePaint)

        // Title (SINGLE LINE): "${faculty.designation}, Department of ${fullDeptName}"
        val cleanDept = getDepartmentCleanName(faculty.deptCode)
        val deptString = "Department of $cleanDept"
        val singleLineTitle = if (faculty.designation.isNotBlank()) {
            "${faculty.designation}, $deptString"
        } else {
            deptString
        }

        val desigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        var desigSize = 20f
        while (desigPaint.measureText(singleLineTitle) > maxTextWidth && desigSize > 14f) {
            desigSize -= 0.5f
            desigPaint.textSize = desigSize
        }
        canvas.drawText(singleLineTitle, leftMarginX, 285f, desigPaint)
    }

    private fun drawQuoteBox(canvas: Canvas) {
        // =========================================================================
        // 3. MIDDLE GREETING QUOTE BOX
        // - Container Box: X = 60px, Y = 345px, Width = 1080px, Height = 120px, Corner Radius = 16px
        // - Line 1: Y = 385px, Centered
        // - Line 2: Y = 425px, Centered
        // =========================================================================
        val boxX = 60f
        val boxY = 345f
        val boxWidth = 1080f
        val boxHeight = 120f
        val quoteBox = RectF(boxX, boxY, boxX + boxWidth, boxY + boxHeight)

        val boxFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#152C47")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(quoteBox, 16f, 16f, boxFill)

        val boxStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#99D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(quoteBox, 16f, 16f, boxStroke)

        // Line 1: Y = 385px, Centered
        val headlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFDF73")
            textSize = 26f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.04f
        }
        canvas.drawText(
            "💐 WISHING YOU A VERY HAPPY & BLESSED BIRTHDAY! 💐",
            quoteBox.centerX(),
            385f,
            headlinePaint
        )

        // Line 2: Y = 420px & 442px, Centered
        val quoteSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 19.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "“May the Almighty bless you with vibrant health, lasting peace, and divine joy",
            quoteBox.centerX(),
            418f,
            quoteSubPaint
        )
        canvas.drawText(
            "as you continue your noble mission of shaping minds at St. Joseph's!”",
            quoteBox.centerX(),
            442f,
            quoteSubPaint
        )
    }

    private fun drawFooter(canvas: Canvas, context: Context) {
        // Subtle Divider Line above footer
        val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33D4AF37")
            strokeWidth = 1f
        }
        canvas.drawLine(60f, 475f, CARD_WIDTH - 60f, 475f, divPaint)

        // =========================================================================
        // 4. BOTTOM-LEFT PRINCIPAL SECTION (SIDE-BY-SIDE FLEX LAYOUT)
        // - Principal Photo Circle: Center X = 110px, Center Y = 555px, Radius = 40px (Diameter 80px)
        // - Label Text (Positioned to the RIGHT of photo at X = 170px):
        //   * Line 1 (Y = 545px): "Principal & Standing Committee" (Bold Gold)
        //   * Line 2 (Y = 570px): "St. Joseph's College (Autonomous)" (Muted Slate)
        // =========================================================================
        val principalCenterX = 110f
        val principalCenterY = 555f
        val principalRadius = 40f
        val principalDiameter = principalRadius * 2f // 80px
        val principalLeft = principalCenterX - principalRadius
        val principalTop = principalCenterY - principalRadius
        val principalRect = RectF(principalLeft, principalTop, principalLeft + principalDiameter, principalTop + principalDiameter)

        val principalBmp = getPrincipalBitmap(context)

        // Draw Circular Clipped Principal Photo
        val clipPath = Path().apply {
            addCircle(principalCenterX, principalCenterY, principalRadius, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(clipPath)
        val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(principalBmp, null, principalRect, bmpPaint)
        canvas.restore()

        // 3px Gold Ring around Principal photo (#D4AF37)
        val photoRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(principalCenterX, principalCenterY, principalRadius, photoRingPaint)

        // Side-by-Side Label Text to the RIGHT of photo at X = 170px
        val textStartX = 170f

        // Line 1: "Rev. Dr. K. Arockiam SJ" (Bold Gold #D4AF37)
        val principalTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37") // Bold Gold
            textSize = 17.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("Rev. Dr. K. Arockiam SJ", textStartX, 545f, principalTitlePaint)

        // Line 2: "Principal, Academic Head, SJC" (White #FFFFFF)
        val principalSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE // White #FFFFFF
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("Principal, Academic Head, SJC", textStartX, 570f, principalSubPaint)

        // =========================================================================
        // 5. BOTTOM-RIGHT LEADERSHIP CONTAINER
        // - Container Box: X = 600px, Y = 490px, Width = 540px, Height = 135px, Corner Radius = 12px
        // - Header (Y = 515px, X = 620px): "WITH PRAYERS & BEST WISHES FROM:"
        // - Sub-Header (Y = 535px, X = 620px): "RECTOR, SECRETARY & PRINCIPAL"
        // - Ordered List Items (X = 620px):
        //   * Y = 560px: "• Rector: Rev. Dr. Pavulraj Michael SJ"
        //   * Y = 580px: "• Secretary: Rev. Dr. M. Arockiasamy Xavier SJ"
        //   * Y = 600px: "• Principal: Rev. Dr. K. Arockiam SJ"
        //   * Y = 618px: "& Standing Committee"
        // =========================================================================
        val leaderBoxX = 600f
        val leaderBoxY = 480f
        val leaderBoxWidth = 540f
        val leaderBoxHeight = 152f
        val leaderBox = RectF(leaderBoxX, leaderBoxY, leaderBoxX + leaderBoxWidth, leaderBoxY + leaderBoxHeight)

        val leaderBoxBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#152336")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(leaderBox, 12f, 12f, leaderBoxBg)

        val leaderBoxBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#66D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        canvas.drawRoundRect(leaderBox, 12f, 12f, leaderBoxBorder)

        val leaderTextX = 624f

        // Header (Y = 504px, X = 624px): "WITH PRAYERS & BEST WISHES FROM:"
        val wishHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFDF73")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("WITH PRAYERS & BEST WISHES FROM:", leaderTextX, 504f, wishHeaderPaint)

        // Sub-Header (Y = 524px, X = 624px): "RECTOR, SECRETARY & PRINCIPAL"
        val wishSubHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 12.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("RECTOR, SECRETARY & PRINCIPAL", leaderTextX, 524f, wishSubHeaderPaint)

        val leaderListPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
        }

        // Ordered List Items:
        canvas.drawText("•  Rector: Rev. Dr. Pavulraj Michael SJ", leaderTextX, 548f, leaderListPaint)
        canvas.drawText("•  Secretary: Rev. Dr. M. Arockiasamy Xavier SJ", leaderTextX, 568f, leaderListPaint)
        canvas.drawText("•  Principal: Rev. Dr. K. Arockiam SJ", leaderTextX, 588f, leaderListPaint)

        // Standing Committee indented cleanly with name text, 22px bottom clearance
        val fraternityPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFDF73")
            textSize = 12f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("& Standing Committee", leaderTextX + 14f, 610f, fraternityPaint)
    }

    private fun loadFacultyPhotoBitmap(photoUrl: String): Bitmap? {
        return try {
            val file = File(photoUrl)
            if (file.exists() && file.isFile) {
                return android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            }
            if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
                val url = java.net.URL(photoUrl)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                return android.graphics.BitmapFactory.decodeStream(input)
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun getPrincipalBitmap(context: Context): Bitmap {
        try {
            val decoded = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.drawable.ic_principal_photo)
            if (decoded != null) return decoded
        } catch (_: Exception) {}
        try {
            val resId = context.resources.getIdentifier("ic_principal_photo", "drawable", context.packageName)
            if (resId != 0) {
                val decoded = android.graphics.BitmapFactory.decodeResource(context.resources, resId)
                if (decoded != null) return decoded
            }
        } catch (_: Exception) {}

        // Programmatic dignified portrait bitmap (80x80)
        val bmp = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0A192F")
        }
        c.drawCircle(40f, 40f, 40f, p)
        val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        c.drawText("SJ", 40f, 49f, tp)
        return bmp
    }

    private fun getInitials(name: String): String {
        val cleanName = name
            .replace("Dr.", "", ignoreCase = true)
            .replace("Rev.", "", ignoreCase = true)
            .replace("Fr.", "", ignoreCase = true)
            .replace("Mr.", "", ignoreCase = true)
            .replace("Mrs.", "", ignoreCase = true)
            .replace("Ms.", "", ignoreCase = true)
            .replace("Ph.D.", "", ignoreCase = true)
            .replace("SJ", "", ignoreCase = true)
            .replace(".", " ")
            .trim()

        val parts = cleanName.split("\\s+".toRegex()).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "SJ"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "${parts[0].first()}${parts[1].first()}".uppercase()
        }
    }

    /**
     * Saves the generated bitmap to the application cache directory and returns a content URI.
     */
    fun saveCardToCache(context: Context, bitmap: Bitmap, facultyId: Long): Uri {
        val cacheFolder = File(context.cacheDir, "gift_cards").apply {
            if (!exists()) mkdirs()
        }
        val file = File(cacheFolder, "sjc_birthday_card_${facultyId}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
