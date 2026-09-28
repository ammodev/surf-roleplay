package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CheckboxNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.SelectNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.NumberInputNode
import dev.slne.surf.roleplay.protocol.screen.Presentation
import dev.slne.surf.roleplay.protocol.screen.ScrollListNode
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [ScreenLayers], [PanelScroll] and [FocusOrder].
 */
class ScreenMechanicsTest {

    /**
     * Verifies that the visible layers start at the topmost full screen, or at the bottom if
     * there is none.
     */
    @Test
    fun `visible layers start at the topmost full screen`() {
        assertEquals(0..0, ScreenLayers.visible(listOf(Presentation.SCREEN)))
        assertEquals(1..2, ScreenLayers.visible(listOf(Presentation.SCREEN, Presentation.SCREEN, Presentation.DIALOG)))
        assertEquals(0..2, ScreenLayers.visible(listOf(Presentation.SCREEN, Presentation.SHEET, Presentation.DIALOG)))
        assertEquals(0..1, ScreenLayers.visible(listOf(Presentation.DIALOG, Presentation.DIALOG)))
        assertEquals(IntRange.EMPTY, ScreenLayers.visible(emptyList()))
    }

    /**
     * Verifies that the scroll offset stays within the content and is kept across a viewport
     * change.
     */
    @Test
    fun `panel scroll clamps to the content`() {
        val scroll = PanelScroll()
        scroll.update(contentHeight = 300, viewportHeight = 200)

        scroll.scrollBy(-500)
        assertEquals(100, scroll.offset)
        scroll.update(contentHeight = 300, viewportHeight = 250)
        assertEquals(50, scroll.offset)
        scroll.scrollBy(1000)
        assertEquals(0, scroll.offset)
    }

    /**
     * Verifies that scrolling reports whether it moved, so that exhausted inner lists hand the
     * wheel to the panel.
     */
    @Test
    fun `scroll reports whether it moved`() {
        val scroll = PanelScroll()
        scroll.update(contentHeight = 100, viewportHeight = 200)

        assertEquals(false, scroll.scrollBy(-10))
        scroll.update(contentHeight = 400, viewportHeight = 200)
        assertEquals(true, scroll.scrollBy(-10))
    }

    /**
     * Verifies that making a band visible scrolls just enough, in either direction.
     */
    @Test
    fun `ensure visible scrolls just enough`() {
        val scroll = PanelScroll()
        scroll.update(contentHeight = 500, viewportHeight = 100)

        scroll.ensureVisible(top = 250, bottom = 270)
        assertEquals(170, scroll.offset)
        scroll.ensureVisible(top = 180, bottom = 200)
        assertEquals(170, scroll.offset)
        scroll.ensureVisible(top = 40, bottom = 60)
        assertEquals(40, scroll.offset)
    }

    /**
     * A tree with a label, a text input, a disabled number input, a checkbox, a scroll list with a
     * button, a select and a final button.
     */
    private val tree = WidgetFactory.create(
        ColumnNode(
            "root",
            children = listOf(
                LabelNode("title"),
                TextInputNode("name"),
                NumberInputNode("age", enabled = false),
                CheckboxNode("agree"),
                ScrollListNode("list", children = listOf(ButtonNode("open"))),
                SelectNode("city"),
                ButtonNode("submit"),
            ),
        ),
    )

    /**
     * Verifies that focus moves through the enabled interactive widgets in tree order, skipping
     * labels and disabled widgets, and wraps at both ends.
     */
    @Test
    fun `focus moves in tree order and wraps`() {
        val order = generateSequence(FocusOrder.next(tree, null, backwards = false)) { FocusOrder.next(tree, it, backwards = false) }
            .take(6).map { it.id }.toList()

        assertEquals(listOf("name", "agree", "open", "city", "submit", "name"), order)
        assertEquals("submit", FocusOrder.next(tree, null, backwards = true)?.id)
        assertEquals("city", FocusOrder.next(tree, WidgetTree.find(tree, "submit"), backwards = true)?.id)
    }

    /**
     * Verifies that a focused widget that left the tree restarts the order at the beginning.
     */
    @Test
    fun `focus restarts when the focused widget left the tree`() {
        val stray = WidgetFactory.create(ButtonNode("gone"))

        assertEquals("name", FocusOrder.next(tree, stray, backwards = false)?.id)
    }

    /**
     * Verifies that a tree without interactive widgets has nothing to focus.
     */
    @Test
    fun `nothing to focus without interactive widgets`() {
        assertNull(FocusOrder.next(WidgetFactory.create(LabelNode("only")), null, backwards = false))
    }
}
