package dev.slne.surf.roleplay.fabric.interaction

import dev.slne.surf.roleplay.protocol.interaction.InteractionLimits
import dev.slne.surf.roleplay.protocol.interaction.RadialNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for moving through the levels of the radial menu.
 */
class RadialNavigationTest {

    /** "Ich" > "Test" > "Hallo". */
    private val nodes = listOf(
        RadialNode(-1, "Ich", submenu = true),
        RadialNode(0, "Test", submenu = true),
        RadialNode(1, "Hallo"),
    )

    /**
     * Verifies that submenus open locally and an action returns its node index.
     */
    @Test
    fun `submenus open and actions are picked`() {
        val navigation = RadialNavigation(nodes)

        assertEquals(listOf(RingItem.Node(0)), navigation.items)
        assertNull(navigation.enter(RingItem.Node(0)))
        assertNull(navigation.enter(RingItem.Node(1)))
        assertEquals(listOf(RingItem.Node(2)), navigation.items)
        assertEquals(2, navigation.enter(RingItem.Node(2)))
    }

    /**
     * Verifies that back returns to the parent level and is refused at the top.
     */
    @Test
    fun `back returns to the parent level`() {
        val navigation = RadialNavigation(nodes)
        navigation.enter(RingItem.Node(0))

        assertTrue(navigation.back())
        assertEquals(listOf(RingItem.Node(0)), navigation.items)
        assertFalse(navigation.back())
    }

    /**
     * Verifies that "Mehr" opens the rest of a large level.
     */
    @Test
    fun `more opens the rest`() {
        val many = (0 until 10).map { RadialNode(-1, "E$it") }
        val navigation = RadialNavigation(many)
        val more = navigation.items.last()

        assertNull(navigation.enter(more))
        assertEquals((7 until 10).map { RingItem.Node(it) }, navigation.items)
    }

    /**
     * Verifies that a well-formed tree is valid, including the empty tree.
     */
    @Test
    fun `a well-formed tree is valid`() {
        assertTrue(RadialNavigation.isValid(nodes))
        assertTrue(RadialNavigation.isValid(emptyList()))
    }

    /**
     * Verifies that a tree over the node limit is invalid.
     */
    @Test
    fun `a tree over the node limit is invalid`() {
        val limit = InteractionLimits.MAX_RADIAL_NODES
        assertTrue(RadialNavigation.isValid(List(limit) { RadialNode(-1, "E$it") }))
        assertFalse(RadialNavigation.isValid(List(limit + 1) { RadialNode(-1, "E$it") }))
    }

    /**
     * Verifies that a parent that is not the top level or an earlier node makes the tree invalid.
     */
    @Test
    fun `a parent that is not an earlier node is invalid`() {
        assertFalse(RadialNavigation.isValid(listOf(RadialNode(0, "Self", submenu = true))))
        assertFalse(RadialNavigation.isValid(listOf(RadialNode(1, "Forward"), RadialNode(-1, "Top"))))
        assertFalse(RadialNavigation.isValid(listOf(RadialNode(-2, "Negative"))))
    }
}
