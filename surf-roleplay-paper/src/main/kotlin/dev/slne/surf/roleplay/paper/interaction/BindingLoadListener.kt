package dev.slne.surf.roleplay.paper.interaction

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import dev.slne.surf.roleplay.paper.plugin
import org.bukkit.event.server.ServerLoadEvent

/**
 * Loads the interaction bindings once the server has finished loading, so that behaviours
 * registered by plugins that enable after this one are known to the binding parser.
 */
class BindingLoadListener : Listener {

    /**
     * Loads the bindings.
     *
     * @param event the server load event
     */
    @EventHandler
    fun onServerLoad(event: ServerLoadEvent) {
        PaperInteractionService.INSTANCE.reloadBindings(plugin)
    }
}
