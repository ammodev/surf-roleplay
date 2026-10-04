package dev.slne.surf.roleplay.api.client.paper.tablist

import dev.slne.surf.api.core.util.requiredService
import org.bukkit.entity.Player

/**
 * The registered tab list service.
 */
private val service = requiredService<TabListService>()

/**
 * Feeds and refreshes the roleplay tab list shown to players whose client runs the roleplay mod.
 */
interface TabListService {

    /**
     * Registers an organisation whose online members are counted in the tab list.
     *
     * @param provider the provider of the organisation
     */
    fun registerOrganisation(provider: OrganisationCountProvider)

    /**
     * Registers a provider of the personal values in the footer of the tab list.
     *
     * @param provider the provider
     */
    fun registerSelfInfo(provider: SelfInfoProvider)

    /**
     * Marks the state of every player as changed so it is pushed soon.
     */
    fun changed()

    /**
     * Marks the state of one player as changed so it is pushed soon.
     *
     * @param player the player
     */
    fun changed(player: Player)

    /** Provides access to the single registered [TabListService]. */
    companion object : TabListService by service {
        /** The single registered [TabListService] instance. */
        val INSTANCE: TabListService get() = service
    }
}
