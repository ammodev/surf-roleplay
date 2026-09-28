package dev.slne.surf.roleplay.fabric.screen

import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.SelectNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.ScreenStack
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [ScreenRules].
 */
class ScreenRulesTest {

    /**
     * Verifies that an open is stale only if it names a parent that is not open.
     */
    @Test
    fun `an open is stale only for a missing parent`() {
        val stack = ScreenStack<String>()
        stack.open(1, null, true, "a")

        assertFalse(ScreenRules.isStaleOpen(stack, null))
        assertFalse(ScreenRules.isStaleOpen(stack, 1))
        assertTrue(ScreenRules.isStaleOpen(stack, 2))
    }

    /**
     * Verifies that a widget stays usable only while it is in the tree and enabled.
     */
    @Test
    fun `widgets stay usable only while present and enabled`() {
        val root = WidgetFactory.create(ColumnNode("root", children = listOf(SelectNode("city"), LabelNode("title"))))
        val select = WidgetTree.find(root, "city")!!
        val replaced = WidgetFactory.create(SelectNode("city"))

        assertTrue(ScreenRules.isStillUsable(root, select))
        assertFalse(ScreenRules.isStillUsable(root, replaced))
        select.enabled = false
        assertFalse(ScreenRules.isStillUsable(root, select))
    }
}
