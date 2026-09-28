package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.screen.ScreenPatcher
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.fabric.ui.widget.ButtonWidget
import dev.slne.surf.roleplay.fabric.ui.widget.ContainerWidget
import dev.slne.surf.roleplay.fabric.ui.widget.OverlayContentWidget
import dev.slne.surf.roleplay.fabric.ui.widget.OverlayHostWidget
import dev.slne.surf.roleplay.fabric.ui.widget.Popover
import dev.slne.surf.roleplay.fabric.ui.widget.TextInputWidget
import dev.slne.surf.roleplay.fabric.ui.widget.TextEditState
import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetPopover
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import dev.slne.surf.roleplay.protocol.screen.SetOpen
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for the overlay stack of screen panels.
 */
class OverlayStackTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * The widgets whose actions reached the listener, in order.
     */
    private val actions = mutableListOf<String>()

    /**
     * The inputs whose changes reached the listener, in order.
     */
    private val changes = mutableListOf<String>()

    /**
     * A listener that records actions and changes.
     */
    private val listener = object : ScreenPanelListener {
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) {
            actions += widget.id
        }
        override fun closeRequested(panel: ScreenPanel) = Unit
        override fun valueChanged(panel: ScreenPanel, widget: Widget) {
            changes += "${widget.id}=${widget.inputValue}"
        }
    }

    /**
     * An overlay host that opens its content below its trigger.
     *
     * @param id the id of the host
     * @param modal whether the overlay is modal
     * @param dismissible whether a click outside a modal overlay closes it
     */
    private class TestHost(id: String, val modal: Boolean = false, val dismissible: Boolean = true) : OverlayHostWidget(id) {
        /**
         * Creates the overlay of this host.
         *
         * @return the overlay
         */
        override fun createPopover(): Popover =
            WidgetPopover(this, OverlaySide.BOTTOM, Align.START, modal = modal, dismissOnOutsideClick = dismissible)
    }

    /**
     * Creates a button that does not submit input.
     *
     * @param id the id of the button
     * @return the button
     */
    private fun button(id: String) = ButtonWidget(id, "B", submitsInput = false)

    /**
     * Creates a host with a trigger button and a content of widgets.
     *
     * @param id the id of the host
     * @param content the widgets of the content
     * @param modal whether the overlay is modal
     * @param dismissible whether a click outside a modal overlay closes it
     * @return the host
     */
    private fun host(id: String, content: List<Widget>, modal: Boolean = false, dismissible: Boolean = true): TestHost =
        TestHost(id, modal, dismissible).apply {
            childList += button("${id}_trigger")
            childList += OverlayContentWidget("${id}_content").apply { childList += content }
        }

    /**
     * Creates a panel around a root and lays it out in a 400 by 300 window.
     *
     * @param root the root of the tree
     * @return the panel
     */
    private fun panel(root: Widget): ScreenPanel =
        ScreenPanel("T", root, true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK))).also { it.layoutIfNeeded(measurer, 400, 300) }

    /**
     * Clicks the middle of a widget.
     *
     * @param panel the panel
     * @param id the id of the widget
     */
    private fun click(panel: ScreenPanel, id: String) {
        val bounds = findVisible(panel, id)
        panel.mouseClicked(bounds.x + bounds.width / 2.0, bounds.y + bounds.height / 2.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
    }

    /**
     * Returns the bounds a widget currently has, laying out the overlays first.
     *
     * @param panel the panel
     * @param id the id of the widget
     * @return the bounds
     */
    private fun findVisible(panel: ScreenPanel, id: String): Rect {
        panel.overlayAreas()
        return WidgetTree.find(panel.root, id)!!.bounds
    }

    /**
     * Returns a key event without modifiers.
     *
     * @param key the GLFW key code
     * @param shift whether Shift is held
     * @return the event
     */
    private fun key(key: Int, shift: Boolean = false) = KeyEvent(key, 0, if (shift) GLFW.GLFW_MOD_SHIFT else 0)

    /**
     * Creates a row of widgets, spaced so that overlays opened below them do not cover their
     * neighbours.
     *
     * @param children the widgets
     * @return the row
     */
    private fun column(vararg children: Widget) = ContainerWidget("root", Axis.HORIZONTAL).apply { childList += children; gap = 60 }

    /**
     * Verifies that a click on a trigger opens its overlay instead of firing an action, and a
     * second click closes it.
     */
    @Test
    fun `trigger toggles its overlay`() {
        val a = host("a", listOf(button("a_item")))
        val panel = panel(column(a, button("other")))

        click(panel, "a_trigger")
        assertSame(a, panel.popover?.owner)
        assertTrue(a.open)
        click(panel, "a_trigger")
        assertNull(panel.popover)
        assertFalse(a.open)
        assertEquals(emptyList(), actions)
    }

    /**
     * Verifies that a click outside an overlay closes it and still reaches the widget under the
     * mouse.
     */
    @Test
    fun `outside click closes and passes on`() {
        val panel = panel(column(host("a", listOf(button("a_item"))), button("other")))

        click(panel, "a_trigger")
        click(panel, "other")

        assertNull(panel.popover)
        assertEquals(listOf("other"), actions)
    }

    /**
     * Verifies that an overlay opened from inside another keeps it open, and a click in the lower
     * overlay closes only the one above it.
     */
    @Test
    fun `overlays stack`() {
        val inner = host("b", listOf(button("b_item")))
        val outer = host("a", listOf(button("a_item"), inner))
        val panel = panel(column(outer))

        click(panel, "a_trigger")
        click(panel, "b_trigger")
        assertEquals(listOf(outer, inner), panel.popovers.map { it.owner })

        click(panel, "a_item")
        assertEquals(listOf(outer), panel.popovers.map { it.owner })
        assertEquals(listOf("a_item"), actions)
    }

    /**
     * Verifies that Escape closes only the top overlay.
     */
    @Test
    fun `escape closes the top overlay`() {
        val outer = host("a", listOf(host("b", listOf(button("b_item")))))
        val panel = panel(column(outer))
        click(panel, "a_trigger")
        click(panel, "b_trigger")

        assertTrue(panel.keyPressed(key(GLFW.GLFW_KEY_ESCAPE)))

        assertEquals(listOf<Widget>(outer), panel.popovers.map { it.owner })
    }

    /**
     * Verifies that Tab stays inside a modal overlay.
     */
    @Test
    fun `tab stays inside a modal overlay`() {
        val panel = panel(column(host("a", listOf(button("x"), button("y")), modal = true), button("other")))
        click(panel, "a_trigger")

        assertEquals("x", panel.focusedWidget?.id)
        panel.keyPressed(key(GLFW.GLFW_KEY_TAB))
        assertEquals("y", panel.focusedWidget?.id)
        panel.keyPressed(key(GLFW.GLFW_KEY_TAB))
        assertEquals("x", panel.focusedWidget?.id)
    }

    /**
     * Verifies that a click on the backdrop of a modal overlay closes it only if it is
     * dismissible, and never reaches the widgets below.
     */
    @Test
    fun `backdrop clicks`() {
        val dismissible = panel(column(host("a", listOf(button("x")), modal = true), button("other")))
        click(dismissible, "a_trigger")
        click(dismissible, "other")
        assertNull(dismissible.popover)

        val fixed = panel(column(host("b", listOf(button("y")), modal = true, dismissible = false), button("other2")))
        click(fixed, "b_trigger")
        click(fixed, "other2")
        assertSame(WidgetTree.find(fixed.root, "b"), fixed.popover?.owner)
        assertEquals(emptyList(), actions)
    }

    /**
     * Verifies that the content of a closed overlay is not in the Tab order but its inputs still
     * report their values.
     */
    @Test
    fun `closed content is hidden but submitted`() {
        val input = TextInputWidget("name", TextEditState("Max"))
        val panel = panel(column(host("a", listOf(input)), button("other")))

        panel.keyPressed(key(GLFW.GLFW_KEY_TAB))
        assertEquals("a_trigger", panel.focusedWidget?.id)
        panel.keyPressed(key(GLFW.GLFW_KEY_TAB))
        assertEquals("other", panel.focusedWidget?.id)
        assertTrue(panel.inputValues().any { it.widgetId == "name" && it.value == "Max" })
    }

    /**
     * Verifies that an overlay's open state is an input value that reports its changes.
     */
    @Test
    fun `open state is reported`() {
        val a = host("a", listOf(button("x"))).apply { notifyChange = true }
        val panel = panel(column(a))

        click(panel, "a_trigger")
        click(panel, "a_trigger")

        assertEquals(listOf("a=true", "a=false"), changes)
    }

    /**
     * Verifies that a set-open patch opens and closes an overlay at the next layout.
     */
    @Test
    fun `set open patch opens the overlay`() {
        val a = host("a", listOf(button("x")))
        val panel = panel(column(a))

        ScreenPatcher.apply(panel.root, listOf(SetOpen("a", true)))
        panel.treeChanged()
        panel.layoutIfNeeded(measurer, 400, 300)
        assertSame(a, panel.popover?.owner)

        ScreenPatcher.apply(panel.root, listOf(SetOpen("a", false)))
        panel.treeChanged()
        panel.layoutIfNeeded(measurer, 400, 300)
        assertNull(panel.popover)
    }

    /**
     * Verifies that removing an overlay's host from the tree closes the overlay.
     */
    @Test
    fun `removed host closes its overlay`() {
        val panel = panel(column(host("a", listOf(button("x")))))
        click(panel, "a_trigger")

        (panel.root as ContainerWidget).childList.clear()
        panel.treeChanged()

        assertNull(panel.popover)
    }

    /**
     * Verifies the placement of an overlay: below its anchor, flipped above when there is no
     * room below, and kept inside the window.
     */
    @Test
    fun `placement flips and clamps`() {
        val window = Rect(0, 0, 400, 300)

        assertEquals(Rect(10, 34, 100, 50), OverlayPlacement.place(Rect(10, 10, 60, 20), Size(100, 50), window, OverlaySide.BOTTOM, Align.START))
        assertEquals(Rect(10, 196, 100, 50), OverlayPlacement.place(Rect(10, 250, 60, 20), Size(100, 50), window, OverlaySide.BOTTOM, Align.START))
        assertEquals(Rect(296, 34, 100, 50), OverlayPlacement.place(Rect(380, 10, 10, 20), Size(100, 50), window, OverlaySide.BOTTOM, Align.START))
        assertEquals(Rect(20, 34, 60, 50), OverlayPlacement.place(Rect(20, 10, 60, 20), Size(60, 50), window, OverlaySide.BOTTOM, Align.CENTER))
        assertEquals(Rect(84, 10, 100, 50), OverlayPlacement.place(Rect(20, 10, 60, 20), Size(100, 50), window, OverlaySide.RIGHT, Align.START))
    }

    /**
     * Verifies that the focus ring shows only after keyboard use, not after a mouse click.
     */
    @Test
    fun `focus ring shows only after keyboard use`() {
        val panel = panel(column(button("a"), button("b")))

        click(panel, "a")
        assertEquals("a", panel.focusedWidget?.id)
        assertFalse(panel.focusVisible)
        panel.keyPressed(key(GLFW.GLFW_KEY_TAB))
        assertTrue(panel.focusVisible)
        click(panel, "b")
        assertFalse(panel.focusVisible)
    }

    /**
     * Verifies that opening a modal overlay moves the focus into its content, and closing it
     * returns the focus to the trigger.
     */
    @Test
    fun `modals take the focus and give it back`() {
        val panel = panel(column(host("a", listOf(button("x"), button("y")), modal = true)))

        click(panel, "a_trigger")
        assertEquals("x", panel.focusedWidget?.id)
        panel.keyPressed(key(GLFW.GLFW_KEY_ESCAPE))

        assertEquals("a_trigger", panel.focusedWidget?.id)
    }

    /**
     * Verifies that closing an overlay while a widget inside it has the focus returns the focus to
     * the trigger instead of leaving it on hidden content.
     */
    @Test
    fun `closing an overlay does not leave the focus inside it`() {
        val panel = panel(column(host("a", listOf(button("x"))), button("other")))
        click(panel, "a_trigger")
        panel.focus(WidgetTree.find(panel.root, "x"))

        click(panel, "other")
        assertEquals("other", panel.focusedWidget?.id)
        click(panel, "a_trigger")
        panel.focus(WidgetTree.find(panel.root, "x"))
        panel.keyPressed(key(GLFW.GLFW_KEY_ESCAPE))

        assertEquals("a_trigger", panel.focusedWidget?.id)
    }
}
