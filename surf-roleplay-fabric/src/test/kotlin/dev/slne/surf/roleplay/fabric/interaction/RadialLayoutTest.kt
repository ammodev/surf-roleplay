package dev.slne.surf.roleplay.fabric.interaction

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for the layout of one ring of the radial menu.
 */
class RadialLayoutTest {

    /**
     * Verifies that up to 8 entries fill the ring directly.
     */
    @Test
    fun `small levels fill the ring`() {
        assertEquals((0 until 8).map { RingItem.Node(it) }, RadialLayout.ring((0 until 8).toList()))
    }

    /**
     * Verifies that more than 8 entries become 7 entries plus "Mehr" with the rest.
     */
    @Test
    fun `large levels overflow into more`() {
        val ring = RadialLayout.ring((0 until 12).toList())

        assertEquals(8, ring.size)
        assertEquals((0 until 7).map { RingItem.Node(it) }, ring.take(7))
        assertEquals(RingItem.More((7 until 12).toList()), ring.last())
    }

    /**
     * Verifies which slot lies under a direction: slot 0 is at the top, slots go clockwise, and
     * the centre selects nothing.
     */
    @Test
    fun `hovered slot follows the direction`() {
        assertEquals(0, RadialLayout.hovered(0.0, -50.0, count = 4, innerRadius = 20.0))
        assertEquals(1, RadialLayout.hovered(50.0, 0.0, count = 4, innerRadius = 20.0))
        assertEquals(2, RadialLayout.hovered(0.0, 50.0, count = 4, innerRadius = 20.0))
        assertNull(RadialLayout.hovered(5.0, 5.0, count = 4, innerRadius = 20.0))
    }
}
