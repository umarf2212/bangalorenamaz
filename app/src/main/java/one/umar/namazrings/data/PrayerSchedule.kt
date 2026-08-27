package one.umar.namazrings.data

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class Prayer(
    val displayName: String,
    val jsonKey: String,
) {
    FAJR("Fajr", "predawn"),
    DHUHR("Dhuhr", "zawal"),
    ASR("Asr", "asr_hanafi"),
    MAGHRIB("Maghrib", "sunset"),
    ISHA("Isha", "isha"),
}

data class PrayerMoment(
    val prayer: Prayer,
    val at: LocalDateTime,
)

data class DaySchedule(
    val city: String,
    val date: LocalDate,
    val prayers: List<PrayerMoment>,
) {
    init {
        require(prayers.map { it.prayer } == Prayer.entries) {
            "A day must contain Fajr, Dhuhr, Asr, Maghrib and Isha in order"
        }
    }
}

data class PrayerRing(
    val moment: PrayerMoment,
    val remaining: Float,
    val isNext: Boolean,
)

data class PrayerSnapshot(
    val schedule: DaySchedule,
    val rings: List<PrayerRing>,
    val nextPrayer: PrayerMoment,
    val remainingUntilNext: Duration,
)

object PrayerProgress {
    fun snapshot(
        now: LocalDateTime,
        today: DaySchedule,
        tomorrow: DaySchedule,
        previousDayIsha: PrayerMoment,
    ): PrayerSnapshot {
        val todayIsha = today.prayers.last().at
        val displayed = if (now >= todayIsha) tomorrow else today
        val priorIsha = if (displayed === today) previousDayIsha else today.prayers.last()
        val nextPrayer = displayed.prayers.first { it.at > now }

        val rings = displayed.prayers.mapIndexed { index, target ->
            val start = if (index == 0) priorIsha.at else displayed.prayers[index - 1].at
            val remaining = when {
                now <= start -> 1f
                now >= target.at -> 0f
                else -> {
                    val intervalMillis = Duration.between(start, target.at).toMillis().toFloat()
                    val remainingMillis = Duration.between(now, target.at).toMillis().toFloat()
                    (remainingMillis / intervalMillis).coerceIn(0f, 1f)
                }
            }
            PrayerRing(target, remaining, target == nextPrayer)
        }

        return PrayerSnapshot(
            schedule = displayed,
            rings = rings,
            nextPrayer = nextPrayer,
            remainingUntilNext = Duration.between(now, nextPrayer.at).coerceAtLeast(Duration.ZERO),
        )
    }
}

internal fun parseClockTime(raw: String, prayer: Prayer): LocalTime {
    val (rawHour, minute) = raw.trim().split(":").map(String::toInt)
    val hour = when (prayer) {
        Prayer.FAJR -> if (rawHour == 12) 0 else rawHour
        Prayer.DHUHR -> if (rawHour < 11) rawHour + 12 else rawHour
        Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA -> if (rawHour < 12) rawHour + 12 else rawHour
    }
    return LocalTime.of(hour, minute)
}
