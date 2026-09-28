package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ContextMenuNode
import dev.slne.surf.roleplay.protocol.screen.DropdownMenuNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.MenuCheckboxItemNode
import dev.slne.surf.roleplay.protocol.screen.MenuContentNode
import dev.slne.surf.roleplay.protocol.screen.MenuItemNode
import dev.slne.surf.roleplay.protocol.screen.MenuRadioGroupNode
import dev.slne.surf.roleplay.protocol.screen.MenuRadioItemNode
import dev.slne.surf.roleplay.protocol.screen.MenuSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.MenuSubNode
import dev.slne.surf.roleplay.protocol.screen.MenuSubTriggerNode
import dev.slne.surf.roleplay.protocol.screen.MenubarMenuNode
import dev.slne.surf.roleplay.protocol.screen.MenubarNode
import dev.slne.surf.roleplay.protocol.screen.MenubarTriggerNode
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for dropdown menus, context menus and menubars in the mod.
 */
class MenuWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * The widgets whose actions reached the listener, with their input value, in order.
     */
    private val actions = mutableListOf<String>()

    /**
     * A listener that records actions.
     */
    private val listener = object : ScreenPanelListener {
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) {
            actions += widget.inputValue?.let { "${widget.id}=$it" } ?: widget.id
        }
        override fun closeRequested(panel: ScreenPanel) = Unit
    }

    /**
     * Creates a laid-out panel around nodes placed in a row.
     *
     * @param nodes the nodes
     * @return the panel
     */
    private fun panel(vararg nodes: ScreenNode): ScreenPanel =
        ScreenPanel("T", WidgetFactory.create(RowNode("root", gap = 60, children = nodes.toList())), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
            .also { it.layoutIfNeeded(measurer, 480, 300) }

    /**
     * Returns a key event without modifiers.
     *
     * @param key the GLFW key code
     * @return the event
     */
    private fun key(key: Int) = KeyEvent(key, 0, 0)

    /**
     * Clicks the middle of a widget, laying out the open overlays first.
     *
     * @param panel the panel
     * @param id the id of the widget
     * @param button the mouse button
     */
    private fun click(panel: ScreenPanel, id: String, button: Int = GLFW.GLFW_MOUSE_BUTTON_LEFT) {
        panel.overlayAreas()
        val b = WidgetTree.find(panel.root, id)!!.bounds
        panel.mouseClicked(b.x + b.width / 2.0, b.y + b.height / 2.0, button)
    }

    /**
     * Creates a dropdown menu with a plain item, a disabled item, a checkbox item, a radio group
     * and a sub-menu.
     *
     * @return the node
     */
    private fun dropdown() = DropdownMenuNode(
        "menu",
        children = listOf(
            ButtonNode("trigger", text = "Menu"),
            MenuContentNode(
                "content",
                children = listOf(
                    MenuItemNode("profile", text = "Profile", shortcut = "P"),
                    MenuItemNode("disabled", text = "Disabled", enabled = false),
                    MenuCheckboxItemNode("status", text = "Status", checked = false),
                    MenuSeparatorNode("sep"),
                    MenuRadioGroupNode("position", value = "top", children = listOf(MenuRadioItemNode("top", text = "Top", value = "top"), MenuRadioItemNode("bottom", text = "Bottom", value = "bottom"))),
                    MenuSubNode("more", children = listOf(MenuSubTriggerNode("more_trigger", text = "More"), MenuContentNode("more_content", children = listOf(MenuItemNode("share", text = "Share"))))),
                ),
            ),
        ),
    )

    /**
     * Verifies that Down moves the highlight through the enabled items, and Enter chooses one,
     * which fires its action and closes the menu.
     */
    @Test
    fun `keyboard chooses items`() {
        val panel = panel(dropdown())
        click(panel, "trigger")

        panel.keyPressed(key(GLFW.GLFW_KEY_DOWN))
        assertEquals("profile", panel.focusedWidget?.id)
        panel.keyPressed(key(GLFW.GLFW_KEY_DOWN))
        assertEquals("status", panel.focusedWidget?.id)
        panel.keyPressed(key(GLFW.GLFW_KEY_UP))
        assertEquals("profile", panel.focusedWidget?.id)
        panel.keyPressed(key(GLFW.GLFW_KEY_ENTER))

        assertEquals(listOf("profile"), actions)
        assertNull(panel.popover)
    }

    /**
     * Verifies that a checkbox item flips its state and fires an action carrying it.
     */
    @Test
    fun `checkbox items fire their new state`() {
        val panel = panel(dropdown())
        click(panel, "trigger")
        click(panel, "status")

        assertEquals(listOf("status=true"), actions)
        assertNull(panel.popover)
    }

    /**
     * Verifies that a radio item fires the group's action carrying the chosen value.
     */
    @Test
    fun `radio items fire the group`() {
        val panel = panel(dropdown())
        click(panel, "trigger")
        click(panel, "bottom")

        assertEquals(listOf("position=bottom"), actions)
    }

    /**
     * Verifies that Right opens a sub-menu from its trigger and Left closes it again.
     */
    @Test
    fun `arrow keys open and close sub-menus`() {
        val panel = panel(dropdown())
        click(panel, "trigger")
        panel.focus(WidgetTree.find(panel.root, "more_trigger"))

        panel.keyPressed(key(GLFW.GLFW_KEY_RIGHT))
        assertEquals(2, panel.popovers.size)
        assertEquals("share", panel.focusedWidget?.id)
        panel.keyPressed(key(GLFW.GLFW_KEY_LEFT))
        assertEquals(1, panel.popovers.size)
        assertEquals("more_trigger", panel.focusedWidget?.id)
    }

    /**
     * Verifies that pointing at another item of a menu closes an open sub-menu.
     */
    @Test
    fun `pointing elsewhere closes the sub-menu`() {
        val panel = panel(dropdown())
        click(panel, "trigger")
        click(panel, "more_trigger")
        assertEquals(2, panel.popovers.size)

        val menu = assertIs<MenuPopover>(panel.popovers.first())
        val profile = WidgetTree.find(panel.root, "profile")!!.bounds
        menu.pointerMoved(panel, profile.x + 1, profile.y + 1)

        assertEquals(1, panel.popovers.size)
    }

    /**
     * Verifies that a right click in a context menu's area opens the menu at the mouse.
     */
    @Test
    fun `context menu opens at the mouse`() {
        val node = ContextMenuNode(
            "ctx",
            children = listOf(LabelNode("area", width = Sizing.fixed(100), height = Sizing.fixed(60), text = "Area"), MenuContentNode("ctx_content", children = listOf(MenuItemNode("copy", text = "Copy")))),
        )
        val panel = panel(node)
        val area = WidgetTree.find(panel.root, "area")!!.bounds
        val x = area.x + 30.0
        val y = area.y + 20.0

        panel.mouseClicked(x, y, GLFW.GLFW_MOUSE_BUTTON_RIGHT)

        val opened = panel.overlayAreas().single()
        assertEquals(x.toInt(), opened.x)
        assertEquals(y.toInt(), opened.y)
    }

    /**
     * Verifies that pointing at another menu of a menubar while one is open switches to it.
     */
    @Test
    fun `menubar switches menus on hover`() {
        fun menu(id: String) = MenubarMenuNode(id, children = listOf(MenubarTriggerNode("${id}_trigger", text = id), MenuContentNode("${id}_content", children = listOf(MenuItemNode("${id}_item", text = "Item")))))
        val panel = panel(MenubarNode("bar", children = listOf(menu("file"), menu("edit"))))
        click(panel, "file_trigger")
        val edit = assertIs<MenubarMenuWidget>(WidgetTree.find(panel.root, "edit"))
        val b = edit.bounds

        edit.hoverSwitch(panel, b.x + 1, b.y + 1)

        assertEquals(edit, panel.popover?.owner)
        assertTrue(edit.open)
    }

    /**
     * Verifies that menubar triggers and menu labels are measured with the bold text they draw.
     */
    @Test
    fun `bold menu texts are measured bold`() {
        val bold = measurer.width(TextStyle.styled("\"Datei\"", bold = true, italic = false))

        assertEquals(bold + 2 * 4, MenubarTriggerWidget("t", "\"Datei\"").contentSize(measurer).width)
        assertEquals(MenuStyle.PADDING_X + bold + MenuStyle.PADDING_X, MenuLabelWidget("l", "\"Datei\"", false).contentSize(measurer).width)
    }
}
