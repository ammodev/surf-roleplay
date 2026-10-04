package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CommandItemNode
import dev.slne.surf.roleplay.protocol.screen.CommandListNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.ScrollAreaNode
import dev.slne.surf.roleplay.protocol.screen.ScrollListNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests that scrolling moves the content without laying the screen out again.
 */
class ScrollOffsetTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * A screen that counts the layouts asked of it.
     */
    private class CountingContext : UiContext {
        /**
         * The number of layouts asked for.
         */
        var layouts: Int = 0

        override val focusedWidget: Widget? = null
        override fun focus(widget: Widget?) = Unit
        override fun requestLayout() {
            layouts++
        }
        override fun actionTriggered(widget: Widget, submitsInput: Boolean) = Unit
        override var clipboard: String = ""
    }

    /**
     * A listener that ignores everything.
     */
    private val listener = object : ScreenPanelListener {
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) = Unit
        override fun closeRequested(panel: ScreenPanel) = Unit
    }

    /**
     * A fixed-size leaf that counts how often it was laid out.
     *
     * @param id the id of the widget
     */
    private class Block(id: String) : Widget(id) {
        /**
         * The number of layouts.
         */
        var layouts: Int = 0

        /**
         * Returns a size of 100 by 40 pixels.
         *
         * @param measurer the text measurer
         * @return the size
         */
        override fun contentSize(measurer: TextMeasurer): Size = Size(100, 40)

        /**
         * Counts the layout and creates the box.
         *
         * @param measurer the text measurer
         * @return the layout box
         */
        override fun createLayout(measurer: TextMeasurer): LayoutBox {
            layouts++
            return super.createLayout(measurer)
        }

        /**
         * Draws nothing.
         *
         * @param ui the graphics to draw with
         * @param context the screen showing the widget
         * @param mouseX the mouse x position
         * @param mouseY the mouse y position
         */
        override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) = Unit
    }

    /**
     * Lays a widget out alone in an area.
     *
     * @param widget the widget
     * @param area the area
     */
    private fun layOut(widget: Widget, area: Rect) {
        FlexLayout.layout(widget.createLayout(measurer), area)
        widget.applyLayout()
    }

    /**
     * Verifies that the wheel moves a scroll area's content at once and asks for no layout, and
     * that a later layout keeps it where it is.
     */
    @Test
    fun `wheel moves a scroll area without a layout`() {
        val area = WidgetFactory.create(ScrollAreaNode("area", width = Sizing.fixed(100), height = Sizing.fixed(50), children = (0 until 20).map { ButtonNode("b$it", text = "B$it") })) as ScrollAreaWidget
        layOut(area, Rect(0, 0, 100, 50))
        val first = WidgetTree.find(area, "b0")!!
        val top = first.bounds.y
        val context = CountingContext()

        assertTrue(area.mouseScrolled(context, 5.0, 5.0, -1.0))

        assertEquals(0, context.layouts)
        assertEquals(top - UiMetrics.SCROLL_STEP, first.bounds.y)
        assertEquals(UiMetrics.SCROLL_STEP, area.scrollY)
        layOut(area, Rect(0, 0, 100, 50))
        assertEquals(top - UiMetrics.SCROLL_STEP, first.bounds.y)
    }

    /**
     * Verifies that dragging a scroll area's thumb moves the content at once and asks for no
     * layout.
     */
    @Test
    fun `dragging moves a scroll area without a layout`() {
        val panel = ScreenPanel("T", WidgetFactory.create(ColumnNode("root", children = listOf(ScrollAreaNode("area", width = Sizing.fixed(100), height = Sizing.fixed(50), children = (0 until 20).map { ButtonNode("b$it", text = "B$it") })))), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
        panel.layoutIfNeeded(measurer, 400, 300)
        val area = WidgetTree.find(panel.root, "area") as ScrollAreaWidget
        val first = WidgetTree.find(area, "b0")!!
        val top = first.bounds.y
        val thumb = area.verticalThumb()
        panel.mouseClicked(thumb.x + 1.0, thumb.y + 1.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        val context = CountingContext()

        area.mouseDragged(context, thumb.x + 1.0, thumb.y + 21.0)

        assertEquals(0, context.layouts)
        assertTrue(area.scrollY > 0)
        assertEquals(top - area.scrollY, first.bounds.y)
    }

    /**
     * Verifies that the wheel moves a scroll list's children at once and asks for no layout.
     */
    @Test
    fun `wheel moves a scroll list without a layout`() {
        val list = WidgetFactory.create(ScrollListNode("list", height = Sizing.fixed(60), children = List(10) { ButtonNode("b$it", height = Sizing.fixed(20)) })) as ScrollListWidget
        layOut(list, Rect(0, 0, 100, 60))
        val first = WidgetTree.find(list, "b0")!!
        val top = first.bounds.y
        val context = CountingContext()

        assertTrue(list.mouseScrolled(context, 10.0, 10.0, -1.0))

        assertEquals(0, context.layouts)
        assertEquals(top - list.scrollOffset, first.bounds.y)
        assertTrue(list.scrollOffset > 0)
    }

    /**
     * Verifies that the wheel moves a command list's items at once and asks for no layout.
     */
    @Test
    fun `wheel moves a command list without a layout`() {
        val list = WidgetFactory.create(CommandListNode("list", children = List(30) { CommandItemNode("i$it", text = "Eintrag $it") })) as ContainerWidget
        layOut(list, Rect(0, 0, 100, CommandListWidget.MAX_HEIGHT))
        val first = WidgetTree.find(list, "i0")!!
        val top = first.bounds.y
        val context = CountingContext()

        assertTrue(list.mouseScrolled(context, 10.0, 10.0, -1.0))

        assertEquals(0, context.layouts)
        assertTrue(first.bounds.y < top)
    }

    /**
     * Verifies that the wheel scrolls the panel's content without laying the tree out again.
     */
    @Test
    fun `wheel scrolls the panel without a layout`() {
        val root = ContainerWidget("root", Axis.VERTICAL)
        val blocks = List(20) { Block("block$it") }
        root.childList += blocks
        val panel = ScreenPanel("T", root, true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
        panel.layoutIfNeeded(measurer, 400, 300)
        val top = blocks.first().bounds.y
        val layouts = blocks.first().layouts

        assertTrue(panel.mouseScrolled(panel.panel.x + 10.0, panel.panel.y + 40.0, -1.0))
        panel.layoutIfNeeded(measurer, 400, 300)

        assertEquals(layouts, blocks.first().layouts)
        assertEquals(top - UiMetrics.SCROLL_STEP, blocks.first().bounds.y)
    }
}
