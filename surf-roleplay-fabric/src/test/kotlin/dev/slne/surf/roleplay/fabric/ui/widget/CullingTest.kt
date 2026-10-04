package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.HoverCardContentNode
import dev.slne.surf.roleplay.protocol.screen.HoverCardNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for skipping the widgets that lie outside the visible area.
 */
class CullingTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * A listener that ignores everything.
     */
    private val listener = object : ScreenPanelListener {
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) = Unit
        override fun closeRequested(panel: ScreenPanel) = Unit
    }

    /**
     * A leaf that counts the frames in which it was skipped.
     *
     * @param id the id of the widget
     * @param area the bounds of the widget
     */
    private class Probe(id: String, area: Rect) : Widget(id) {
        /**
         * The number of skipped frames.
         */
        var skipped: Int = 0

        init {
            bounds = area
        }

        /**
         * Returns no content size.
         *
         * @param measurer the text measurer
         * @return zero
         */
        override fun contentSize(measurer: TextMeasurer): Size = Size.ZERO

        /**
         * Draws nothing.
         *
         * @param ui the graphics to draw with
         * @param context the screen showing the widget
         * @param mouseX the mouse x position
         * @param mouseY the mouse y position
         */
        override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) = Unit

        /**
         * Counts the skipped frame.
         *
         * @param context the screen showing the widget
         */
        override fun renderSkipped(context: UiContext) {
            skipped++
        }
    }

    /**
     * Creates a laid-out panel around a node placed in a column.
     *
     * @param node the node
     * @return the panel
     */
    private fun panel(node: ScreenNode): ScreenPanel =
        ScreenPanel("T", WidgetFactory.create(ColumnNode("root", children = listOf(node))), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
            .also { it.layoutIfNeeded(measurer, 400, 300) }

    /**
     * Verifies that every child is drawn while nothing clips the drawing.
     */
    @Test
    fun `nothing is skipped without a clip`() {
        val children = listOf(Probe("a", Rect(0, 0, 10, 10)), Probe("b", Rect(5000, 5000, 10, 10)))

        assertEquals(children, childrenIntersecting(children, null))
    }

    /**
     * Verifies that children far outside the clip are skipped, while children that overlap it or
     * lie just beside it, where their edges may still show, are drawn.
     */
    @Test
    fun `children outside the clip are skipped`() {
        val clip = Rect(0, 100, 200, 100)
        val inside = Probe("inside", Rect(10, 120, 50, 20))
        val overlapping = Probe("overlapping", Rect(10, 90, 50, 20))
        val beside = Probe("beside", Rect(10, 200 + CULL_MARGIN - 1, 50, 20))
        val above = Probe("above", Rect(10, 0, 50, 20))
        val below = Probe("below", Rect(10, 400, 50, 20))
        val right = Probe("right", Rect(300, 120, 50, 20))

        assertEquals(listOf(inside, overlapping, beside), childrenIntersecting(listOf(inside, overlapping, beside, above, below, right), clip))
    }

    /**
     * Verifies that a skipped container passes the skipped frame on to its shown children, and
     * an overlay host only to its triggers, not to its content shown elsewhere.
     */
    @Test
    fun `skipped frames reach the children that would be drawn`() {
        val column = ContainerWidget("column", Axis.VERTICAL)
        val shown = Probe("shown", Rect.EMPTY)
        val hidden = Probe("hidden", Rect.EMPTY).also { it.hidden = true }
        column.childList += listOf(shown, hidden)
        val card = HoverCardWidget("card", OverlaySide.BOTTOM, Align.CENTER, 100, 100)
        val trigger = Probe("trigger", Rect.EMPTY)
        val content = OverlayContentWidget("content")
        val inContent = Probe("in_content", Rect.EMPTY)
        content.childList += inContent
        card.childList += listOf(trigger, content)
        column.childList += card
        val context = panel(LabelNode("x"))

        column.renderSkipped(context)

        assertEquals(1, shown.skipped)
        assertEquals(0, hidden.skipped)
        assertEquals(1, trigger.skipped)
        assertEquals(0, inContent.skipped)
    }

    /**
     * Verifies that a hover card whose trigger is not drawn treats the mouse as away from it and
     * closes its content.
     */
    @Test
    fun `skipped hover cards close`() {
        val panel = panel(HoverCardNode("card", openDelay = 100, closeDelay = 100, children = listOf(LabelNode("name", text = "Name"), HoverCardContentNode("content", children = listOf(LabelNode("bio"))))))
        val card = assertIs<HoverCardWidget>(WidgetTree.find(panel.root, "card"))
        val trigger = card.bounds
        card.pointer(panel, trigger.x + 1, trigger.y + 1, now = 0)
        card.pointer(panel, trigger.x + 1, trigger.y + 1, now = 100)
        assertTrue(card.open)

        card.renderSkipped(panel)

        assertFalse(card.open)
        assertNull(panel.popover)
    }
}
