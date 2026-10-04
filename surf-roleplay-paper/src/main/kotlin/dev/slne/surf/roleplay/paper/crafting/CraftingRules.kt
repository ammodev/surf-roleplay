package dev.slne.surf.roleplay.paper.crafting

import org.bukkit.event.inventory.InventoryType

/**
 * The rules that decide which vanilla crafting features the roleplay server blocks.
 */
object CraftingRules {

    /**
     * Returns whether an inventory of [type] is kept from opening because it crafts items.
     *
     * @param type the type of the inventory that is about to open
     * @return true for crafting tables and crafters, false for every other inventory
     */
    fun blocksOpening(type: InventoryType): Boolean =
        type == InventoryType.WORKBENCH || type == InventoryType.CRAFTER
}
