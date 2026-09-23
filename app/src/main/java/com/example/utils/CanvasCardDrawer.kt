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
     * Generates a high-definition 1200 x 675 Bitmap greeting card for the faculty member.
     */
    fun generateGreetingCardBitmap(
        context: Context,
        faculty: FacultyEntity,
        resolvedDeptName: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw Royal Navy Gradient Background
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

        // 2. Draw Decorative Gold Framing & Border
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

        // Draw Ornamental Corner Flourishes
        drawCornerAccents(canvas, outerMargin, innerMargin)

        // =========================================================================
        // TOP HEADER (Y: 0 - 150)
        // =========================================================================
        drawHeader(canvas, context)

        // =========================================================================
        // CENTER BODY (Y: 150 - 450)
        // =========================================================================
        drawFacultyProfile(canvas, faculty, resolvedDeptName)
        drawQuoteBox(canvas)

        // =========================================================================
        // FOOTER (Y: 450 - 675)
        // =========================================================================
        drawFooter(canvas, context)

        return bitmap
    }

    private fun drawCornerAccents(canvas: Canvas, outer: Float, inner: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.FILL
        }
        val size = 12f
        // 4 corner dots
        canvas.drawCircle(outer + 20f, outer + 20f, 4f, paint)
        canvas.drawCircle(CARD_WIDTH - outer - 20f, outer + 20f, 4f, paint)
        canvas.drawCircle(outer + 20f, CARD_HEIGHT - outer - 20f, 4f, paint)
        canvas.drawCircle(CARD_WIDTH - outer - 20f, CARD_HEIGHT - outer - 20f, 4f, paint)
    }

    private fun drawHeader(canvas: Canvas, context: Context) {
        // 1. Top Left Corner: Official SJC Crest Emblem (Real College Logo Image)
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

        // Center / Header Text Area
        val headerCenterX = CARD_WIDTH / 2f + 30f

        // Top Title: ST. JOSEPH'S COLLEGE (AUTONOMOUS)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 31f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.07f
        }
        canvas.drawText("ST. JOSEPH'S COLLEGE (AUTONOMOUS)", headerCenterX, 74f, titlePaint)

        // Subtitle Header: SJC BIRTHDAY WISHES
        val birthdayTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37") // Metallic Gold
            textSize = 25f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.12f
        }
        canvas.drawText("★ SJC BIRTHDAY WISHES ★", headerCenterX, 107f, birthdayTitlePaint)

        // Institutional Details & Motto
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.04f
        }
        canvas.drawText("TIRUCHIRAPPALLI - 620 002, TAMIL NADU • ESTD. 1844", headerCenterX, 131f, subtitlePaint)

        val mottoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            textSize = 13.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Motto: \"Pro Bono Et Vero\" (For the Good and the True)", headerCenterX, 150f, mottoPaint)

        // Divider Line
        val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#50D4AF37")
            strokeWidth = 1.8f
        }
        canvas.drawLine(CARD_WIDTH * 0.08f, 162f, CARD_WIDTH * 0.92f, 162f, divPaint)
    }

    private fun drawFacultyProfile(
        canvas: Canvas,
        faculty: FacultyEntity,
        resolvedDeptName: String
    ) {
        // =========================================================================
        // A) FACULTY AVATAR / PHOTO POSITION:
        // - Move the Birthday Faculty Photo/Avatar from LEFT to the RIGHT side of the card.
        // - Position: X = CardWidth - 220px, Y = 180px, Circle Diameter = 170px.
        // =========================================================================
        val avatarLeft = CARD_WIDTH - 220f // 980px
        val avatarTop = 180f
        val avatarDiameter = 170f
        val avatarRadius = avatarDiameter / 2f // 85px
        val avatarCenterX = avatarLeft + avatarRadius // 1065px
        val avatarCenterY = avatarTop + avatarRadius // 265px

        // Outer Gold Ring for Avatar (6px border)
        val outerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius + 3f, outerRingPaint)

        // Try loading faculty photo from photoUrl; Fallback to monogram initials if missing/fails
        val facultyPhotoBitmap = if (faculty.photoUrl.isNotBlank()) {
            loadFacultyPhotoBitmap(faculty.photoUrl)
        } else null

        if (facultyPhotoBitmap != null) {
            // Draw Circular Clipped Faculty Photo
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
            // Fallback: Avatar Circular Fill with Burgundy / Crimson gradient & Monogram Initials
            val avatarFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    avatarCenterX - avatarRadius, avatarCenterY - avatarRadius,
                    avatarCenterX + avatarRadius, avatarCenterY + avatarRadius,
                    intArrayOf(
                        Color.parseColor("#800000"), // Crimson Red
                        Color.parseColor("#4A0000")  // Deep Burgundy
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, avatarFillPaint)

            // Avatar Monogram Initials (e.g. "GG" for Dr. G. Genifer Silvena)
            val initials = getInitials(faculty.name)
            val initialsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D4AF37")
                textSize = 54f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val textBounds = Rect()
            initialsPaint.getTextBounds(initials, 0, initials.length, textBounds)
            val initialsY = avatarCenterY + (textBounds.height() / 2f)
            canvas.drawText(initials, avatarCenterX, initialsY, initialsPaint)
        }

        // =========================================================================
        // B) FACULTY NAME & DESIGNATION:
        // - Align on the LEFT side (X = 80px)
        // - REMOVE Staff ID
        // - COMBINE Designation and Expanded Department into a SINGLE LINE:
        //   Format: "${staff.designation} of ${getDepartmentFullName(staff.deptCode)}"
        //   Example Output: "Assistant Professor of Department of Physics"
        // =========================================================================
        val infoStartX = 80f

        // "★ HAPPY BIRTHDAY PROFESSOR ★" Pill
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#800000") // Crimson Red Pill
            style = Paint.Style.FILL
        }
        val pillRect = RectF(infoStartX, 185f, infoStartX + 300f, 215f)
        canvas.drawRoundRect(pillRect, 8f, 8f, pillPaint)

        val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(pillRect, 8f, 8f, pillBorderPaint)

        val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFDF73")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("★ HAPPY BIRTHDAY PROFESSOR ★", pillRect.centerX(), 206f, pillTextPaint)

        // Full Name
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        var nameTextSize = 34f
        while (namePaint.measureText(faculty.name) > 870f && nameTextSize > 24f) {
            nameTextSize -= 1f
            namePaint.textSize = nameTextSize
        }
        canvas.drawText(faculty.name, infoStartX, 255f, namePaint)

        // Single Combined Line: "${staff.designation} of ${getDepartmentFullName(staff.deptCode)}"
        val expandedDept = getDepartmentFullName(faculty.deptCode)
        val combinedDesigDept = if (faculty.designation.isNotBlank()) {
            "${faculty.designation} of $expandedDept"
        } else {
            "Faculty Member of $expandedDept"
        }

        val desigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        var desigTextSize = 21f
        while (desigPaint.measureText(combinedDesigDept) > 870f && desigTextSize > 16f) {
            desigTextSize -= 0.5f
            desigPaint.textSize = desigTextSize
        }
        canvas.drawText(combinedDesigDept, infoStartX, 295f, desigPaint)
    }

    private fun drawQuoteBox(canvas: Canvas) {
        val quoteBox = RectF(65f, 350f, CARD_WIDTH - 65f, 450f)

        // Box background fill with deep royal navy
        val boxFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#152C47")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(quoteBox, 14f, 14f, boxFill)

        // Box border with metallic gold
        val boxStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#99D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(quoteBox, 14f, 14f, boxStroke)

        // Enlarged Birthday Greeting Words (KOJAM PERSA VENUM)
        val greetingHeadlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFDF73")
            textSize = 27f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.05f
        }
        canvas.drawText(
            "💐 WISHING YOU A VERY HAPPY & BLESSED BIRTHDAY! 💐",
            quoteBox.centerX(),
            388f,
            greetingHeadlinePaint
        )

        val greetingSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 21f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "“May the Almighty bless you with vibrant health, lasting peace, and divine joy",
            quoteBox.centerX(),
            418f,
            greetingSubPaint
        )
        canvas.drawText(
            "as you continue your noble mission of shaping minds at St. Joseph's!”",
            quoteBox.centerX(),
            442f,
            greetingSubPaint
        )
    }

    private fun drawFooter(canvas: Canvas, context: Context) {
        // Divider
        val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#40D4AF37")
            strokeWidth = 1.2f
        }
        canvas.drawLine(65f, 465f, CARD_WIDTH - 65f, 465f, divPaint)

        // =========================================================================
        // C) BOTTOM-LEFT SECTION OVERHAUL:
        // - REMOVE the old address footer text block
        // - IN THE BOTTOM-LEFT CORNER (X = 80px, Y = 500px):
        //   * Draw Principal's Photo Bitmap (Size: 100x100 px circle).
        //   * Draw Label Text beneath the photo: "Principal & Standing Committee" (Bold, Metallic Gold color #D4AF37).
        // =========================================================================
        val principalX = 80f
        val principalY = 500f
        val principalSize = 100f
        val principalRect = RectF(principalX, principalY, principalX + principalSize, principalY + principalSize)

        // Decode / Retrieve Principal Photo Bitmap
        val principalBmp = getPrincipalBitmap(context)

        // Draw Circular Clipped Photo
        val clipPath = Path().apply {
            addOval(principalRect, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(clipPath)
        val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(principalBmp, null, principalRect, bmpPaint)
        canvas.restore()

        // Outer Gold Ring around Principal photo
        val photoRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawOval(principalRect, photoRingPaint)

        // Draw Label Text beneath the photo: "Principal & Standing Committee" (Bold, Metallic Gold color #D4AF37)
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            textSize = 12.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val labelText = "Principal & Standing Committee"
        val textWidth = labelPaint.measureText(labelText)
        val labelCenterX = maxOf(38f + (textWidth / 2f) + 4f, principalRect.centerX())
        canvas.drawText(labelText, labelCenterX, 624f, labelPaint)

        // =========================================================================
        // BOTTOM-RIGHT SECTION: WITH PRAYERS & BEST WISHES
        // Ordered strictly: RECTOR -> PRINCIPAL -> SECRETARY
        // =========================================================================
        val leadershipX = CARD_WIDTH - 500f
        val leadershipY = 482f

        // Best Wishes Box
        val wishBox = RectF(leadershipX, leadershipY, CARD_WIDTH - 65f, 625f)
        val wishBoxBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#152336")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(wishBox, 10f, 10f, wishBoxBg)

        val wishBoxBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#66D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        canvas.drawRoundRect(wishBox, 10f, 10f, wishBoxBorder)

        // Section Title: Best Wishes
        val wishTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFDF73")
            textSize = 14.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        canvas.drawText("WITH PRAYERS & BEST WISHES FROM:", leadershipX + 18f, leadershipY + 26f, wishTitlePaint)

        val wishSubTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText("RECTOR, PRINCIPAL & SECRETARY", leadershipX + 18f, leadershipY + 47f, wishSubTitlePaint)

        // 3 Leaders
        val leaderNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val leaderRolePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // 1. Rector
        canvas.drawText("• Rev. Dr. Pavulraj Michael SJ", leadershipX + 18f, leadershipY + 71f, leaderNamePaint)
        canvas.drawText(" (Rector)", leadershipX + 245f, leadershipY + 71f, leaderRolePaint)

        // 2. Principal
        canvas.drawText("• Rev. Dr. K. Arockiam SJ", leadershipX + 18f, leadershipY + 93f, leaderNamePaint)
        canvas.drawText(" (Principal)", leadershipX + 215f, leadershipY + 93f, leaderRolePaint)

        // 3. Secretary
        canvas.drawText("• Rev. Dr. M. Arockiasamy Xavier SJ", leadershipX + 18f, leadershipY + 115f, leaderNamePaint)
        canvas.drawText(" (Secretary)", leadershipX + 285f, leadershipY + 115f, leaderRolePaint)

        val familyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 12f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        }
        canvas.drawText("& the entire St. Joseph's College Fraternity", leadershipX + 18f, leadershipY + 134f, familyPaint)
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
            val resId = context.resources.getIdentifier("ic_principal_photo", "drawable", context.packageName)
            if (resId != 0) {
                val decoded = android.graphics.BitmapFactory.decodeResource(context.resources, resId)
                if (decoded != null) return decoded
            }
        } catch (_: Exception) {}

        // Fallback programmatic dignified portrait bitmap (100x100)
        val bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0A192F")
        }
        c.drawCircle(50f, 50f, 50f, p)
        val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        c.drawText("SJ", 50f, 60f, tp)
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

    private fun drawStar(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        spikes: Int,
        outerRadius: Float,
        innerRadius: Float,
        paint: Paint
    ) {
        val path = Path()
        var rot = Math.PI / 2 * 3
        val step = Math.PI / spikes

        path.moveTo(cx, cy - outerRadius)
        for (i in 0 until spikes) {
            val x = (cx + Math.cos(rot) * outerRadius).toFloat()
            val y = (cy + Math.sin(rot) * outerRadius).toFloat()
            path.lineTo(x, y)
            rot += step

            val ix = (cx + Math.cos(rot) * innerRadius).toFloat()
            val iy = (cy + Math.sin(rot) * innerRadius).toFloat()
            path.lineTo(ix, iy)
            rot += step
        }
        path.lineTo(cx, cy - outerRadius)
        path.close()
        canvas.drawPath(path, paint)
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
