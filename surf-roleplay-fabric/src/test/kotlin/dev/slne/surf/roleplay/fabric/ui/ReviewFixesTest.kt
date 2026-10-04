package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.widget.ScrollListWidget
import dev.slne.surf.roleplay.fabric.ui.widget.UiContext
import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.ScrollListNode
import dev.slne.surf.roleplay.protocol.screen.SizeMode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.fabric.screen.ScreenPatcher
import dev.slne.surf.roleplay.protocol.screen.RemoveNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for panel sizing, scroll list behaviour, focus after patches and key repeat filtering.
 */
class ReviewFixesTest {

    /**
     * A context that records nothing and focuses nothing.
     */
    private val context = object : UiContext {
        override val focusedWidget: Widget? = null
        override fun focus(widget: Widget?) = Unit
        override fun requestLayout() = Unit
        override fun actionTriggered(widget: Widget, submitsInput: Boolean) = Unit
        override var clipboard: String = ""
    }

    /**
     * A measurer with fixed text sizes, so that layouts run without Minecraft's font.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * Verifies that a growing root is never shorter than its content, so that tall content still
     * scrolls.
     */
    @Test
    fun `growing roots keep their content height`() {
        assertEquals(300, PanelSizing.contentHeight(SizeMode.GROW, preferred = 300, shortest = 300, available = 200))
        assertEquals(300, PanelSizing.contentHeight(SizeMode.GROW, preferred = 300, shortest = 250, available = 200))
        assertEquals(200, PanelSizing.contentHeight(SizeMode.GROW, preferred = 100, shortest = 100, available = 200))
        assertEquals(100, PanelSizing.contentHeight(SizeMode.FIT, preferred = 100, shortest = 100, available = 200))
    }

    /**
     * Verifies that a growing root whose content can become as short as the window fills exactly
     * the window height, and that other roots are not affected.
     */
    @Test
    fun `growing roots that can shrink fill the window height`() {
        assertEquals(200, PanelSizing.contentHeight(SizeMode.GROW, preferred = 900, shortest = 50, available = 200))
        assertEquals(200, PanelSizing.contentHeight(SizeMode.GROW, preferred = 900, shortest = 200, available = 200))
        assertEquals(900, PanelSizing.contentHeight(SizeMode.FIT, preferred = 900, shortest = 50, available = 200))
    }

    /**
     * Lays out a scroll list with ten rows of twenty pixels in a sixty pixel viewport.
     *
     * @return the list
     */
    private fun laidOutList(): ScrollListWidget {
        val list = WidgetFactory.create(
            ScrollListNode("list", height = Sizing.fixed(60), children = List(10) { ButtonNode("b$it", height = Sizing.fixed(20)) }),
        ) as ScrollListWidget
        val box = list.createLayout(measurer)
        FlexLayout.layout(box, Rect(0, 0, 100, 60))
        list.applyLayout()
        return list
    }

    /**
     * Verifies that a scroll list reports an exhausted scroll, so that the panel takes the wheel.
     */
    @Test
    fun `exhausted scroll lists hand the wheel on`() {
        val list = laidOutList()

        assertFalse(list.mouseScrolled(context, 10.0, 10.0, 1.0))
        assertTrue(list.mouseScrolled(context, 10.0, 10.0, -1.0))
    }

    /**
     * Verifies that a scroll list scrolls just enough to show a child and reports whether it moved.
     */
    @Test
    fun `scroll lists reveal their children`() {
        val list = laidOutList()
        val last = WidgetTree.find(list, "b9")!!

        assertTrue(list.ensureVisible(last))
        assertEquals(140, list.scrollOffset)
        assertFalse(list.ensureVisible(last))
    }

    /**
     * Verifies that the focus moves on from a widget that a patch removed.
     */
    @Test
    fun `focus continues after the focused widget was patched away`() {
        val root = WidgetFactory.create(ColumnNode("root", children = listOf(ButtonNode("a"), ButtonNode("b"), LabelNode("l"))))
        val removed = WidgetTree.find(root, "a")!!

        ScreenPatcher.apply(root, listOf(RemoveNode("a")))

        assertEquals("b", FocusOrder.next(root, removed, backwards = false)?.id)
    }

    /**
     * Verifies that a held activation key triggers once until it is released, while other keys
     * pass.
     */
    @Test
    fun `held activation keys trigger once`() {
        val filter = KeyRepeatFilter()

        assertTrue(filter.accept(257))
        assertFalse(filter.accept(257))
        filter.release(257)
        assertTrue(filter.accept(257))
        assertTrue(filter.accept(65))
        assertTrue(filter.accept(65))
    }

    /**
     * Verifies that the scroll bar handle never extends beyond a short viewport.
     */
    @Test
    fun `scroll handle fits short viewports`() {
        assertEquals(5, PanelSizing.handleHeight(viewport = 5, content = 50))
        assertEquals(20, PanelSizing.handleHeight(viewport = 40, content = 80))
    }
}
