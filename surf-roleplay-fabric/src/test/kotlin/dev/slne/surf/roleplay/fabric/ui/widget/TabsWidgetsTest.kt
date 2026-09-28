package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.TabsContentNode
import dev.slne.surf.roleplay.protocol.screen.TabsListNode
import dev.slne.surf.roleplay.protocol.screen.TabsNode
import dev.slne.surf.roleplay.protocol.screen.TabsTriggerNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for tabs in the mod.
 */
class TabsWidgetsTest {

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
     * Creates a laid-out panel around nodes placed in a column.
     *
     * @param nodes the nodes
     * @return the panel
     */
    private fun panel(vararg nodes: ScreenNode): ScreenPanel =
        ScreenPanel("T", WidgetFactory.create(ColumnNode("root", children = nodes.toList())), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
            .also { it.layoutIfNeeded(measurer, 400, 300) }

    /**
     * Clicks the middle of a widget and lays the panel out again.
     *
     * @param panel the panel
     * @param id the id of the widget
     */
    private fun click(panel: ScreenPanel, id: String) {
        panel.layoutIfNeeded(measurer, 400, 300)
        val b = WidgetTree.find(panel.root, id)!!.bounds
        panel.mouseClicked(b.x + b.width / 2.0, b.y + b.height / 2.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        panel.layoutIfNeeded(measurer, 400, 300)
    }

    /**
     * Returns a key event without modifiers.
     *
     * @param key the GLFW key code
     * @return the event
     */
    private fun key(key: Int) = KeyEvent(key, 0, 0)

    /**
     * Returns the widget with an id.
     *
     * @param panel the panel
     * @param id the id
     * @return the widget
     */
    private fun widget(panel: ScreenPanel, id: String): Widget = WidgetTree.find(panel.root, id)!!

    /**
     * Creates tabs with three tabs, the second one disabled.
     *
     * @param value the selected tab
     * @param orientation where the triggers are
     * @return the node
     */
    private fun tabs(value: String = "a", orientation: Orientation = Orientation.HORIZONTAL) = TabsNode(
        "tabs",
        value = value,
        orientation = orientation,
        notifyChange = true,
        children = listOf(
            TabsListNode("list", children = listOf("a", "b", "c").map { v -> TabsTriggerNode("trigger_$v", value = v, text = "\"$v\"", enabled = v != "b") }),
        ) + listOf("a", "b", "c").map { v -> TabsContentNode("content_$v", value = v, children = listOf(LabelNode("text_$v", text = "Text $v"))) },
    )

    /**
     * Verifies that only the content of the selected tab is shown, and a click on another trigger
     * selects it without firing an action.
     */
    @Test
    fun `clicking a trigger selects its tab`() {
        val panel = panel(tabs())
        assertFalse(widget(panel, "content_a").hidden)
        assertTrue(widget(panel, "content_c").hidden)

        click(panel, "trigger_c")

        assertEquals("c", widget(panel, "tabs").inputValue)
        assertTrue(widget(panel, "content_a").hidden)
        assertFalse(widget(panel, "content_c").hidden)
        assertEquals(listOf("tabs=c"), changes)
        assertEquals(emptyList(), actions)
    }

    /**
     * Verifies that a disabled trigger cannot be selected.
     */
    @Test
    fun `disabled triggers are not selected`() {
        val panel = panel(tabs())

        click(panel, "trigger_b")

        assertEquals("a", widget(panel, "tabs").inputValue)
    }

    /**
     * Verifies that Right and Left move between the enabled triggers and select them, wrapping
     * around.
     */
    @Test
    fun `arrow keys move between tabs`() {
        val panel = panel(tabs())
        panel.keyPressed(key(GLFW.GLFW_KEY_TAB))
        assertEquals("trigger_a", panel.focusedWidget?.id)

        panel.keyPressed(key(GLFW.GLFW_KEY_RIGHT))
        assertEquals("trigger_c", panel.focusedWidget?.id)
        assertEquals("c", widget(panel, "tabs").inputValue)
        panel.keyPressed(key(GLFW.GLFW_KEY_RIGHT))

        assertEquals("trigger_a", panel.focusedWidget?.id)
        assertEquals("a", widget(panel, "tabs").inputValue)
    }

    /**
     * Verifies that vertical tabs use Up and Down and place the triggers beside the contents.
     */
    @Test
    fun `vertical tabs sit beside their contents`() {
        val panel = panel(tabs(orientation = Orientation.VERTICAL))
        assertTrue(widget(panel, "content_a").bounds.x >= widget(panel, "list").bounds.right)
        panel.keyPressed(key(GLFW.GLFW_KEY_TAB))

        panel.keyPressed(key(GLFW.GLFW_KEY_DOWN))

        assertEquals("c", widget(panel, "tabs").inputValue)
    }

    /**
     * Verifies that tabs without a selected value select their first enabled tab, and that a
     * value set by the server selects its tab without a report.
     */
    @Test
    fun `tabs take the first tab and server values`() {
        val panel = panel(tabs(value = ""))
        assertEquals("a", widget(panel, "tabs").inputValue)

        widget(panel, "tabs").applyValue("c")

        assertFalse(widget(panel, "content_c").hidden)
        assertEquals(emptyList(), changes)
    }
}
