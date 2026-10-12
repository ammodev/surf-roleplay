package dev.slne.surf.roleplay.fabric.interaction

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for placing the option list next to the crosshair.
 */
class OptionListLayoutTest {

    /** A 400 x 240 GUI window. */
    private val window = Rect(0, 0, 400, 240)

    /**
     * Verifies that rows stack right of the anchor, centred vertically on it.
     */
    @Test
    fun `rows stack beside the anchor`() {
        val rows = OptionListLayout.place(count = 3, rowHeight = 14, width = 80, anchorX = 200, anchorY = 120, window = window)

        assertEquals(listOf(Rect(212, 99, 80, 14), Rect(212, 113, 80, 14), Rect(212, 127, 80, 14)), rows)
    }

    /**
     * Verifies that the list moves left of the anchor when it would leave the window.
     */
    @Test
    fun `list flips at the right edge`() {
        val rows = OptionListLayout.place(count = 1, rowHeight = 14, width = 80, anchorX = 360, anchorY = 120, window = window)

        assertEquals(268, rows.single().x)
    }

    /**
     * Verifies hit testing of rows.
     */
    @Test
    fun `index at a point`() {
        val rows = OptionListLayout.place(count = 2, rowHeight = 14, width = 80, anchorX = 200, anchorY = 120, window = window)

        assertEquals(1, OptionListLayout.indexAt(rows, 220.0, 125.0))
        assertNull(OptionListLayout.indexAt(rows, 10.0, 10.0))
    }
}
