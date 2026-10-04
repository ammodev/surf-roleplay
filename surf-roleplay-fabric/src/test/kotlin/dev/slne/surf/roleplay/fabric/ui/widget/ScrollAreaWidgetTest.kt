package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.ScrollAreaNode
import dev.slne.surf.roleplay.protocol.screen.ScrollOrientation
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for scroll areas in the mod.
 */
class ScrollAreaWidgetTest {

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
     * Creates a laid-out panel around nodes placed in a column.
     *
     * @param nodes the nodes
     * @return the panel
     */
    private fun panel(vararg nodes: ScreenNode): ScreenPanel =
        ScreenPanel("T", WidgetFactory.create(ColumnNode("root", children = nodes.toList())), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
            .also { it.layoutIfNeeded(measurer, 400, 300) }

    /**
     * Returns the widget with an id.
     *
     * @param panel the panel
     * @param id the id
     * @return the widget
     */
    private fun widget(panel: ScreenPanel, id: String): Widget = WidgetTree.find(panel.root, id)!!

    /**
     * Verifies that a root that grows in height and holds a growing scroll area fills the window
     * height exactly: the elements outside the area keep their place and only the area scrolls.
     */
    @Test
    fun `a growing area in a growing root scrolls instead of the panel`() {
        val root = ColumnNode(
            "root",
            width = Sizing.grow(),
            height = Sizing.grow(),
            children = listOf(
                LabelNode("header", text = "Kopf"),
                ScrollAreaNode("area", width = Sizing.grow(), height = Sizing.grow(), children = List(40) { ButtonNode("b$it", text = "B$it", height = Sizing.fixed(20)) }),
            ),
        )
        val panel = ScreenPanel("T", WidgetFactory.create(root), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
        panel.layoutIfNeeded(measurer, 400, 300)
        val area = assertIs<ScrollAreaWidget>(widget(panel, "area"))

        assertTrue(panel.panel.height <= 300)
        assertEquals(panel.panel.bottom, area.bounds.bottom + UiMetrics.PANEL_PADDING)
        assertTrue(area.maxScrollY > 0)
        val header = widget(panel, "header").bounds
        panel.mouseScrolled(area.bounds.x + 5.0, area.bounds.y + 5.0, -1.0)
        panel.layoutIfNeeded(measurer, 400, 300)
        assertEquals(header, widget(panel, "header").bounds)
        assertTrue(area.scrollY > 0)
    }

    /**
     * Verifies that a growing root whose content cannot become shorter than the window keeps its
     * content height, so that the panel scrolls.
     */
    @Test
    fun `a growing root without a growing area keeps its content height`() {
        val root = ColumnNode("root", height = Sizing.grow(), children = List(40) { ButtonNode("b$it", text = "B$it", height = Sizing.fixed(20)) })
        val panel = ScreenPanel("T", WidgetFactory.create(root), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
        panel.layoutIfNeeded(measurer, 400, 300)

        assertEquals(800, widget(panel, "root").bounds.height)
    }

    /**
     * Creates a vertical area 100 wide and 50 tall holding twenty buttons of 20 pixels.
     *
     * @return the node
     */
    private fun tall() = ScrollAreaNode(
        "area",
        width = Sizing.fixed(100),
        height = Sizing.fixed(50),
        children = (0 until 20).map { ButtonNode("b$it", text = "B$it") },
    )

    /**
     * Creates a horizontal area 100 wide and 30 tall holding a row of ten buttons 40 wide.
     *
     * @return the node
     */
    private fun wide() = ScrollAreaNode(
        "area",
        width = Sizing.fixed(100),
        height = Sizing.fixed(30),
        orientation = ScrollOrientation.HORIZONTAL,
        children = listOf(RowNode("row", children = (0 until 10).map { ButtonNode("w$it", text = "W", width = Sizing.fixed(40)) })),
    )

    /**
     * Verifies that the wheel scrolls a vertical area and stops at the end of the content.
     */
    @Test
    fun `wheel scrolls vertically within the content`() {
        val panel = panel(tall())
        val area = assertIs<ScrollAreaWidget>(widget(panel, "area"))
        val top = widget(panel, "b0").bounds.y

        panel.mouseScrolled(area.bounds.x + 5.0, area.bounds.y + 5.0, -1.0)
        panel.layoutIfNeeded(measurer, 400, 300)
        assertTrue(widget(panel, "b0").bounds.y < top)
        repeat(100) { panel.mouseScrolled(area.bounds.x + 5.0, area.bounds.y + 5.0, -1.0) }
        panel.layoutIfNeeded(measurer, 400, 300)

        assertEquals(area.bounds.bottom, widget(panel, "b19").bounds.bottom)
        assertEquals(area.maxScrollY, area.scrollY)
    }

    /**
     * Verifies that the wheel scrolls a horizontal area sideways, with its content as tall as the
     * area and as wide as it needs.
     */
    @Test
    fun `wheel scrolls horizontal areas sideways`() {
        val panel = panel(wide())
        val area = assertIs<ScrollAreaWidget>(widget(panel, "area"))
        assertEquals(400 - 100, area.maxScrollX)

        panel.mouseScrolled(area.bounds.x + 5.0, area.bounds.y + 5.0, -1.0)
        panel.layoutIfNeeded(measurer, 400, 300)

        assertTrue(area.scrollX > 0)
        assertEquals(area.bounds.x - area.scrollX, widget(panel, "w0").bounds.x)
    }

    /**
     * Verifies that widgets scrolled out of the area do not receive clicks.
     */
    @Test
    fun `hidden content does not receive clicks`() {
        val panel = panel(ColumnNode("wrap", gap = 10, children = listOf(tall(), ButtonNode("below", text = "Unten"))))
        val hidden = widget(panel, "b5").bounds
        assertTrue(hidden.y >= widget(panel, "area").bounds.bottom)

        panel.mouseClicked(hidden.x + 2.0, hidden.y + 2.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)

        assertEquals(emptyList(), actions.filter { it == "b5" })
    }

    /**
     * Verifies that dragging the thumb of the vertical bar scrolls the content in proportion.
     */
    @Test
    fun `dragging the bar scrolls`() {
        val panel = panel(tall())
        val area = assertIs<ScrollAreaWidget>(widget(panel, "area"))
        val thumb = area.verticalThumb()

        panel.mouseClicked(thumb.x + 1.0, thumb.y + 1.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        assertTrue(panel.mouseDragged(thumb.x + 1.0, area.bounds.bottom + 100.0))
        panel.mouseReleased()

        assertEquals(area.maxScrollY, area.scrollY)
    }

    /**
     * Verifies that moving the focus to a widget below the visible part scrolls it into view.
     */
    @Test
    fun `focus scrolls into view`() {
        val panel = panel(tall())
        val area = assertIs<ScrollAreaWidget>(widget(panel, "area"))
        repeat(6) { panel.keyPressed(KeyEvent(GLFW.GLFW_KEY_TAB, 0, 0)) }
        panel.layoutIfNeeded(measurer, 400, 300)

        assertEquals("b5", panel.focusedWidget?.id)
        val b5 = widget(panel, "b5").bounds
        assertTrue(b5.bottom <= area.bounds.bottom && b5.y >= area.bounds.y)
    }
}
