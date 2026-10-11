package dev.slne.surf.roleplay.paper.interaction

import dev.slne.surf.roleplay.api.client.paper.interaction.RadialAction
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialCategory
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialCondition
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialSubmenu
import dev.slne.surf.roleplay.protocol.interaction.InteractionLimits
import io.mockk.mockk
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for building the radial tree of a player.
 */
class RadialTreeBuilderTest {

    /** The player the tree is built for. */
    private val player = mockk<Player>(relaxed = true)

    /**
     * Renders a component as plain text for assertions.
     *
     * @param component the component
     * @return the text
     */
    private fun plain(component: Component) = PlainTextComponentSerializer.plainText().serialize(component)

    /**
     * Builds a tree from entries.
     *
     * @param entries the entries with their categories
     * @return the built tree
     */
    private fun build(vararg entries: Pair<RadialCategory, dev.slne.surf.roleplay.api.client.paper.interaction.RadialEntry>) =
        RadialTreeBuilder.build(entries.toList(), player, ::plain) { _, _ -> }

    /**
     * Verifies that categories become top-level submenus, children follow their parents, and the
     * path of every action is recorded.
     */
    @Test
    fun `tree is breadth first with parent links`() {
        val hello = RadialAction("demo.hello", Component.text("Hallo")) { }
        val built = build(RadialCategory.SELF to RadialSubmenu("demo", Component.text("Test"), children = listOf(hello)))

        assertEquals(listOf("Ich", "Test", "Hallo"), built.nodes.map { it.label })
        assertEquals(listOf(-1, 0, 1), built.nodes.map { it.parent })
        assertEquals(listOf(true, true, false), built.nodes.map { it.submenu })
        assertEquals(hello, built.actions[2]?.last())
        assertEquals(0, built.dropped)
    }

    /**
     * Verifies that hidden entries, empty submenus and empty categories are left out.
     */
    @Test
    fun `hidden and empty entries are left out`() {
        val hidden = RadialAction("a.hidden", Component.text("Versteckt"), conditions = listOf(RadialCondition { false })) { }
        val emptyMenu = RadialSubmenu("b.empty", Component.text("Leer"), children = listOf(hidden))

        assertEquals(emptyList(), build(RadialCategory.JOB to hidden, RadialCategory.SELF to emptyMenu).nodes)
    }

    /**
     * Verifies that a tree with more than the node limit is cut and the cut is counted.
     */
    @Test
    fun `large trees are cut at the node limit`() {
        val entries = (1..200).map { RadialCategory.SELF to RadialAction("a.e$it", Component.text("E$it")) { } }
        val built = RadialTreeBuilder.build(entries, player, ::plain) { _, _ -> }

        assertEquals(InteractionLimits.MAX_RADIAL_NODES, built.nodes.size)
        assertEquals(201 - InteractionLimits.MAX_RADIAL_NODES, built.dropped)
        assertTrue(built.nodes.withIndex().all { (i, node) -> node.parent < i })
    }
}
