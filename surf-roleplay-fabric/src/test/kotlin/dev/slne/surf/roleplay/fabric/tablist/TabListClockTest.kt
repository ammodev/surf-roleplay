package dev.slne.surf.roleplay.fabric.tablist

import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for the clock, playtime and restart countdown of the tab list.
 */
class TabListClockTest {

    /**
     * The time zone the tests show the clock in.
     */
    private val berlin: ZoneId = ZoneId.of("Europe/Berlin")

    /**
     * Returns the epoch milliseconds of a time of day on a fixed date in Berlin.
     *
     * @param hour the hour of the day
     * @param minute the minute of the hour
     * @param second the second of the minute
     * @return the epoch milliseconds
     */
    private fun at(hour: Int, minute: Int, second: Int = 0): Long =
        ZonedDateTime.of(2026, 10, 4, hour, minute, second, 0, berlin).toInstant().toEpochMilli()

    /**
     * Verifies that the current server time is the time base plus the elapsed time.
     */
    @Test
    fun `server now adds the elapsed time to the time base`() {
        assertEquals(1_000L + 5_000L, TabListClock.serverNow(1_000L, 5_000L))
    }

    /**
     * Verifies that a negative elapsed time does not move the time base back.
     */
    @Test
    fun `server now ignores negative elapsed time`() {
        assertEquals(1_000L, TabListClock.serverNow(1_000L, -5_000L))
    }

    /**
     * Verifies that the clock shows hours and minutes in the given zone.
     */
    @Test
    fun `clock shows hours and minutes`() {
        assertEquals("09:05", TabListClock.clock(at(9, 5, 59), berlin))
        assertEquals("18:46", TabListClock.clock(at(18, 46), berlin))
    }

    /**
     * Verifies that the clock wraps over midnight as the elapsed time grows.
     */
    @Test
    fun `clock wraps over midnight`() {
        val now = TabListClock.serverNow(at(23, 59), 2 * 60_000L)
        assertEquals("00:01", TabListClock.clock(now, berlin))
    }

    /**
     * Verifies that the playtime is shown as hours and two-digit minutes.
     */
    @Test
    fun `playtime is hours and minutes`() {
        assertEquals("0:00", TabListClock.playtime(at(10, 0), at(10, 0, 59)))
        assertEquals("0:07", TabListClock.playtime(at(10, 0), at(10, 7)))
        assertEquals("1:05", TabListClock.playtime(at(10, 0), at(11, 5)))
        assertEquals("12:30", TabListClock.playtime(at(0, 0), at(12, 30)))
    }

    /**
     * Verifies that a session start after the current time is shown as no playtime.
     */
    @Test
    fun `playtime is never negative`() {
        assertEquals("0:00", TabListClock.playtime(at(11, 0), at(10, 0)))
    }

    /**
     * Verifies that no restart time gives no countdown.
     */
    @Test
    fun `no restart gives no countdown`() {
        assertNull(TabListClock.restart(null, at(10, 0)))
    }

    /**
     * Verifies the countdown with hours and minutes.
     */
    @Test
    fun `restart countdown with hours`() {
        assertEquals("Neustart in 1 h 23 min", TabListClock.restart(at(11, 23), at(10, 0)))
        assertEquals("Neustart in 1 h 0 min", TabListClock.restart(at(11, 0), at(10, 0)))
    }

    /**
     * Verifies the countdown with minutes only, rounded up to whole minutes.
     */
    @Test
    fun `restart countdown with minutes`() {
        assertEquals("Neustart in 4 min", TabListClock.restart(at(10, 4), at(10, 0)))
        assertEquals("Neustart in 4 min", TabListClock.restart(at(10, 4), at(10, 0, 1)))
        assertEquals("Neustart in 59 min", TabListClock.restart(at(11, 0), at(10, 1)))
    }

    /**
     * Verifies that less than a minute left, and a restart time already passed, show one minute.
     */
    @Test
    fun `restart countdown under one minute`() {
        assertEquals("Neustart in 1 min", TabListClock.restart(at(10, 1), at(10, 0, 30)))
        assertEquals("Neustart in 1 min", TabListClock.restart(at(10, 0), at(10, 0)))
        assertEquals("Neustart in 1 min", TabListClock.restart(at(10, 0), at(10, 2)))
    }
}
