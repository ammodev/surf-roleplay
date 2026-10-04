package dev.slne.surf.roleplay.fabric.vanilla

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the filtering of debug screen entries on the roleplay server.
 */
class HiddenDebugEntriesTest {

    /**
     * Verifies that hidden entries are dropped while the roleplay server is active.
     */
    @Test
    fun `hidden entries are dropped while active`() =
        assertEquals(listOf("fps"), HiddenDebugEntries.visible(listOf("fps", "player_position"), { it in HiddenDebugEntries.PATHS }, active = true).toList())

    /**
     * Verifies that nothing is dropped while the roleplay server is not active.
     */
    @Test
    fun `nothing is dropped while inactive`() =
        assertEquals(listOf("fps", "player_position"), HiddenDebugEntries.visible(listOf("fps", "player_position"), { it in HiddenDebugEntries.PATHS }, active = false).toList())

    /**
     * Verifies that the position entries are part of the hidden set.
     */
    @Test
    fun `position entries are hidden`() =
        assertTrue(setOf("player_position", "player_section_position", "looking_at_block_state", "looking_at_fluid_state").all { it in HiddenDebugEntries.PATHS })
}
