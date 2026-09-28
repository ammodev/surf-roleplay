package dev.slne.surf.roleplay.paper.screen

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Limits how many rejected screen actions are logged per player.
 *
 * Each player may produce [maxPerMinute] log entries per minute. The first rejection beyond that
 * produces one notice that further rejections are suppressed; the rest of the minute is silent.
 *
 * @property maxPerMinute the number of rejections logged per player and minute
 * @property clock returns the current time in milliseconds
 */
class RejectionLog(
    private val maxPerMinute: Int,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    /**
     * What to log for one rejection.
     */
    enum class Decision {
        /**
         * Log the rejection.
         */
        LOG,

        /**
         * Log that further rejections of this player are suppressed for the rest of the minute.
         */
        SUPPRESS_NOTICE,

        /**
         * Log nothing.
         */
        SILENT,
    }

    /**
     * The logging window of one player.
     *
     * @property start when the window started, in milliseconds
     * @property count the number of rejections in the window
     */
    private class Window(var start: Long, var count: Int)

    /**
     * The current window of every player with recent rejections.
     */
    private val windows = ConcurrentHashMap<UUID, Window>()

    /**
     * Decides what to log for a rejection of a player's action.
     *
     * @param player the player's unique id
     * @return the decision
     */
    fun decide(player: UUID): Decision {
        val now = clock()
        val window = windows.computeIfAbsent(player) { Window(now, 0) }
        synchronized(window) {
            if (now - window.start >= WINDOW_MILLIS) {
                window.start = now
                window.count = 0
            }
            window.count++
            return when {
                window.count <= maxPerMinute -> Decision.LOG
                window.count == maxPerMinute + 1 -> Decision.SUPPRESS_NOTICE
                else -> Decision.SILENT
            }
        }
    }

    /**
     * Forgets a player's window.
     *
     * @param player the player's unique id
     */
    fun forget(player: UUID) {
        windows.remove(player)
    }

    /**
     * Holds the window length.
     */
    private companion object {
        /**
         * The length of one logging window.
         */
        const val WINDOW_MILLIS: Long = 60_000
    }
}
