package dev.slne.surf.roleplay.paper.screen

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Limits how many screen actions each player may send per second.
 *
 * @property maxPerSecond the largest number of actions a player may send within any second
 * @property clock returns the current time in milliseconds
 */
class ActionRateLimiter(
    private val maxPerSecond: Int,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    /**
     * The times of each player's recent actions, oldest first.
     */
    private val actions = ConcurrentHashMap<UUID, ArrayDeque<Long>>()

    /**
     * Records an action of a player if the player is within the limit.
     *
     * @param player the player's unique id
     * @return whether the action is allowed
     */
    fun tryAcquire(player: UUID): Boolean {
        val now = clock()
        val times = actions.computeIfAbsent(player) { ArrayDeque() }
        synchronized(times) {
            while (times.isNotEmpty() && now - times.first() >= WINDOW_MILLIS) times.removeFirst()
            if (times.size >= maxPerSecond) return false
            times.addLast(now)
            return true
        }
    }

    /**
     * Forgets a player's recent actions.
     *
     * @param player the player's unique id
     */
    fun forget(player: UUID) {
        actions.remove(player)
    }

    /**
     * Holds the window length.
     */
    private companion object {
        /**
         * The length of the window the limit applies to.
         */
        const val WINDOW_MILLIS: Long = 1000
    }
}
