package dev.slne.surf.roleplay.api.client.paper.interaction

import dev.slne.surf.api.core.util.requiredService
import org.bukkit.entity.Player

/**
 * The registered interaction service.
 */
private val service = requiredService<InteractionService>()

/**
 * Registers what players can do through the target eye and the radial menu.
 */
interface InteractionService {

    /**
     * Registers a behaviour that bindings can refer to by its key.
     *
     * @param behaviour the behaviour
     * @throws IllegalArgumentException if a behaviour with the same key is registered
     */
    fun registerBehaviour(behaviour: InteractionBehaviour)

    /**
     * Adds an entry to a category of the radial menu.
     *
     * @param category the category
     * @param entry the entry
     */
    fun registerRadialEntry(category: RadialCategory, entry: RadialEntry)

    /**
     * Adds a contribution to the interaction-blocked predicate. The contributing system calls
     * [refreshAvailability] whenever its answer for a player may have changed.
     *
     * @param blocker the contribution
     */
    fun addBlocker(blocker: InteractionBlocker)

    /**
     * Re-evaluates the interaction-blocked predicate for a player and tells their client if the
     * result changed. Safe to call from any thread.
     *
     * @param player the player
     */
    fun refreshAvailability(player: Player)

    /** Provides access to the single registered [InteractionService]. */
    companion object : InteractionService by service {
        /** The single registered [InteractionService] instance. */
        val INSTANCE: InteractionService get() = service
    }
}
