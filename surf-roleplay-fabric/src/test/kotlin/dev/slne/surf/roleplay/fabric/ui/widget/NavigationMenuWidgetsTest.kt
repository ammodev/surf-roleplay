package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuContentNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuItemNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuLinkNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuListNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuNode
import dev.slne.surf.roleplay.protocol.screen.NavigationMenuTriggerNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for navigation menus in the mod.
 */
class NavigationMenuWidgetsTest {

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
     * Clicks the middle of a widget, laying out the open overlays first.
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
     * Returns the item with an id.
     *
     * @param panel the panel
     * @param id the id
     * @return the item
     */
    private fun item(panel: ScreenPanel, id: String): NavigationMenuItemWidget = assertIs(WidgetTree.find(panel.root, id))

    /**
     * A navigation menu with two items with content and a link item.
     */
    private val menu = NavigationMenuNode(
        "nav",
        children = listOf(
            NavigationMenuListNode(
                "list",
                children = listOf("a", "b").map { v ->
                    NavigationMenuItemNode(
                        "item_$v",
                        children = listOf(
                            NavigationMenuTriggerNode("trigger_$v", text = "\"$v\""),
                            NavigationMenuContentNode("content_$v", children = listOf(NavigationMenuLinkNode("link_$v", children = listOf(LabelNode("title_$v", text = "Titel"))))),
                        ),
                    )
                } + NavigationMenuItemNode("item_docs", children = listOf(NavigationMenuLinkNode("docs", children = listOf(LabelNode("docs_text", text = "Doku"))))),
            ),
        ),
    )

    /**
     * Verifies that a click on a trigger opens its content below it without an action, and that a
     * link in the content fires its action and closes the content.
     */
    @Test
    fun `trigger opens content whose links act`() {
        val panel = panel(menu)

        click(panel, "trigger_a")
        assertTrue(item(panel, "item_a").open)
        val area = panel.overlayAreas().single()
        assertTrue(area.y >= WidgetTree.find(panel.root, "trigger_a")!!.bounds.bottom)
        click(panel, "link_a")

        assertEquals(listOf("link_a"), actions)
        assertNull(panel.popover)
    }

    /**
     * Verifies that pointing at another trigger while one content is open switches to it.
     */
    @Test
    fun `hover switches between open items`() {
        val panel = panel(menu)
        click(panel, "trigger_a")
        val b = WidgetTree.find(panel.root, "trigger_b")!!.bounds

        item(panel, "item_b").pointer(panel, b.x + 1, b.y + 1, now = 0)

        assertFalse(item(panel, "item_a").open)
        assertTrue(item(panel, "item_b").open)
    }

    /**
     * Verifies that resting on a trigger opens its content after a delay, and leaving closes it
     * after a delay, while content opened by a click stays when the mouse leaves.
     */
    @Test
    fun `hover opens and closes with delays`() {
        val panel = panel(menu)
        val a = item(panel, "item_a")
        val t = WidgetTree.find(panel.root, "trigger_a")!!.bounds

        a.pointer(panel, t.x + 1, t.y + 1, now = 0)
        assertFalse(a.open)
        a.pointer(panel, t.x + 1, t.y + 1, now = NavigationMenuItemWidget.OPEN_DELAY.toLong())
        assertTrue(a.open)
        a.pointer(panel, -100, -100, now = 1000)
        a.pointer(panel, -100, -100, now = 1000 + NavigationMenuItemWidget.CLOSE_DELAY.toLong())
        assertFalse(a.open)

        click(panel, "trigger_a")
        a.pointer(panel, -100, -100, now = 5000)
        a.pointer(panel, -100, -100, now = 9000)
        assertTrue(a.open)
    }

    /**
     * Verifies that a link directly in an item fires its action and is drawn like a trigger.
     */
    @Test
    fun `links in the list act like triggers`() {
        val panel = panel(menu)

        click(panel, "docs")

        assertEquals(listOf("docs"), actions)
        assertTrue(assertIs<NavigationMenuLinkWidget>(WidgetTree.find(panel.root, "docs")).triggerStyle)
        assertNull(panel.popover)
    }
}
