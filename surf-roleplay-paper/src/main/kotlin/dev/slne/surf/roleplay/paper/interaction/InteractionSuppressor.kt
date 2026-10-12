package dev.slne.surf.roleplay.paper.interaction

import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Stops vanilla right-click interaction with bound blocks and entities, so that they are used only
 * through the target eye.
 *
 * @property service the interaction service that holds the bindings
 */
class InteractionSuppressor(private val service: PaperInteractionService) : Listener {

    /**
     * Denies the use of a bound block on right-click.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (service.bindings.forBlock(block) == null) return
        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)
    }

    /**
     * Cancels right-clicks on bound entities.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (service.bindings.forEntity(event.rightClicked) != null) event.isCancelled = true
    }

    /**
     * Cancels right-clicks at a position on bound entities, such as armor stand equipment swaps.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        if (service.bindings.forEntity(event.rightClicked) != null) event.isCancelled = true
    }
}
