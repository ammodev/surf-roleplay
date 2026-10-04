package dev.slne.surf.roleplay.paper.tablist

import java.util.UUID

/**
 * Collects tab list changes per player and releases each player for a push at most once per
 * window.
 *
 * A change marks the player dirty. A dirty player becomes due once the window, less the tolerance,
 * has passed since the player's last push; [due] then returns the player once and starts a new
 * window. The tolerance lets a periodic caller of [due] whose calls jitter around the end of the
 * window push on time instead of one period late. Changes made
 * within the window are kept and released when the window ends, so none is lost. Every method is
 * thread-safe.
 *
 * @property windowMillis the nominal time between two pushes to the same player
 * @property toleranceMillis how much earlier than the window a player may become due
 * @property clock returns the current time in milliseconds
 */
class TabListCoalescer(
    private val windowMillis: Long = DEFAULT_WINDOW_MILLIS,
    private val toleranceMillis: Long = 0,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    /**
     * The players with a change that was not pushed yet.
     */
    private val dirty = HashSet<UUID>()

    /**
     * The time of each player's last push.
     */
    private val lastPush = HashMap<UUID, Long>()

    /**
     * Marks a player's tab list as changed.
     *
     * @param player the player's unique id
     */
    @Synchronized
    fun markDirty(player: UUID) {
        dirty += player
    }

    /**
     * Marks the tab lists of several players as changed.
     *
     * @param players the players' unique ids
     */
    @Synchronized
    fun markDirty(players: Collection<UUID>) {
        dirty += players
    }

    /**
     * Returns the dirty players whose window has passed, clears their change and starts their next
     * window now.
     *
     * @return the players to push to now
     */
    @Synchronized
    fun due(): Set<UUID> {
        if (dirty.isEmpty()) return emptySet()
        val now = clock()
        val due = dirty.filterTo(HashSet()) { player -> lastPush[player]?.let { now - it >= windowMillis - toleranceMillis } ?: true }
        dirty -= due
        due.forEach { lastPush[it] = now }
        return due
    }

    /**
     * Records a push to a player made outside [due]: clears the player's change and starts the
     * player's next window now.
     *
     * @param player the player's unique id
     */
    @Synchronized
    fun pushed(player: UUID) {
        dirty -= player
        lastPush[player] = clock()
    }

    /**
     * Forgets a player who left.
     *
     * @param player the player's unique id
     */
    @Synchronized
    fun remove(player: UUID) {
        dirty -= player
        lastPush -= player
    }

    /**
     * Holds the default window.
     */
    companion object {
        /**
         * The default shortest time between two pushes to the same player.
         */
        const val DEFAULT_WINDOW_MILLIS: Long = 2000
    }
}
