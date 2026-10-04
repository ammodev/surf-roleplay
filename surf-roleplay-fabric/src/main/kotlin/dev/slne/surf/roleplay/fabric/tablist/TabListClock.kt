package dev.slne.surf.roleplay.fabric.tablist

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Formats the values of the tab list that the client counts itself: the clock, the playtime and
 * the restart countdown. All times are server epoch milliseconds.
 */
object TabListClock {

    /**
     * The format of the clock.
     */
    private val CLOCK: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * The milliseconds of one minute.
     */
    private const val MINUTE: Long = 60_000L

    /**
     * Returns the current server time from the time base the server sent and the time elapsed
     * since it was received.
     *
     * @param serverTimeMillis the server time when the state was built
     * @param elapsedMillis the time elapsed since the state was received; negative values count as zero
     * @return the current server time in epoch milliseconds
     */
    fun serverNow(serverTimeMillis: Long, elapsedMillis: Long): Long = serverTimeMillis + elapsedMillis.coerceAtLeast(0)

    /**
     * Returns the time of day as `HH:mm`.
     *
     * @param nowMillis the current server time
     * @param zone the time zone to show the time in
     * @return the formatted time
     */
    fun clock(nowMillis: Long, zone: ZoneId): String = CLOCK.format(Instant.ofEpochMilli(nowMillis).atZone(zone))

    /**
     * Returns the playtime of the session as hours and two-digit minutes, such as `1:05`. A
     * session start after the current time counts as no playtime.
     *
     * @param sessionStartMillis the start of the session
     * @param nowMillis the current server time
     * @return the formatted playtime
     */
    fun playtime(sessionStartMillis: Long, nowMillis: Long): String {
        val minutes = (nowMillis - sessionStartMillis).coerceAtLeast(0) / MINUTE
        return "%d:%02d".format(minutes / 60, minutes % 60)
    }

    /**
     * Returns the restart countdown in German, such as `Neustart in 1 h 23 min` or
     * `Neustart in 4 min`. The minutes are rounded up, so the countdown shows at least one minute
     * until the restart, and also once the restart time has passed.
     *
     * @param restartAtMillis the time of the next restart, or `null` if none is scheduled
     * @param nowMillis the current server time
     * @return the countdown, or `null` if no restart is scheduled
     */
    fun restart(restartAtMillis: Long?, nowMillis: Long): String? {
        if (restartAtMillis == null) return null
        val left = restartAtMillis - nowMillis
        val minutes = ((left + MINUTE - 1).floorDiv(MINUTE)).coerceAtLeast(1)
        return if (minutes >= 60) "Neustart in ${minutes / 60} h ${minutes % 60} min" else "Neustart in $minutes min"
    }
}
