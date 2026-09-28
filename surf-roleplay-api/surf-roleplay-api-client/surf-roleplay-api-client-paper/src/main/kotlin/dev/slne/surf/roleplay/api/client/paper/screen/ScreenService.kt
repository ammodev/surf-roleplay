package dev.slne.surf.roleplay.api.client.paper.screen

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import org.bukkit.entity.Player

private val service = requiredService<ScreenService>()

/**
 * Opens and closes server-driven screens for players who run the roleplay client mod.
 *
 * Each player has a stack of open screens. Only the top screen is shown; closing it with Escape
 * shows the screen below it again. Every method must be called on the player's region thread.
 */
interface ScreenService {
    /**
     * Opens a generic screen for a player.
     *
     * Without a parent, every screen the player has open is closed first. With a parent, the
     * screens above the parent are closed and the new screen opens on top of it.
     *
     * @param player the player
     * @param definition the screen to open
     * @param parent the open screen of the same player to open on top of, or `null`
     * @return the open screen
     * @throws IllegalArgumentException if [parent] is not an open screen of [player]
     */
    fun open(player: Player, definition: ScreenDefinition, parent: OpenScreen? = null): OpenScreen

    /**
     * Returns the screens a player has open.
     *
     * @param player the player
     * @return the open screens, from bottom to top
     */
    fun openScreens(player: Player): List<OpenScreen>

    /**
     * Closes every screen a player has open.
     *
     * @param player the player
     */
    fun closeAll(player: Player)

    /** Provides access to the single registered [ScreenService]. */
    companion object : ScreenService by service {
        /** The single registered [ScreenService] instance. */
        val INSTANCE get() = service
    }
}
