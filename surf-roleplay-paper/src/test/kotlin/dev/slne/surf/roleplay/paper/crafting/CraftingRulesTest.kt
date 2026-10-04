package dev.slne.surf.roleplay.paper.crafting

import org.bukkit.event.inventory.InventoryType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for the inventories that are blocked from opening because they craft.
 */
class CraftingRulesTest {

    /**
     * Verifies that crafting tables and crafters are blocked and other inventories are not.
     */
    @Test
    fun `crafting tables and crafters do not open`() {
        assertTrue(CraftingRules.blocksOpening(InventoryType.WORKBENCH))
        assertTrue(CraftingRules.blocksOpening(InventoryType.CRAFTER))
        assertFalse(CraftingRules.blocksOpening(InventoryType.CHEST))
    }
}
