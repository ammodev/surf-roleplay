package dev.slne.surf.roleplay.paper.interaction

import java.util.concurrent.ConcurrentHashMap

/**
 * Lets a log entry through at most once per interval for each key.
 *
 * @property intervalMillis the least time between two entries of the same key, in milliseconds
 * @property clock returns the current time in milliseconds
 */
class LogThrottle(
    private val intervalMillis: Long,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    /**
     * When each key was last let through.
     */
    private val last = ConcurrentHashMap<String, Long>()

    /**
     * Decides whether an entry for a key may be logged now, and counts it as logged if so.
     *
     * @param key what the entry is about
     * @return `true` if the key was not let through within the interval
     */
    fun shouldLog(key: String): Boolean {
        val now = clock()
        var allowed = false
        last.compute(key) { _, previous ->
            if (previous == null || now - previous >= intervalMillis) {
                allowed = true
                now
            } else {
                previous
            }
        }
        return allowed
    }
}
