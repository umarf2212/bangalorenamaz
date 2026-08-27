package one.umar.namazrings.data

import android.content.Context
import org.json.JSONObject
import java.time.LocalDate

class ScheduleRepository(private val context: Context) {
    fun scheduleFor(date: LocalDate): DaySchedule {
        val monthFile = MONTH_FILES[date.monthValue - 1]
        val root = context.assets.open("$monthFile.json")
            .bufferedReader()
            .use { JSONObject(it.readText()) }
        val days = root.getJSONArray("data")
        val wantedDay = date.dayOfMonth.toString().padStart(2, '0')
        val day = (0 until days.length())
            .asSequence()
            .map(days::getJSONObject)
            .first { it.getString("date") == wantedDay }

        val prayers = Prayer.entries.map { prayer ->
            val baseTime = parseClockTime(day.getString(prayer.jsonKey), prayer)
            // The source table stores Zawal. Congregational Dhuhr begins 10 minutes later.
            val prayerTime = if (prayer == Prayer.DHUHR) baseTime.plusMinutes(10) else baseTime
            PrayerMoment(prayer, date.atTime(prayerTime))
        }
        return DaySchedule(CITY, date, prayers)
    }

    companion object {
        const val CITY = "Bengaluru"

        private val MONTH_FILES = listOf(
            "january",
            "february",
            "march",
            "april",
            "may",
            "june",
            "july",
            "august",
            "september",
            "october",
            "november",
            "december",
        )
    }
}
