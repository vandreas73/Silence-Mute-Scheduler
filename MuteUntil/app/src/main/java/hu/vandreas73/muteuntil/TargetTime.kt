package hu.vandreas73.muteuntil

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Pure time maths. No Android imports, so it runs as a JVM unit test. */
object TargetTime {

    private val FORMAT_24_HOUR = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val FORMAT_12_HOUR = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    /**
     * The next instant matching [hour]:[minute] that is strictly after [nowMillis].
     * If that time has already passed today, returns tomorrow's occurrence.
     */
    fun resolveTarget(
        hour: Int,
        minute: Int,
        nowMillis: Long,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Long {
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        val today = now.toLocalDate().atTime(hour, minute).atZone(zone)
        val target = if (today.toInstant().isAfter(now.toInstant())) today else today.plusDays(1)
        return target.toInstant().toEpochMilli()
    }

    /** Formats [epochMillis] as "18:00" when [use24Hour] is true, else "6:00 PM". */
    fun formatClock(
        epochMillis: Long,
        use24Hour: Boolean,
        zone: ZoneId = ZoneId.systemDefault(),
    ): String {
        val formatter = if (use24Hour) FORMAT_24_HOUR else FORMAT_12_HOUR
        return formatter.format(Instant.ofEpochMilli(epochMillis).atZone(zone))
    }
}
