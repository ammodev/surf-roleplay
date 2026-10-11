package dev.slne.surf.roleplay.fabric.interaction

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for the dwell before a target query is sent.
 */
class TargetDwellTest {

    /** A door. */
    private val door = TargetRef("block", "10,64,-3")

    /** Another door. */
    private val other = TargetRef("block", "11,64,-3")

    /**
     * Verifies that a target is queried once after resting for the dwell.
     */
    @Test
    fun `query once after the dwell`() {
        val dwell = TargetDwell(120)

        assertNull(dwell.update(0, door, paused = false))
        assertNull(dwell.update(100, door, paused = false))
        assertEquals(door, dwell.update(120, door, paused = false))
        assertNull(dwell.update(500, door, paused = false))
    }

    /**
     * Verifies that a change of target restarts the dwell.
     */
    @Test
    fun `changing target restarts the dwell`() {
        val dwell = TargetDwell(120)
        dwell.update(0, door, paused = false)

        assertNull(dwell.update(100, other, paused = false))
        assertNull(dwell.update(200, other, paused = false))
        assertEquals(other, dwell.update(220, other, paused = false))
    }

    /**
     * Verifies that no query is sent while paused over the list, and that the dwell restarts
     * afterwards for a new target.
     */
    @Test
    fun `paused over the list sends nothing`() {
        val dwell = TargetDwell(120)
        dwell.update(0, door, paused = false)

        assertNull(dwell.update(130, other, paused = true))
        assertNull(dwell.update(300, other, paused = false))
        assertEquals(other, dwell.update(420, other, paused = false))
    }

    /**
     * Verifies that a reset queries the same target again after the dwell.
     */
    @Test
    fun `reset queries again`() {
        val dwell = TargetDwell(120)
        dwell.update(0, door, paused = false)
        dwell.update(120, door, paused = false)
        dwell.reset()

        assertNull(dwell.update(130, door, paused = false))
        assertEquals(door, dwell.update(250, door, paused = false))
    }
}
