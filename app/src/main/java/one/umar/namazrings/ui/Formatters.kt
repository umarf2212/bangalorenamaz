package one.umar.namazrings.ui

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale

private val TIME = DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH)
private val MERIDIEM = DateTimeFormatter.ofPattern("a", Locale.ENGLISH)
private val LONG_DATE = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
private val SHORT_DATE = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)

fun formatTime(value: LocalDateTime): String = value.format(TIME)

fun formatMeridiem(value: LocalDateTime): String = value.format(MERIDIEM)

fun formatLongDate(value: LocalDate): String = value.format(LONG_DATE)

fun formatShortDate(value: LocalDate): String = value.format(SHORT_DATE)

fun formatHijriDate(value: LocalDate, includeYear: Boolean = true): String {
    val hijri = HijrahDate.from(value)
    val day = hijri.get(ChronoField.DAY_OF_MONTH)
    val month = HIJRI_MONTHS[hijri.get(ChronoField.MONTH_OF_YEAR) - 1]
    return if (includeYear) {
        "$day $month ${hijri.get(ChronoField.YEAR_OF_ERA)} AH"
    } else {
        "$day $month"
    }
}

fun formatCountdown(duration: Duration): String {
    val minutes = duration.toMinutes().coerceAtLeast(0)
    val hoursPart = minutes / 60
    val minutesPart = minutes % 60
    return when {
        hoursPart > 0 && minutesPart > 0 -> "${hoursPart}h ${minutesPart}m"
        hoursPart > 0 -> "${hoursPart}h"
        minutesPart > 0 -> "${minutesPart}m"
        else -> "now"
    }
}

private val HIJRI_MONTHS = listOf(
    "Muharram",
    "Safar",
    "Rabi I",
    "Rabi II",
    "Jumada I",
    "Jumada II",
    "Rajab",
    "Sha'ban",
    "Ramadan",
    "Shawwal",
    "Dhu al-Qi'dah",
    "Dhu al-Hijjah",
)
