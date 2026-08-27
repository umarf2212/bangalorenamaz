package one.umar.namazrings.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class PrayerProgressTest {
    private val date = LocalDate.of(2026, 8, 23)
    private val today = schedule(date)
    private val tomorrow = schedule(date.plusDays(1))
    private val previousIsha = PrayerMoment(Prayer.ISHA, date.minusDays(1).atTime(20, 0))

    @Test
    fun `Asr is full just after Dhuhr and empties by Asr`() {
        val afterDhuhr = snapshotAt(date.atTime(12, 33))
        val nearAsr = snapshotAt(date.atTime(16, 43))

        assertEquals(Prayer.ASR, afterDhuhr.nextPrayer.prayer)
        assertTrue(afterDhuhr.rings[2].remaining > 0.99f)
        assertTrue(nearAsr.rings[2].remaining < 0.01f)
    }

    @Test
    fun `past rings are empty and later rings are full`() {
        val snapshot = snapshotAt(date.atTime(15, 0))

        assertEquals(0f, snapshot.rings[0].remaining)
        assertEquals(0f, snapshot.rings[1].remaining)
        assertTrue(snapshot.rings[2].remaining in 0f..1f)
        assertEquals(1f, snapshot.rings[3].remaining)
        assertEquals(1f, snapshot.rings[4].remaining)
    }

    @Test
    fun `after Isha the widget rolls over to tomorrow`() {
        val snapshot = snapshotAt(date.atTime(21, 0))

        assertEquals(date.plusDays(1), snapshot.schedule.date)
        assertEquals(Prayer.FAJR, snapshot.nextPrayer.prayer)
        assertTrue(snapshot.rings.first().remaining in 0f..1f)
    }

    @Test
    fun `source clock values are converted to expected periods`() {
        assertEquals(LocalTime.of(4, 56), parseClockTime("4:56", Prayer.FAJR))
        assertEquals(LocalTime.of(12, 22), parseClockTime("12:22", Prayer.DHUHR))
        assertEquals(LocalTime.of(16, 44), parseClockTime("4:44", Prayer.ASR))
        assertEquals(LocalTime.of(19, 48), parseClockTime("7:48", Prayer.ISHA))
    }

    private fun snapshotAt(now: LocalDateTime): PrayerSnapshot =
        PrayerProgress.snapshot(now, today, tomorrow, previousIsha)

    private fun schedule(day: LocalDate): DaySchedule {
        val times = listOf(
            LocalTime.of(4, 56),
            LocalTime.of(12, 32),
            LocalTime.of(16, 44),
            LocalTime.of(18, 37),
            LocalTime.of(19, 48),
        )
        return DaySchedule(
            "Bengaluru",
            day,
            Prayer.entries.zip(times).map { (prayer, time) -> PrayerMoment(prayer, day.atTime(time)) },
        )
    }
}
