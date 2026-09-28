package dev.slne.surf.roleplay.api.client.paper.toast

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.roleplay.api.client.common.toast.Toast
import org.bukkit.entity.Player

/**
 * The registered toast service.
 */
private val service = requiredService<ToastService>()

/**
 * Shows toasts to players whose client runs the roleplay mod.
 */
interface ToastService {

    /**
     * Shows a toast to a player, or replaces the shown toast with the same id. A toast for a
     * player whose client is not ready yet is dropped.
     *
     * @param player the player
     * @param toast the toast
     * @return the id of the toast
     */
    fun show(player: Player, toast: Toast): String

    /**
     * Removes a shown toast. Its buttons no longer run their handlers.
     *
     * @param player the player
     * @param id the id of the toast
     */
    fun dismiss(player: Player, id: String)

    /** Provides access to the single registered [ToastService]. */
    companion object : ToastService by service {
        /** The single registered [ToastService] instance. */
        val INSTANCE: ToastService get() = service
    }
}
