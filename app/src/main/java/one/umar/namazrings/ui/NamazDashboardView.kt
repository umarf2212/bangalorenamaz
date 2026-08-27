package one.umar.namazrings.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import one.umar.namazrings.data.PrayerSnapshot
import one.umar.namazrings.data.SnapshotProvider
import kotlin.math.min

class NamazDashboardView(context: Context) : View(context) {
    var onAddWidget: (() -> Unit)? = null
    var canPinWidget: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var snapshot: PrayerSnapshot = SnapshotProvider.get(context)
    private val buttonBounds = RectF()
    private val density = resources.displayMetrics.density

    init {
        setBackgroundColor(NamazPalette.CREAM)
        contentDescription = "Namaz timings and countdown rings for Bengaluru"
    }

    fun refresh() {
        snapshot = SnapshotProvider.get(context)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val side = dp(22f)

        drawHeader(canvas, side)
        drawHero(canvas, side, w - side)
        drawPrayerList(canvas, side, w - side, h)
    }

    private fun drawHeader(canvas: Canvas, x: Float) {
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = NamazPalette.FOREST
        paint.textSize = sp(13f)
        canvas.drawText("NAMAZ RINGS", x, dp(31f), paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = NamazPalette.INK_MUTED
        paint.textSize = sp(14f)
        canvas.drawText("${snapshot.schedule.city}  ·  ${formatLongDate(snapshot.schedule.date)}", x, dp(55f), paint)
    }

    private fun drawHero(canvas: Canvas, left: Float, right: Float) {
        val top = dp(76f)
        val bottom = dp(267f)
        paint.color = NamazPalette.FOREST
        canvas.drawRoundRect(RectF(left, top, right, bottom), dp(28f), dp(28f), paint)

        val nextRing = snapshot.rings.first { it.isNext }
        val centerX = left + dp(82f)
        val centerY = top + dp(92f)
        val radius = dp(55f)
        val arc = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = dp(10f)
        paint.color = NamazPalette.TRACK_LIGHT
        canvas.drawOval(arc, paint)
        paint.color = NamazPalette.ringColor(nextRing.moment.prayer)
        canvas.drawArc(arc, -90f, nextRing.remaining * 360f, false, paint)
        paint.style = Paint.Style.FILL

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = NamazPalette.WHITE
        paint.textSize = sp(23f)
        canvas.drawText(formatTime(nextRing.moment.at), centerX, centerY + dp(2f), paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = 0xB8FFFFFF.toInt()
        paint.textSize = sp(10f)
        canvas.drawText(formatMeridiem(nextRing.moment.at), centerX, centerY + dp(23f), paint)

        val textX = left + dp(155f)
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = 0xB8FFFFFF.toInt()
        paint.textSize = sp(11f)
        canvas.drawText("NEXT NAMAZ", textX, top + dp(50f), paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = NamazPalette.WHITE
        paint.textSize = sp(28f)
        canvas.drawText(nextRing.moment.prayer.displayName, textX, top + dp(84f), paint)
        paint.color = NamazPalette.ringColor(nextRing.moment.prayer)
        paint.textSize = sp(18f)
        canvas.drawText("in ${formatCountdown(snapshot.remainingUntilNext)}", textX, top + dp(114f), paint)

        paint.color = 0x24FFFFFF
        canvas.drawRoundRect(
            RectF(left + dp(18f), bottom - dp(42f), right - dp(18f), bottom - dp(15f)),
            dp(12f),
            dp(12f),
            paint,
        )
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = 0xD8FFFFFF.toInt()
        paint.textSize = sp(10.5f)
        canvas.drawText("Ring empties as the next prayer approaches", (left + right) / 2f, bottom - dp(24f), paint)
    }

    private fun drawPrayerList(canvas: Canvas, left: Float, right: Float, viewHeight: Float) {
        val titleY = dp(309f)
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = NamazPalette.FOREST
        paint.textSize = sp(16f)
        canvas.drawText("Five daily prayers", left, titleY, paint)

        val listTop = dp(326f)
        val buttonHeight = dp(52f)
        val bottomPadding = dp(18f)
        val maxListBottom = viewHeight - buttonHeight - bottomPadding - dp(14f)
        val rowHeight = min(dp(57f), (maxListBottom - listTop) / 5f).coerceAtLeast(dp(34f))
        val listBottom = listTop + rowHeight * 5f
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawRoundRect(RectF(left, listTop, right, listBottom), dp(24f), dp(24f), paint)

        snapshot.rings.forEachIndexed { index, ring ->
            val cy = listTop + rowHeight * (index + 0.5f)
            if (index > 0) {
                paint.color = 0x1120372F
                canvas.drawRect(left + dp(58f), cy - rowHeight / 2f, right - dp(16f), cy - rowHeight / 2f + dp(1f), paint)
            }
            drawMiniRing(canvas, left + dp(31f), cy, ring.remaining, NamazPalette.ringColor(ring.moment.prayer))

            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.create(Typeface.DEFAULT, if (ring.isNext) Typeface.BOLD else Typeface.NORMAL)
            paint.color = NamazPalette.FOREST
            paint.textSize = sp(14f)
            canvas.drawText(ring.moment.prayer.displayName, left + dp(58f), cy + dp(5f), paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = if (ring.isNext) NamazPalette.ringColor(ring.moment.prayer) else NamazPalette.FOREST
            paint.textSize = sp(15f)
            canvas.drawText(
                "${formatTime(ring.moment.at)} ${formatMeridiem(ring.moment.at)}",
                right - dp(18f),
                cy + dp(5f),
                paint,
            )
        }

        val buttonTop = listBottom + dp(14f)
        buttonBounds.set(left, buttonTop, right, buttonTop + buttonHeight)
        paint.color = if (canPinWidget) NamazPalette.FOREST else 0xFF8A938F.toInt()
        canvas.drawRoundRect(buttonBounds, dp(18f), dp(18f), paint)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = NamazPalette.WHITE
        paint.textSize = sp(13f)
        val label = if (canPinWidget) "ADD WIDGET TO HOME SCREEN" else "ADD FROM YOUR WIDGET PICKER"
        canvas.drawText(label, (left + right) / 2f, buttonTop + dp(32f), paint)
    }

    private fun drawMiniRing(canvas: Canvas, cx: Float, cy: Float, remaining: Float, color: Int) {
        val radius = dp(12f)
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = dp(3.2f)
        paint.color = NamazPalette.TRACK_DARK
        canvas.drawOval(rect, paint)
        paint.color = color
        canvas.drawArc(rect, -90f, remaining * 360f, false, paint)
        paint.style = Paint.Style.FILL
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && buttonBounds.contains(event.x, event.y)) {
            performClick()
            if (canPinWidget) onAddWidget?.invoke()
            return true
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun dp(value: Float): Float = value * density

    private fun sp(value: Float): Float = dp(value) * resources.configuration.fontScale
}
