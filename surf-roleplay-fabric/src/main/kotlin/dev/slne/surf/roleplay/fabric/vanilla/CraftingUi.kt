package dev.slne.surf.roleplay.fabric.vanilla

/**
 * The crafting area of the vanilla player inventory screen, and the rule that hides its slots
 * while the roleplay server is active.
 */
object CraftingUi {

    /**
     * The left edge of the crafting area in the inventory texture, which covers the 2x2 grid, the
     * arrow and the result slot to the right of the player model.
     */
    const val GRID_X: Int = 97

    /**
     * The top edge of the crafting area in the inventory texture.
     */
    const val GRID_Y: Int = 17

    /**
     * The width of the crafting area in pixels, from the left edge of the grid to the right edge of
     * the result slot.
     */
    const val GRID_WIDTH: Int = 74

    /**
     * The height of the crafting area in pixels, which is the height of the 2x2 grid.
     */
    const val GRID_HEIGHT: Int = 36

    /**
     * The ARGB colour of the inventory panel background that the crafting area is covered with.
     */
    const val PANEL_COLOR: Int = 0xFFC6C6C6.toInt()

    /**
     * Returns whether a slot is hidden: a crafting grid slot or crafting result slot of the player's
     * own inventory menu, while the roleplay server is active.
     *
     * @param isCraftingSlot whether the slot belongs to a crafting grid
     * @param isResultSlot whether the slot is a crafting result slot
     * @param inPlayerInventoryMenu whether the slot belongs to the player's own inventory menu
     * @param active whether the roleplay server is active
     * @return true if the slot is neither drawn nor usable
     */
    fun hidesSlot(isCraftingSlot: Boolean, isResultSlot: Boolean, inPlayerInventoryMenu: Boolean, active: Boolean): Boolean =
        active && inPlayerInventoryMenu && (isCraftingSlot || isResultSlot)
}
