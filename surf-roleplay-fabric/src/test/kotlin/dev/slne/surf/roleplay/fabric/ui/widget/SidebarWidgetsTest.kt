package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.SidebarCollapsible
import dev.slne.surf.roleplay.protocol.screen.SidebarContentNode
import dev.slne.surf.roleplay.protocol.screen.SidebarFooterNode
import dev.slne.surf.roleplay.protocol.screen.SidebarGroupActionNode
import dev.slne.surf.roleplay.protocol.screen.SidebarGroupContentNode
import dev.slne.surf.roleplay.protocol.screen.SidebarGroupLabelNode
import dev.slne.surf.roleplay.protocol.screen.SidebarGroupNode
import dev.slne.surf.roleplay.protocol.screen.SidebarHeaderNode
import dev.slne.surf.roleplay.protocol.screen.SidebarInsetNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuActionNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuBadgeNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuButtonNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuItemNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuSubButtonNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuSubItemNode
import dev.slne.surf.roleplay.protocol.screen.SidebarMenuSubNode
import dev.slne.surf.roleplay.protocol.screen.SidebarNode
import dev.slne.surf.roleplay.protocol.screen.SidebarProviderNode
import dev.slne.surf.roleplay.protocol.screen.SidebarRailNode
import dev.slne.surf.roleplay.protocol.screen.SidebarSide
import dev.slne.surf.roleplay.protocol.screen.SidebarTriggerNode
import dev.slne.surf.roleplay.protocol.screen.SidebarVariant
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
 * Tests for sidebars in the mod.
 */
class SidebarWidgetsTest {

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
     * The inputs whose changes reached the listener, with their value, in order.
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
     * Creates a laid-out panel around a node.
     *
     * @param node the node
     * @return the panel
     */
    private fun panel(node: ScreenNode): ScreenPanel =
        ScreenPanel("T", WidgetFactory.create(node), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
            .also { it.layoutIfNeeded(measurer, 400, 300) }

    /**
     * Lays the panel out again.
     *
     * @param panel the panel
     */
    private fun relayout(panel: ScreenPanel) {
        panel.requestLayout()
        panel.layoutIfNeeded(measurer, 400, 300)
    }

    /**
     * Clicks the middle of a widget and lays the panel out again.
     *
     * @param panel the panel
     * @param id the id of the widget
     */
    private fun click(panel: ScreenPanel, id: String) {
        val b = widget(panel, id).bounds
        panel.mouseClicked(b.x + b.width / 2.0, b.y + b.height / 2.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        relayout(panel)
    }

    /**
     * Returns the widget with an id.
     *
     * @param panel the panel
     * @param id the id
     * @return the widget
     */
    private fun widget(panel: ScreenPanel, id: String): Widget = WidgetTree.find(panel.root, id)!!

    /**
     * Creates a provider 300 wide and 160 tall with a sidebar and an inset holding a trigger.
     *
     * @param side the side of the sidebar
     * @param variant how the sidebar is drawn
     * @param collapsible how the sidebar collapses
     * @return the node
     */
    private fun provider(side: SidebarSide = SidebarSide.LEFT, variant: SidebarVariant = SidebarVariant.SIDEBAR, collapsible: SidebarCollapsible = SidebarCollapsible.OFFCANVAS) = SidebarProviderNode(
        "provider",
        width = Sizing.fixed(300),
        height = Sizing.fixed(160),
        notifyChange = true,
        children = listOf(
            SidebarNode(
                "sidebar",
                side = side,
                variant = variant,
                collapsible = collapsible,
                children = listOf(
                    SidebarHeaderNode("header", children = listOf(LabelNode("brand", text = "Leitstelle"))),
                    SidebarContentNode(
                        "content",
                        children = listOf(
                            SidebarGroupNode(
                                "group",
                                children = listOf(
                                    SidebarGroupLabelNode("label", text = "\"Dienst\""),
                                    SidebarGroupActionNode("group_action"),
                                    SidebarGroupContentNode(
                                        "group_content",
                                        children = listOf(
                                            SidebarMenuNode(
                                                "menu",
                                                children = listOf(
                                                    SidebarMenuItemNode(
                                                        "home_item",
                                                        children = listOf(
                                                            SidebarMenuButtonNode("home", text = "\"Start\"", icon = "house", active = true, tooltip = "\"Start\""),
                                                            SidebarMenuActionNode("home_action"),
                                                            SidebarMenuBadgeNode("home_badge", text = "\"3\""),
                                                            SidebarMenuSubNode("sub", children = listOf(SidebarMenuSubItemNode("sub_item", children = listOf(SidebarMenuSubButtonNode("sub_button", text = "\"Neu\""))))),
                                                        ),
                                                    ),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    SidebarFooterNode("footer", children = listOf(LabelNode("user", text = "Max"))),
                    SidebarRailNode("rail"),
                ),
            ),
            SidebarInsetNode("inset", children = listOf(SidebarTriggerNode("trigger"), LabelNode("main", text = "Inhalt"))),
        ),
    )

    /**
     * Verifies that an expanded sidebar takes its width at its side and the inset the rest.
     */
    @Test
    fun `sidebar sits beside the inset`() {
        val panel = panel(provider())
        val provider = widget(panel, "provider").bounds
        val sidebar = widget(panel, "sidebar").bounds
        val inset = widget(panel, "inset").bounds

        assertEquals(provider.x, sidebar.x)
        assertEquals(SidebarWidget.WIDTH, sidebar.width)
        assertEquals(provider.height, sidebar.height)
        assertEquals(sidebar.right, inset.x)
        assertEquals(provider.right, inset.right)
    }

    /**
     * Verifies that a sidebar on the right sits after the inset.
     */
    @Test
    fun `right sidebars sit at the right`() {
        val panel = panel(provider(side = SidebarSide.RIGHT))

        assertEquals(widget(panel, "provider").bounds.right, widget(panel, "sidebar").bounds.right)
        assertEquals(widget(panel, "provider").bounds.x, widget(panel, "inset").bounds.x)
    }

    /**
     * Verifies that the trigger collapses an offcanvas sidebar completely without an action, and
     * expands it again.
     */
    @Test
    fun `trigger collapses offcanvas sidebars`() {
        val panel = panel(provider())

        click(panel, "trigger")
        assertEquals("false", widget(panel, "provider").inputValue)
        assertTrue(widget(panel, "sidebar").hidden)
        assertEquals(widget(panel, "provider").bounds.x, widget(panel, "inset").bounds.x)
        click(panel, "trigger")

        assertFalse(widget(panel, "sidebar").hidden)
        assertEquals(listOf("provider=false", "provider=true"), changes)
        assertEquals(emptyList(), actions)
    }

    /**
     * Verifies that a sidebar collapsed to icons shrinks to its icons, hides its texts, labels,
     * actions, badges and sub-menus, and offers the tooltips of its buttons.
     */
    @Test
    fun `icon sidebars shrink to their icons`() {
        val panel = panel(provider(collapsible = SidebarCollapsible.ICON))

        click(panel, "trigger")

        assertEquals(SidebarWidget.ICON_WIDTH, widget(panel, "sidebar").bounds.width)
        val home = assertIs<SidebarMenuButtonWidget>(widget(panel, "home"))
        assertTrue(home.iconOnly)
        assertEquals(home.bounds.width, home.bounds.height)
        assertTrue(home.wantsTooltip)
        listOf("label", "group_action", "home_action", "home_badge", "sub").forEach { assertTrue(widget(panel, it).hidden, it) }
    }

    /**
     * Verifies that a sidebar that cannot collapse ignores its trigger.
     */
    @Test
    fun `fixed sidebars ignore the trigger`() {
        val panel = panel(provider(collapsible = SidebarCollapsible.NONE))

        click(panel, "trigger")

        assertFalse(widget(panel, "sidebar").hidden)
        assertEquals(SidebarWidget.WIDTH, widget(panel, "sidebar").bounds.width)
    }

    /**
     * Verifies that Ctrl+B toggles the sidebar without any focus, and that the rail toggles it
     * when clicked.
     */
    @Test
    fun `shortcut and rail toggle the sidebar`() {
        val panel = panel(provider(collapsible = SidebarCollapsible.ICON))

        panel.keyPressed(KeyEvent(GLFW.GLFW_KEY_B, 0, GLFW.GLFW_MOD_CONTROL))
        relayout(panel)
        assertEquals("false", widget(panel, "provider").inputValue)
        click(panel, "rail")

        assertEquals("true", widget(panel, "provider").inputValue)
    }

    /**
     * Verifies that menu buttons, menu actions and group actions fire actions, and that the
     * action and the badge sit at the end of the button.
     */
    @Test
    fun `menu parts act`() {
        val panel = panel(provider())
        val home = widget(panel, "home").bounds
        val action = widget(panel, "home_action").bounds

        assertTrue(action.right <= home.right && action.x > home.x + home.width / 2)
        assertTrue(widget(panel, "home_badge").bounds.right <= action.x)
        click(panel, "home_action")
        click(panel, "home")
        click(panel, "group_action")
        click(panel, "sub_button")

        assertEquals(listOf("home_action", "home", "group_action", "sub_button"), actions)
    }

    /**
     * Verifies that a floating sidebar keeps a margin around its card.
     */
    @Test
    fun `floating sidebars keep a margin`() {
        val panel = panel(provider(variant = SidebarVariant.FLOATING))

        assertEquals(widget(panel, "sidebar").bounds.x + SidebarWidget.MARGIN, widget(panel, "header").bounds.x)
    }
}
