package one.umar.namazrings.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import one.umar.namazrings.data.PrayerSnapshot
import one.umar.namazrings.ui.NamazPalette
import one.umar.namazrings.ui.formatCountdown
import one.umar.namazrings.ui.formatHijriDate
import one.umar.namazrings.ui.formatShortDate
import one.umar.namazrings.ui.formatTime
import kotlin.math.min
import kotlin.math.roundToInt

object WidgetRenderer {
    fun render(
        snapshot: PrayerSnapshot,
        widthDp: Int,
        heightDp: Int,
        displayDensity: Float,
    ): Bitmap {
        // The widget is a bitmap inside RemoteViews. Render at the launcher's physical
        // pixel density so high-resolution screens do not upscale a low-resolution image.
        val renderScale = displayDensity.coerceIn(MIN_RENDER_SCALE, MAX_RENDER_SCALE)
        val width = (widthDp.coerceIn(220, 520) * renderScale).roundToInt()
        val height = (heightDp.coerceIn(100, 240) * renderScale).roundToInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)

        val s = renderScale
        fun sp(value: Float) = value * s

        paint.color = NamazPalette.FOREST
        canvas.drawRoundRect(
            RectF(sp(3f), sp(3f), width - sp(3f), height - sp(3f)),
            sp(24f),
            sp(24f),
            paint,
        )

        val padding = sp(16f)
        val compact = heightDp < 125
        val wideHeader = widthDp >= 315
        val titleBaseline = if (compact) sp(26f) else sp(32f)

        paint.typeface = FONT_MEDIUM
        paint.color = NamazPalette.WHITE
        paint.textSize = sp(if (compact) 14f else 17f)
        canvas.drawText(snapshot.schedule.city, padding, titleBaseline, paint)

        paint.typeface = FONT_REGULAR
        paint.color = 0xB8FFFFFF.toInt()
        paint.textSize = sp(if (compact) 10.5f else 12f)
        canvas.drawText(
            formatShortDate(snapshot.schedule.date).uppercase(),
            padding,
            titleBaseline + sp(if (compact) 14f else 17f),
            paint,
        )

        if (wideHeader) {
            val headerCenterX = width / 2f
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = FONT_MEDIUM
            paint.color = 0x9EFFFFFF.toInt()
            paint.textSize = sp(if (compact) 7.5f else 8.5f)
            canvas.drawText("HIJRI", headerCenterX, titleBaseline - sp(10f), paint)

            paint.color = NamazPalette.WHITE
            paint.textSize = sp(if (compact) 10f else 11.5f)
            canvas.drawText(
                formatHijriDate(
                    snapshot.schedule.date,
                    includeYear = !compact && widthDp >= 370,
                ).uppercase(),
                headerCenterX,
                titleBaseline + sp(if (compact) 5f else 7f),
                paint,
            )

            paint.textAlign = Paint.Align.RIGHT
            paint.color = 0x9EFFFFFF.toInt()
            paint.textSize = sp(if (compact) 7.5f else 8.5f)
            canvas.drawText(
                "NEXT ${snapshot.nextPrayer.prayer.displayName.uppercase()}",
                width - padding,
                titleBaseline - sp(10f),
                paint,
            )

            paint.color = NamazPalette.ringColor(snapshot.nextPrayer.prayer)
            paint.textSize = sp(if (compact) 13f else 16f)
            canvas.drawText(
                formatCountdown(snapshot.remainingUntilNext),
                width - padding,
                titleBaseline + sp(if (compact) 6f else 9f),
                paint,
            )
            paint.textAlign = Paint.Align.LEFT
        } else {
            paint.typeface = FONT_REGULAR
            paint.color = 0xB8FFFFFF.toInt()
            paint.textSize = sp(if (compact) 8.5f else 10f)
            canvas.drawText(
                formatHijriDate(snapshot.schedule.date, includeYear = !compact).uppercase(),
                padding,
                titleBaseline + sp(if (compact) 25f else 30f),
                paint,
            )

            paint.typeface = FONT_MEDIUM
            paint.color = NamazPalette.ringColor(snapshot.nextPrayer.prayer)
            paint.textSize = sp(if (compact) 11f else 13f)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                "${snapshot.nextPrayer.prayer.displayName.uppercase()}  ${formatCountdown(snapshot.remainingUntilNext)}",
                width - padding,
                titleBaseline + sp(4f),
                paint,
            )
            paint.textAlign = Paint.Align.LEFT
        }

        val top = if (compact) sp(59f) else sp(68f)
        val bottomSpace = sp(if (compact) 20f else 28f)
        val availableHeight = height - top - bottomSpace - sp(7f)
        val cellWidth = (width - 2 * padding) / 5f
        val radius = min(cellWidth * 0.41f, availableHeight / 2f).coerceAtLeast(sp(13f))
        val centerY = top + availableHeight / 2f
        val stroke = (radius * 0.13f).coerceIn(sp(3.5f), sp(7f))

        snapshot.rings.forEachIndexed { index, ring ->
            val centerX = padding + cellWidth * (index + 0.5f)
            val oval = RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
            )

            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = stroke
            paint.color = NamazPalette.TRACK_LIGHT
            canvas.drawOval(oval, paint)
            paint.color = NamazPalette.ringColor(ring.moment.prayer)
            canvas.drawArc(oval, -90f, ring.remaining * 360f, false, paint)

            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = FONT_MEDIUM
            paint.color = NamazPalette.WHITE
            paint.textSize = (radius * 0.62f).coerceIn(sp(10.5f), sp(16.5f))
            val timeY = centerY - (paint.ascent() + paint.descent()) / 2f
            canvas.drawText(formatTime(ring.moment.at), centerX, timeY, paint)

            paint.typeface = FONT_MEDIUM
            paint.color = if (ring.isNext) NamazPalette.WHITE else 0xB8FFFFFF.toInt()
            paint.textSize = sp(if (compact) 9.5f else 11.5f)
            canvas.drawText(
                ring.moment.prayer.displayName,
                centerX,
                centerY + radius + sp(if (compact) 15f else 19f),
                paint,
            )
        }

        return bitmap
    }

    private const val MIN_RENDER_SCALE = 1f
    private const val MAX_RENDER_SCALE = 4f
    private val FONT_REGULAR = Typeface.create("sans-serif", Typeface.NORMAL)
    private val FONT_MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL)
}
