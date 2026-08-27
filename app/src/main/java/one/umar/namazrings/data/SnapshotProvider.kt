package one.umar.namazrings.data

import android.content.Context
import java.time.LocalDateTime
import java.time.ZoneId

object SnapshotProvider {
    val cityZone: ZoneId = ZoneId.of("Asia/Kolkata")

    fun now(): LocalDateTime = LocalDateTime.now(cityZone)

    fun get(context: Context, now: LocalDateTime = now()): PrayerSnapshot {
        val repository = ScheduleRepository(context.applicationContext)
        val today = repository.scheduleFor(now.toLocalDate())
        val tomorrow = repository.scheduleFor(now.toLocalDate().plusDays(1))
        val previousIsha = repository.scheduleFor(now.toLocalDate().minusDays(1)).prayers.last()
        return PrayerProgress.snapshot(now, today, tomorrow, previousIsha)
    }
}
