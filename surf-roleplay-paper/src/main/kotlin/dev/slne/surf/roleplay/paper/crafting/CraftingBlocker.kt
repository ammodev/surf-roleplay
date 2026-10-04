package dev.slne.surf.roleplay.paper.crafting

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.CrafterCraftEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRecipeDiscoverEvent

/**
 * Blocks vanilla crafting: crafting grids never produce a result, crafting tables and crafters do
 * not open, crafters do not craft, recipe book placement requests are ignored and recipes are
 * never discovered.
 *
 * The listener holds no state and touches only the objects of the event it handles, so it is safe
 * on every region thread.
 */
class CraftingBlocker : Listener {

    /**
     * Clears the result of every crafting grid, including the 2x2 grid of the player inventory.
     *
     * @param event the event fired when a crafting grid changes
     */
    @EventHandler
    fun onPrepareItemCraft(event: PrepareItemCraftEvent) {
        event.inventory.result = null
    }

    /**
     * Cancels taking a crafted item out of a result slot.
     *
     * @param event the event fired when a player takes a crafting result
     */
    @EventHandler
    fun onCraftItem(event: CraftItemEvent) {
        event.isCancelled = true
    }

    /**
     * Keeps crafting tables and crafters from opening.
     *
     * @param event the event fired when an inventory is about to open
     */
    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (CraftingRules.blocksOpening(event.inventory.type)) {
            event.isCancelled = true
        }
    }

    /**
     * Keeps crafters from crafting when powered.
     *
     * @param event the event fired when a crafter is about to craft
     */
    @EventHandler
    fun onCrafterCraft(event: CrafterCraftEvent) {
        event.isCancelled = true
    }

    /**
     * Ignores requests to place a recipe from the recipe book into a crafting grid.
     *
     * @param event the event fired when a player clicks a recipe in the recipe book
     */
    @EventHandler
    fun onRecipeBookClick(event: PlayerRecipeBookClickEvent) {
        event.isCancelled = true
    }

    /**
     * Keeps players from discovering recipes, so no recipe toasts appear.
     *
     * @param event the event fired when a player is about to discover a recipe
     */
    @EventHandler
    fun onRecipeDiscover(event: PlayerRecipeDiscoverEvent) {
        event.isCancelled = true
    }

    /**
     * Removes every recipe a joining player discovered earlier.
     *
     * @param event the event fired when a player joins the server
     */
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        player.undiscoverRecipes(player.discoveredRecipes)
    }
}
