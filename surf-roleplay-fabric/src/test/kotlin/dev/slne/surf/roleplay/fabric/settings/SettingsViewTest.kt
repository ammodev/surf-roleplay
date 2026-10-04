package dev.slne.surf.roleplay.fabric.settings

import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.ContainerNode
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TabsContentNode
import dev.slne.surf.roleplay.protocol.screen.TabsNode
import dev.slne.surf.roleplay.protocol.screen.TabsTriggerNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the node tree of the settings screen.
 */
class SettingsViewTest {

    /**
     * One binding row on its default key.
     */
    private val rows = listOf(BindingRow("hud_cursor", "Mauszeiger im HUD", "Links Alt", isDefault = true, unbound = false))

    /**
     * Returns this node and every node below it.
     *
     * @return the nodes, depth first
     */
    private fun ScreenNode.all(): Sequence<ScreenNode> =
        sequenceOf(this) + ((this as? ContainerNode)?.children?.asSequence()?.flatMap { it.all() } ?: emptySequence())

    /**
     * Verifies that every binding has a key button and a reset button, and that the screen has
     * the reset-all button and the cursor mode select.
     */
    @Test
    fun `every binding has a key button and a reset button`() {
        val ids = SettingsView.build(rows, emptySet(), null, ClientSettings()).all().map { it.id }.toSet()
        assertTrue("binding_hud_cursor" in ids && "reset_hud_cursor" in ids && "reset_all" in ids && "cursor_mode" in ids)
    }

    /**
     * Verifies that the binding being captured shows the key prompt.
     */
    @Test
    fun `capturing shows the prompt on the binding`() {
        val text = SettingsView.build(rows, emptySet(), "hud_cursor", ClientSettings()).all().first { it.id == "binding_hud_cursor" }.toString()
        assertTrue("Taste drücken" in text)
    }

    /**
     * Verifies that a conflicting binding is marked.
     */
    @Test
    fun `conflicts are marked`() {
        val ids = SettingsView.build(rows, setOf("hud_cursor"), null, ClientSettings()).all().map { it.id }.toSet()
        assertTrue("conflict_hud_cursor" in ids)
    }

    /**
     * Verifies that the tree does not repeat the screen title, which the panel's title bar shows.
     */
    @Test
    fun `the tree does not repeat the title`() {
        val texts = SettingsView.build(rows, emptySet(), null, ClientSettings()).all().map { it.toString() }
        assertTrue(texts.none { "Roleplay-Einstellungen" in it })
    }

    /**
     * Verifies that the tree is vertical tabs of a fixed size and that the binding rows sit in a
     * column with a gap between them.
     */
    @Test
    fun `the tabs are vertical and the binding rows are spaced`() {
        val tree = SettingsView.build(rows, emptySet(), null, ClientSettings()) as TabsNode
        assertEquals(Orientation.VERTICAL, tree.orientation)
        assertEquals(Sizing.fixed(390), tree.width)
        assertEquals(Sizing.fixed(190), tree.height)
        val column = tree.all().first { it.id == "binding_rows" } as ColumnNode
        assertEquals(6, column.gap)
        assertTrue(column.children.any { it.id == "row_hud_cursor" })
    }

    /**
     * Verifies that every category is a tab trigger with a content, and that the tabs report
     * their changes.
     */
    @Test
    fun `both categories are tab triggers`() {
        val tree = SettingsView.build(rows, emptySet(), null, ClientSettings()) as TabsNode
        val triggers = tree.all().filterIsInstance<TabsTriggerNode>().toList()
        assertEquals(listOf("controls", "cursor"), triggers.map { it.value })
        assertEquals(listOf("keyboard", "mouse-pointer"), triggers.map { it.icon })
        assertEquals(listOf("Steuerung", "Mauszeiger"), SettingsView.categories.map { it.title })
        assertEquals(listOf(SettingsView.text("Steuerung"), SettingsView.text("Mauszeiger")), triggers.map { it.text })
        assertEquals(listOf("controls", "cursor"), tree.all().filterIsInstance<TabsContentNode>().map { it.value }.toList())
        assertTrue(tree.notifyChange)
    }

    /**
     * Verifies that each category's content holds its own widgets.
     */
    @Test
    fun `each category holds its own content`() {
        val tree = SettingsView.build(rows, emptySet(), null, ClientSettings()) as TabsNode
        val contents = tree.children.filterIsInstance<TabsContentNode>().associateBy { it.value }
        val controls = contents.getValue("controls").all().map { it.id }.toSet()
        val cursor = contents.getValue("cursor").all().map { it.id }.toSet()
        assertTrue("binding_hud_cursor" in controls && "reset_all" in controls && "cursor_mode" !in controls)
        assertTrue("cursor_mode" in cursor && "binding_hud_cursor" !in cursor)
    }

    /**
     * Verifies that the selected category is the one given, and the first one by default.
     */
    @Test
    fun `the selected category is preserved`() {
        assertEquals("controls", (SettingsView.build(rows, emptySet(), null, ClientSettings()) as TabsNode).value)
        assertEquals("cursor", (SettingsView.build(rows, emptySet(), null, ClientSettings(), "cursor") as TabsNode).value)
    }
}
