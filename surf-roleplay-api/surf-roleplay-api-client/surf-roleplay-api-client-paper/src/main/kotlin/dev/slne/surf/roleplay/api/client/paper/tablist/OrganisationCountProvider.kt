package dev.slne.surf.roleplay.api.client.paper.tablist

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * Counts the online members of one organisation for the tab list.
 */
interface OrganisationCountProvider {

    /** The stable identifier of the organisation. */
    val key: String

    /** The display name of the organisation. */
    val label: Component

    /** The icon name of the organisation, empty for none. */
    val icon: String

    /**
     * Tells whether a player counts as a member of the organisation. Reads in-memory state only.
     *
     * @param player the player to test
     * @return `true` if the player is counted
     */
    fun counts(player: Player): Boolean
}
