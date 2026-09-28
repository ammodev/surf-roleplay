package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.TooltipPainter
import dev.slne.surf.roleplay.fabric.ui.TooltipRequest
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.HoverCardContentNode
import dev.slne.surf.roleplay.protocol.screen.HoverCardNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import dev.slne.surf.roleplay.protocol.screen.PopoverContentNode
import dev.slne.surf.roleplay.protocol.screen.PopoverNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import dev.slne.surf.roleplay.protocol.screen.TooltipNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for popovers, hover cards and tooltips in the mod.
 */
class PopoverWidgetsTest {

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
     * A listener that records actions.
     */
    private val listener = object : ScreenPanelListener {
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) {
            actions += widget.id
        }
        override fun closeRequested(panel: ScreenPanel) = Unit
    }

    /**
     * Verifies that a popover node creates a popover widget that is open when the node says so,
     * with its content placed on the asked side at its width.
     */
    @Test
    fun `popover opens as the node says`() {
        val node = PopoverNode(
            "popover",
            open = true,
            side = OverlaySide.BOTTOM,
            children = listOf(ButtonNode("trigger", text = "Open"), PopoverContentNode("content", width = Sizing.fixed(144), children = listOf(TextInputNode("name")))),
        )
        val panel = ScreenPanel("T", WidgetFactory.create(ColumnNode("root", children = listOf(node))), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))

        panel.layoutIfNeeded(measurer, 400, 300)
        val host = assertIs<PopoverWidget>(WidgetTree.find(panel.root, "popover"))
        assertSame(host, panel.popover?.owner)
        val area = panel.overlayAreas().single()
        assertEquals(144, area.width)
        assertTrue(area.y >= WidgetTree.find(panel.root, "trigger")!!.bounds.bottom)
    }

    /**
     * Verifies the hover timing: a hover card opens once the mouse rested long enough and closes
     * a while after it left, and coming back in time keeps it open.
     */
    @Test
    fun `hover timer opens and closes with delays`() {
        val timer = HoverTimer(openDelay = 700, closeDelay = 300)

        assertFalse(timer.update(0, hovered = true))
        assertFalse(timer.update(600, hovered = true))
        assertTrue(timer.update(700, hovered = true))
        assertTrue(timer.update(800, hovered = false))
        assertTrue(timer.update(1000, hovered = true))
        assertTrue(timer.update(1100, hovered = false))
        assertFalse(timer.update(1400, hovered = false))
    }

    /**
     * Verifies that a hover card node carries its delays into the widget.
     */
    @Test
    fun `hover card takes its delays`() {
        val card = assertIs<HoverCardWidget>(WidgetFactory.create(HoverCardNode("card", openDelay = 500, closeDelay = 100, children = listOf(LabelNode("name"), HoverCardContentNode("content")))))

        assertEquals(500, card.timer.openDelay)
        assertEquals(100, card.timer.closeDelay)
    }

    /**
     * Verifies that a tooltip is wanted while its trigger is hovered or focused, and not
     * otherwise.
     */
    @Test
    fun `tooltip follows hover and focus`() {
        val tooltip = assertIs<TooltipWidget>(WidgetFactory.create(TooltipNode("tip", text = "Hi", children = listOf(ButtonNode("trigger", text = "B")))))
        tooltip.bounds = Rect(10, 10, 20, 20)
        val trigger = WidgetTree.find(tooltip, "trigger")!!

        assertTrue(tooltip.wantsTooltip(null, 15, 15))
        assertFalse(tooltip.wantsTooltip(null, 50, 50))
        assertTrue(tooltip.wantsTooltip(trigger, 50, 50))
    }

    /**
     * Verifies that a tooltip box is centered above its anchor, clear of the arrow.
     */
    @Test
    fun `tooltip sits above its anchor`() {
        val area = TooltipPainter.area(measurer, TooltipRequest("Hint", Rect(100, 100, 40, 20), OverlaySide.TOP), Rect(0, 0, 400, 300))

        assertEquals(100 + 20 - area.width / 2, area.x, "centered")
        assertEquals(100 - TooltipPainter.ARROW - 1 - area.height, area.y)
    }

    /**
     * Verifies that a click on a button that triggers a hover card fires the button's action
     * instead of toggling the card.
     */
    @Test
    fun `hover card triggers keep their action`() {
        val node = HoverCardNode("card", children = listOf(ButtonNode("link", text = "Link"), HoverCardContentNode("content", children = listOf(LabelNode("bio")))))
        val panel = ScreenPanel("T", WidgetFactory.create(ColumnNode("root", children = listOf(node))), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
        panel.layoutIfNeeded(measurer, 400, 300)
        val b = WidgetTree.find(panel.root, "link")!!.bounds

        panel.mouseClicked(b.x + b.width / 2.0, b.y + b.height / 2.0, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT)

        assertEquals(listOf("link"), actions)
        assertEquals(null, panel.popover)
    }
}
