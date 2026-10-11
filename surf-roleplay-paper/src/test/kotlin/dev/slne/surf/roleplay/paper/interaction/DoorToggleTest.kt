package dev.slne.surf.roleplay.paper.interaction

import dev.slne.surf.roleplay.api.client.paper.interaction.BlockTarget
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionContext
import dev.slne.surf.roleplay.api.client.paper.interaction.TargetKind
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.block.Block
import org.bukkit.block.data.type.Door
import org.bukkit.entity.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for the door toggle behaviour.
 */
class DoorToggleTest {

    /**
     * Creates a context for a block with block data.
     *
     * @param data the block data
     * @return the block and its context
     */
    private fun context(data: org.bukkit.block.data.BlockData): Pair<Block, InteractionContext> {
        val block = mockk<Block>(relaxed = true) { every { blockData } returns data }
        return block to InteractionContext(mockk<Player>(relaxed = true), BlockTarget(block), emptyMap())
    }

    /**
     * Verifies the key, the kinds and the German labels.
     */
    @Test
    fun `labels follow the door state`() {
        assertEquals("door.toggle", DoorToggle.behaviour.key)
        assertEquals(setOf(TargetKind.BLOCK), DoorToggle.behaviour.kinds)
        assertEquals("Schließen", DoorToggle.label(open = true))
        assertEquals("Öffnen", DoorToggle.label(open = false))
    }

    /**
     * Verifies that the condition passes only for openable blocks.
     */
    @Test
    fun `condition needs an openable block`() {
        val (_, door) = context(mockk<Door>(relaxed = true))
        val (_, stone) = context(mockk(relaxed = true))

        assertTrue(DoorToggle.behaviour.conditions.all { it.test(door) })
        assertFalse(DoorToggle.behaviour.conditions.all { it.test(stone) })
    }

    /**
     * Verifies that the action flips the open state and writes the data back.
     */
    @Test
    fun `action flips the open state`() {
        val data = mockk<Door>(relaxed = true) { every { isOpen } returns false }
        val (block, ctx) = context(data)

        DoorToggle.behaviour.action.run(ctx)

        verify { data.setOpen(true) }
        verify { block.setBlockData(data) }
    }
}
