package one.umar.namazrings.ui

import android.graphics.Color
import one.umar.namazrings.data.Prayer

object NamazPalette {
    const val CREAM = 0xFFF6F1E7.toInt()
    const val FOREST = 0xFF102E24.toInt()
    const val FOREST_SOFT = 0xFF1A4033.toInt()
    const val INK_MUTED = 0xFF627069.toInt()
    const val WHITE = Color.WHITE
    const val TRACK_LIGHT = 0x2BFFFFFF
    const val TRACK_DARK = 0x1820372F

    fun ringColor(prayer: Prayer): Int = when (prayer) {
        Prayer.FAJR -> 0xFF9B8AD7.toInt()
        Prayer.DHUHR -> 0xFFE7B34E.toInt()
        Prayer.ASR -> 0xFF49B884.toInt()
        Prayer.MAGHRIB -> 0xFFE77B64.toInt()
        Prayer.ISHA -> 0xFF5F92D6.toInt()
    }
}
