package dev.slne.surf.roleplay.paper.interaction

import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the distance from the eyes to a target surface.
 */
class ReachTest {

    /**
     * Verifies that the distance is measured to the nearest face, edge or inside point.
     */
    @Test
    fun `distance to box surface`() {
        val block = BoundingBox(0.0, 64.0, 0.0, 1.0, 65.0, 1.0)

        assertEquals(2.0, Reach.distanceToBox(Vector(3.0, 64.5, 0.5), block), 1e-9)
        assertEquals(kotlin.math.sqrt(2.0), Reach.distanceToBox(Vector(2.0, 64.5, 2.0), block), 1e-9)
        assertEquals(0.0, Reach.distanceToBox(Vector(0.5, 64.5, 0.5), block), 1e-9)
    }
}
