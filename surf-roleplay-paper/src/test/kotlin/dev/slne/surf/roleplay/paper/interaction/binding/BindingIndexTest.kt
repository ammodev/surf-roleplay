package dev.slne.surf.roleplay.paper.interaction.binding

import io.mockk.every
import io.mockk.mockk
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.TrapDoor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests for looking up the binding of a targeted block.
 */
class BindingIndexTest {

    /** A world named "world". */
    private val world = mockk<World> { every { name } returns "world" }

    /** The binding of the door's lower half. */
    private val binding = TargetBinding(listOf(BehaviourBinding("door.toggle", emptyMap())), null)

    /**
     * Creates a block mock.
     *
     * @param y the y coordinate
     * @param half the door half, or `null` for a block that is not two blocks tall
     * @return the block
     */
    private fun block(y: Int, half: Bisected.Half?): Block = mockk(relaxed = true) {
        every { this@mockk.world } returns this@BindingIndexTest.world
        every { x } returns 10
        every { this@mockk.y } returns y
        every { z } returns -3
        every { blockData } returns if (half == null) mockk(relaxed = true) else mockk<Door> { every { this@mockk.half } returns half }
    }

    /**
     * Verifies that the upper half of a door resolves to the binding of its lower half.
     */
    @Test
    fun `upper door half resolves to the lower half`() {
        val lower = block(64, Bisected.Half.BOTTOM)
        val upper = block(65, Bisected.Half.TOP)
        every { upper.getRelative(BlockFace.DOWN) } returns lower
        val index = BindingIndex().apply { bindings = Bindings(mapOf(BlockPosition("world", 10, 64, -3) to binding), emptyMap()) }

        assertEquals(lower to binding, index.forBlock(lower))
        val (base, found) = index.forBlock(upper)!!
        assertSame(lower, base)
        assertEquals(binding, found)
    }

    /**
     * Verifies that a top-half trapdoor is looked up at its own position.
     */
    @Test
    fun `top half trapdoor resolves to itself`() {
        val trapdoor = block(64, null)
        every { trapdoor.blockData } returns mockk<TrapDoor> { every { half } returns Bisected.Half.TOP }
        val index = BindingIndex().apply { bindings = Bindings(mapOf(BlockPosition("world", 10, 64, -3) to binding), emptyMap()) }

        assertEquals(trapdoor to binding, index.forBlock(trapdoor))
    }

    /**
     * Verifies that an upper stair is not redirected to the block below it.
     */
    @Test
    fun `upper stair does not resolve to the block below`() {
        val below = block(64, null)
        val stair = block(65, null)
        every { stair.blockData } returns mockk<Stairs> { every { half } returns Bisected.Half.TOP }
        every { stair.getRelative(BlockFace.DOWN) } returns below
        val index = BindingIndex().apply { bindings = Bindings(mapOf(BlockPosition("world", 10, 64, -3) to binding), emptyMap()) }

        assertNull(index.forBlock(stair))
    }

    /**
     * Verifies that an unbound block has no binding.
     */
    @Test
    fun `unbound blocks have no binding`() {
        assertNull(BindingIndex().forBlock(block(70, null)))
    }
}
