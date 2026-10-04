package dev.slne.surf.roleplay.fabric.vanilla

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for hiding the crafting slots of the player inventory on the roleplay server.
 */
class CraftingUiTest {

    /**
     * Verifies that the crafting grid and result slots of the player inventory are hidden only
     * while the roleplay server is active, and other slots never are.
     */
    @Test
    fun `grid and result slots of the player inventory are hidden while active`() {
        assertTrue(CraftingUi.hidesSlot(isCraftingSlot = true, isResultSlot = false, inPlayerInventoryMenu = true, active = true))
        assertTrue(CraftingUi.hidesSlot(false, true, true, true))
        assertFalse(CraftingUi.hidesSlot(false, false, true, true))
        assertFalse(CraftingUi.hidesSlot(true, false, true, false))
    }
}
