package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbEllipsisNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbItemNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbLinkNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbListNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbPageNode
import dev.slne.surf.roleplay.protocol.screen.BreadcrumbSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.DropdownMenuNode
import dev.slne.surf.roleplay.protocol.screen.MenuContentNode
import dev.slne.surf.roleplay.protocol.screen.MenuItemNode
import dev.slne.surf.roleplay.protocol.screen.PaginationContentNode
import dev.slne.surf.roleplay.protocol.screen.PaginationEllipsisNode
import dev.slne.surf.roleplay.protocol.screen.PaginationItemNode
import dev.slne.surf.roleplay.protocol.screen.PaginationLinkNode
import dev.slne.surf.roleplay.protocol.screen.PaginationNextNode
import dev.slne.surf.roleplay.protocol.screen.PaginationNode
import dev.slne.surf.roleplay.protocol.screen.PaginationPreviousNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for breadcrumbs and paginations in the mod.
 */
class PathWidgetsTest {

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
        ScreenPanel("T", WidgetFactory.create(ColumnNode("root", gap = 40, children = nodes.toList())), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
            .also { it.layoutIfNeeded(measurer, 400, 300) }

    /**
     * Clicks the middle of a widget.
     *
     * @param panel the panel
     * @param id the id of the widget
     */
    private fun click(panel: ScreenPanel, id: String) {
        panel.overlayAreas()
        val b = WidgetTree.find(panel.root, id)!!.bounds
        panel.mouseClicked(b.x + b.width / 2.0, b.y + b.height / 2.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
    }

    /**
     * A breadcrumb with a link, a disabled link, an ellipsis that opens a menu, and the page.
     */
    private val breadcrumb = BreadcrumbNode(
        "breadcrumb",
        children = listOf(
            BreadcrumbListNode(
                "list",
                children = listOf(
                    BreadcrumbItemNode("home_item", children = listOf(BreadcrumbLinkNode("home", text = "\"Start\""))),
                    BreadcrumbSeparatorNode("sep1"),
                    BreadcrumbItemNode(
                        "more_item",
                        children = listOf(
                            DropdownMenuNode("more", children = listOf(BreadcrumbEllipsisNode("ellipsis"), MenuContentNode("more_content", children = listOf(MenuItemNode("docs", text = "Doku"))))),
                        ),
                    ),
                    BreadcrumbSeparatorNode("sep2"),
                    BreadcrumbItemNode("off_item", children = listOf(BreadcrumbLinkNode("off", text = "\"Aus\"", enabled = false))),
                    BreadcrumbSeparatorNode("sep3"),
                    BreadcrumbItemNode("page_item", children = listOf(BreadcrumbPageNode("page", text = "\"Akte\""))),
                ),
            ),
        ),
    )

    /**
     * A pagination with a disabled previous link, two page links of which the first is active, an
     * ellipsis and a next link.
     */
    private val pagination = PaginationNode(
        "pagination",
        children = listOf(
            PaginationContentNode(
                "content",
                children = listOf(
                    PaginationItemNode("i1", children = listOf(PaginationPreviousNode("prev", text = "\"Zurück\"", enabled = false))),
                    PaginationItemNode("i2", children = listOf(PaginationLinkNode("p1", text = "\"1\"", active = true))),
                    PaginationItemNode("i3", children = listOf(PaginationLinkNode("p2", text = "\"2\""))),
                    PaginationItemNode("i4", children = listOf(PaginationEllipsisNode("gap"))),
                    PaginationItemNode("i5", children = listOf(PaginationNextNode("next", text = "\"Weiter\""))),
                ),
            ),
        ),
    )

    /**
     * Verifies that breadcrumb links fire actions, and that disabled links and the page do not.
     */
    @Test
    fun `breadcrumb links fire actions`() {
        val panel = panel(breadcrumb)

        click(panel, "home")
        click(panel, "off")
        click(panel, "page")

        assertEquals(listOf("home"), actions)
    }

    /**
     * Verifies that an ellipsis inside a dropdown menu opens the menu.
     */
    @Test
    fun `breadcrumb ellipsis opens its menu`() {
        val panel = panel(breadcrumb)

        click(panel, "ellipsis")

        assertEquals("more", panel.popover?.owner?.id)
        assertEquals(emptyList(), actions)
    }

    /**
     * Verifies that the items of a breadcrumb are laid out in a row with separators between
     * them.
     */
    @Test
    fun `breadcrumb items sit in a row`() {
        val panel = panel(breadcrumb)
        val home = WidgetTree.find(panel.root, "home")!!.bounds
        val sep = WidgetTree.find(panel.root, "sep1")!!.bounds

        assertTrue(sep.x >= home.right)
        assertEquals(home.y + home.height / 2, sep.y + sep.height / 2, "centered on one line")
    }

    /**
     * Verifies that page links fire actions, and that a disabled previous link does not.
     */
    @Test
    fun `pagination links fire actions`() {
        val panel = panel(pagination)

        click(panel, "prev")
        click(panel, "p2")
        click(panel, "next")

        assertEquals(listOf("p2", "next"), actions)
    }

    /**
     * Verifies that the current page is marked active, and that the pagination is centered in
     * its width.
     */
    @Test
    fun `pagination centers its links`() {
        val panel = panel(ColumnNode("wide", width = dev.slne.surf.roleplay.protocol.screen.Sizing.fixed(300), children = listOf(pagination)))

        assertTrue(assertIs<PaginationLinkWidget>(WidgetTree.find(panel.root, "p1")).active)
        val row = WidgetTree.find(panel.root, "content")!!.bounds
        val outer = WidgetTree.find(panel.root, "pagination")!!.bounds
        assertEquals(outer.x + (outer.width - row.width) / 2, row.x)
    }
}
