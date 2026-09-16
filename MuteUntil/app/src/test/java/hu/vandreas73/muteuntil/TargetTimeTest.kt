package hu.vandreas73.muteuntil

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class TargetTimeTest {

    @Test
    fun timeLaterToday_staysToday() {
        val now = at(2026, 9, 16, 14, 0) + 37_500 // odd seconds must not leak into the target
        assertEquals(at(2026, 9, 16, 18, 30), TargetTime.resolveTarget(18, 30, now, BUDAPEST))
    }

    @Test
    fun timeEarlierToday_rollsToTomorrow() {
        val now = at(2026, 9, 16, 14, 0)
        assertEquals(at(2026, 9, 17, 9, 0), TargetTime.resolveTarget(9, 0, now, BUDAPEST))
    }

    @Test
    fun timeEqualToNow_rollsToTomorrow() {
        val now = at(2026, 9, 16, 14, 0)
        assertEquals(at(2026, 9, 17, 14, 0), TargetTime.resolveTarget(14, 0, now, BUDAPEST))
    }

    @Test
    fun midnight_rollsToNextDay() {
        val now = at(2026, 9, 16, 23, 30)
        assertEquals(at(2026, 9, 17, 0, 0), TargetTime.resolveTarget(0, 0, now, BUDAPEST))
    }

    @Test
    fun springForwardGap_movesPastTheJump() {
        // Budapest jumps 02:00 to 03:00 on 2026-03-29, so 02:30 never happens that night.
        val now = at(2026, 3, 29, 1, 30)
        assertEquals(at(2026, 3, 29, 3, 30), TargetTime.resolveTarget(2, 30, now, BUDAPEST))
    }

    @Test
    fun formatClock_in24HourMode() {
        assertEquals("18:00", TargetTime.formatClock(at(2026, 9, 16, 18, 0), true, BUDAPEST))
        assertEquals("09:05", TargetTime.formatClock(at(2026, 9, 16, 9, 5), true, BUDAPEST))
    }

    @Test
    fun formatClock_in12HourMode() {
        assertEquals("6:00 PM", TargetTime.formatClock(at(2026, 9, 16, 18, 0), false, BUDAPEST))
        assertEquals("9:05 AM", TargetTime.formatClock(at(2026, 9, 16, 9, 5), false, BUDAPEST))
    }

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, BUDAPEST).toInstant().toEpochMilli()

    private companion object {
        val BUDAPEST: ZoneId = ZoneId.of("Europe/Budapest")
    }
}
