package dev.slne.surf.roleplay.api.client.paper.tablist

import org.bukkit.entity.Player

/**
 * Provides the personal values shown in the footer of a player's own tab list. A value that is
 * not provided is shown as unknown.
 *
 * Its functions may be called from any thread, including threads that do not own the player's
 * region. They must read only thread-safe in-memory state, never world or entity state, must not
 * block, and must return quickly.
 */
interface SelfInfoProvider {

    /**
     * Returns the name of the player's character.
     *
     * @param player the player
     * @return the character name, or `null` if unknown
     */
    fun characterName(player: Player): String? = null

    /**
     * Returns the player's job.
     *
     * @param player the player
     * @return the job, or `null` if none
     */
    fun job(player: Player): String? = null

    /**
     * Returns the player's rank.
     *
     * @param player the player
     * @return the rank, or `null` if none
     */
    fun rank(player: Player): String? = null
}
