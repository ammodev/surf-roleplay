package dev.slne.surf.roleplay.paper.interaction

import dev.slne.surf.roleplay.api.client.paper.interaction.BlockTarget
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionContext
import dev.slne.surf.roleplay.api.client.paper.interaction.TargetKind
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.BlockData
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
    private fun context(data: BlockData): Pair<Block, InteractionContext> {
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

    /**
     * Verifies that the action closes an open door.
     */
    @Test
    fun `action closes an open door`() {
        val data = mockk<Door>(relaxed = true) { every { isOpen } returns true }
        val (_, ctx) = context(data)

        DoorToggle.behaviour.action.run(ctx)

        verify { data.setOpen(false) }
    }

    /**
     * Verifies that the other half of a door receives the same open state.
     */
    @Test
    fun `action opens the other door half`() {
        val data = mockk<Door>(relaxed = true) {
            every { isOpen } returns false
            every { half } returns Bisected.Half.BOTTOM
        }
        val otherData = mockk<Door>(relaxed = true) { every { half } returns Bisected.Half.TOP }
        val other = mockk<Block>(relaxed = true) {
            every { blockData } returns otherData
            every { type } returns Material.OAK_DOOR
        }
        val (block, ctx) = context(data)
        every { block.type } returns Material.OAK_DOOR
        every { block.getRelative(BlockFace.UP) } returns other

        DoorToggle.behaviour.action.run(ctx)

        verify { otherData.setOpen(true) }
        verify { other.setBlockData(otherData) }
    }

    /**
     * Verifies that opening an iron door plays the iron door sound.
     */
    @Test
    fun `opening an iron door plays the iron sound`() {
        val data = mockk<Door>(relaxed = true) { every { isOpen } returns false }
        val (block, ctx) = context(data)
        val world = mockk<World>(relaxed = true)
        every { block.type } returns Material.IRON_DOOR
        every { block.world } returns world

        DoorToggle.behaviour.action.run(ctx)

        verify(exactly = 1) { world.playSound(any<Location>(), Sound.BLOCK_IRON_DOOR_OPEN, 1f, 1f) }
    }

    /**
     * Verifies that closing a wooden door plays the wooden door sound.
     */
    @Test
    fun `closing a wooden door plays the wooden sound`() {
        val data = mockk<Door>(relaxed = true) { every { isOpen } returns true }
        val (block, ctx) = context(data)
        val world = mockk<World>(relaxed = true)
        every { block.type } returns Material.OAK_DOOR
        every { block.world } returns world

        DoorToggle.behaviour.action.run(ctx)

        verify(exactly = 1) { world.playSound(any<Location>(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1f, 1f) }
    }
}
