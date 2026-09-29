package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.CarouselContentNode
import dev.slne.surf.roleplay.protocol.screen.CarouselItemNode
import dev.slne.surf.roleplay.protocol.screen.CarouselNextNode
import dev.slne.surf.roleplay.protocol.screen.CarouselNode
import dev.slne.surf.roleplay.protocol.screen.CarouselPreviousNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.DirectionNode
import dev.slne.surf.roleplay.protocol.screen.DropdownMenuNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.LayoutDirection
import dev.slne.surf.roleplay.protocol.screen.MenuContentNode
import dev.slne.surf.roleplay.protocol.screen.MenuSubNode
import dev.slne.surf.roleplay.protocol.screen.MenuSubTriggerNode
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.SheetContentNode
import dev.slne.surf.roleplay.protocol.screen.SheetNode
import dev.slne.surf.roleplay.protocol.screen.SidebarInsetNode
import dev.slne.surf.roleplay.protocol.screen.SidebarNode
import dev.slne.surf.roleplay.protocol.screen.SidebarProviderNode
import dev.slne.surf.roleplay.protocol.screen.SidebarRailNode
import dev.slne.surf.roleplay.protocol.screen.SidebarSide
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for right-to-left directions in the mod.
 */
class DirectionWidgetsTest {

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
     * Wraps nodes in a right-to-left direction 200 wide.
     *
     * @param children the nodes
     * @return the direction node
     */
    private fun rtl(vararg children: ScreenNode) = DirectionNode("dir", width = Sizing.fixed(200), direction = LayoutDirection.RTL, children = children.toList())

    /**
     * Verifies that a row inside a right-to-left direction places its first child at the right
     * and the next ones to its left.
     */
    @Test
    fun `rows lay out from the right`() {
        val panel = panel(rtl(RowNode("row", width = Sizing.grow(), gap = 4, children = listOf(LabelNode("a", text = "aa"), LabelNode("b", text = "bbbb")))))
        val row = widget(panel, "row").bounds
        val a = widget(panel, "a").bounds
        val b = widget(panel, "b").bounds

        assertEquals(200, row.width)
        assertEquals(row.right, a.right)
        assertEquals(a.x - 4, b.right)
    }

    /**
     * Verifies that start and end alignment swap inside a right-to-left direction.
     */
    @Test
    fun `alignment swaps`() {
        val panel = panel(
            rtl(
                ColumnNode("start", width = Sizing.grow(), crossAlign = Align.START, children = listOf(LabelNode("s", text = "x"))),
                ColumnNode("end", width = Sizing.grow(), crossAlign = Align.END, children = listOf(LabelNode("e", text = "x"))),
                RowNode("main", width = Sizing.grow(), mainAlign = Align.END, children = listOf(LabelNode("m", text = "x"))),
            ),
        )

        assertEquals(widget(panel, "start").bounds.right, widget(panel, "s").bounds.right)
        assertEquals(widget(panel, "end").bounds.x, widget(panel, "e").bounds.x)
        assertEquals(widget(panel, "main").bounds.x, widget(panel, "m").bounds.x)
    }

    /**
     * Verifies that a left-to-right direction inside a right-to-left one lays its own subtree out
     * from the left, while the outer row still places it from the right.
     */
    @Test
    fun `nested directions override`() {
        val panel = panel(
            rtl(
                RowNode(
                    "outer",
                    width = Sizing.grow(),
                    children = listOf(
                        LabelNode("first", text = "ab"),
                        DirectionNode("ltr", children = listOf(RowNode("inner", children = listOf(LabelNode("b", text = "b"), LabelNode("c", text = "c"))))),
                    ),
                ),
            ),
        )

        assertTrue(widget(panel, "first").rtl)
        assertFalse(widget(panel, "b").rtl)
        assertEquals(widget(panel, "outer").bounds.right, widget(panel, "first").bounds.right)
        assertTrue(widget(panel, "ltr").bounds.right <= widget(panel, "first").bounds.x)
        assertTrue(widget(panel, "b").bounds.x < widget(panel, "c").bounds.x)
    }

    /**
     * Verifies that a left sidebar inside a right-to-left direction is shown at the right, with
     * its rail along its inner edge at the left.
     */
    @Test
    fun `sidebars mirror their side`() {
        val provider = SidebarProviderNode(
            "provider",
            width = Sizing.fixed(200),
            height = Sizing.fixed(100),
            children = listOf(
                SidebarNode("sidebar", side = SidebarSide.LEFT, children = listOf(LabelNode("brand", text = "x"), SidebarRailNode("rail"))),
                SidebarInsetNode("inset", children = listOf(LabelNode("main", text = "y"))),
            ),
        )
        val panel = panel(rtl(provider))
        val sidebar = assertIs<SidebarWidget>(widget(panel, "sidebar"))

        assertEquals(SidebarSide.RIGHT, sidebar.shownSide)
        assertEquals(OverlaySide.LEFT, sidebar.tooltipSide)
        assertEquals(widget(panel, "provider").bounds.right, sidebar.bounds.right)
        assertEquals(sidebar.bounds.x, widget(panel, "inset").bounds.right)
        val rail = widget(panel, "rail").bounds
        assertEquals(sidebar.bounds.x, rail.x + rail.width / 2)
    }

    /**
     * Verifies that a sheet on the left inside a right-to-left direction opens at the right and
     * has its close button at the top left.
     */
    @Test
    fun `sheets mirror their side`() {
        val root = WidgetFactory.create(rtl(SheetNode("sheet", children = listOf(SheetContentNode("content", side = OverlaySide.LEFT)))))
        WidgetTree.resolveDirection(root)
        val content = assertIs<SheetContentWidget>(WidgetTree.find(root, "content"))
        content.bounds = Rect(100, 0, 100, 80)

        assertEquals(OverlaySide.RIGHT, content.shownSide)
        assertEquals(content.bounds.x + ModalSurfaceWidget.CLOSE_INSET, content.closeButton().x)
    }

    /**
     * Verifies that a horizontal carousel inside a right-to-left direction has its previous
     * button at the right, its first slide at the right, and that Left moves it forward.
     */
    @Test
    fun `carousels mirror their buttons`() {
        val carousel = CarouselNode(
            "carousel",
            width = Sizing.fixed(200),
            notifyChange = true,
            children = listOf(
                CarouselContentNode("content", children = (0 until 3).map { CarouselItemNode("slide$it", basis = 50.0, children = listOf(LabelNode("text$it", text = "$it"))) }),
                CarouselPreviousNode("prev"),
                CarouselNextNode("next"),
            ),
        )
        val panel = panel(rtl(carousel))
        val content = widget(panel, "content").bounds

        assertTrue(widget(panel, "prev").bounds.x > widget(panel, "next").bounds.x)
        assertEquals(content.right, widget(panel, "slide0").bounds.right)
        assertEquals(widget(panel, "slide0").bounds.x, widget(panel, "slide1").bounds.right)

        panel.focus(widget(panel, "next"))
        panel.keyPressed(KeyEvent(GLFW.GLFW_KEY_LEFT, 0, 0))
        assertEquals("1", widget(panel, "carousel").inputValue)
        panel.keyPressed(KeyEvent(GLFW.GLFW_KEY_RIGHT, 0, 0))
        assertEquals("0", widget(panel, "carousel").inputValue)
    }

    /**
     * Verifies that overlays of right-to-left hosts swap start and end alignment below their
     * anchor, and that sub-menus open to the left.
     */
    @Test
    fun `overlays mirror their placement`() {
        val menu = DropdownMenuNode(
            "menu",
            side = OverlaySide.BOTTOM,
            align = Align.START,
            children = listOf(
                LabelNode("trigger", text = "x"),
                MenuContentNode("items", children = listOf(MenuSubNode("sub", children = listOf(MenuSubTriggerNode("more", text = "\"Mehr\""), MenuContentNode("sub_items"))))),
            ),
        )
        val root = WidgetFactory.create(rtl(menu))
        WidgetTree.resolveDirection(root)
        val window = Rect(0, 0, 400, 300)
        val anchor = Rect(100, 50, 40, 12)

        val dropdown = assertIs<DropdownMenuWidget>(WidgetTree.find(root, "menu"))
        dropdown.bounds = anchor
        val placed = assertIs<WidgetPopover>(dropdown.createPopover()).place(Size(80, 30), window)
        assertEquals(anchor.right - 80, placed.x)

        val sub = assertIs<MenuSubWidget>(WidgetTree.find(root, "sub"))
        sub.bounds = anchor
        val subPlaced = assertIs<WidgetPopover>(sub.createPopover()).place(Size(80, 30), window)
        assertTrue(subPlaced.right <= anchor.x)
    }

    /**
     * Verifies that icons with a left or right part point the other way right-to-left.
     */
    @Test
    fun `directional icons flip`() {
        assertEquals("chevron-left", directionalIcon("chevron-right", true))
        assertEquals("arrow-big-right", directionalIcon("arrow-big-left", true))
        assertEquals("chevron-right", directionalIcon("chevron-right", false))
        assertEquals("slash", directionalIcon("slash", true))
    }
}
