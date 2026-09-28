package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.AccordionContentNode
import dev.slne.surf.roleplay.protocol.screen.AccordionItemNode
import dev.slne.surf.roleplay.protocol.screen.AccordionNode
import dev.slne.surf.roleplay.protocol.screen.AccordionTriggerNode
import dev.slne.surf.roleplay.protocol.screen.AccordionType
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CollapsibleContentNode
import dev.slne.surf.roleplay.protocol.screen.CollapsibleNode
import dev.slne.surf.roleplay.protocol.screen.CollapsibleTriggerNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for collapsibles and accordions in the mod.
 */
class DisclosureWidgetsTest {

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
        ScreenPanel("T", WidgetFactory.create(ColumnNode("root", gap = 4, children = nodes.toList())), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
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
     * Creates a collapsible with a button trigger and a content holding a button.
     *
     * @param notify whether the collapsible reports changes
     * @return the node
     */
    private fun collapsible(notify: Boolean = false) = CollapsibleNode(
        "collapsible",
        notifyChange = notify,
        children = listOf(
            CollapsibleTriggerNode("trigger", children = listOf(ButtonNode("toggle", text = "Mehr"))),
            CollapsibleContentNode("content", children = listOf(ButtonNode("inner", text = "Innen"))),
        ),
    )

    /**
     * Creates an accordion with three items, the last one disabled.
     *
     * @param type how many items can be open
     * @param collapsible whether the open item of a single accordion can be closed
     * @param value the open items
     * @return the node
     */
    private fun accordion(type: AccordionType = AccordionType.SINGLE, collapsible: Boolean = false, value: List<String> = emptyList()) = AccordionNode(
        "accordion",
        type = type,
        collapsible = collapsible,
        value = value,
        notifyChange = true,
        children = listOf("a", "b", "c").map { v ->
            AccordionItemNode(
                "item_$v",
                value = v,
                enabled = v != "c",
                children = listOf(AccordionTriggerNode("trigger_$v", text = "\"$v\""), AccordionContentNode("content_$v", children = listOf(LabelNode("text_$v", text = "Text")))),
            )
        },
    )

    /**
     * Verifies that the trigger of a collapsible shows and hides its content without firing an
     * action.
     */
    @Test
    fun `collapsible trigger toggles the content`() {
        val panel = panel(collapsible())
        assertTrue(widget(panel, "content").hidden)

        click(panel, "toggle")
        assertFalse(widget(panel, "content").hidden)
        assertEquals("true", widget(panel, "collapsible").inputValue)
        click(panel, "toggle")

        assertTrue(widget(panel, "content").hidden)
        assertEquals(emptyList(), actions)
    }

    /**
     * Verifies that a collapsible that reports changes reports each toggle, and that a value set
     * by the server closes it without a report.
     */
    @Test
    fun `collapsible reports changes and takes server values`() {
        val panel = panel(collapsible(notify = true))

        click(panel, "toggle")
        assertEquals(listOf("collapsible=true"), changes)
        widget(panel, "collapsible").applyValue("false")

        assertTrue(widget(panel, "content").hidden)
        assertEquals(listOf("collapsible=true"), changes)
    }

    /**
     * Verifies that closed content leaves the Tab order.
     */
    @Test
    fun `closed content leaves the tab order`() {
        val panel = panel(collapsible())
        panel.keyPressed(key(GLFW.GLFW_KEY_TAB))
        assertEquals("toggle", panel.focusedWidget?.id)
        panel.keyPressed(key(GLFW.GLFW_KEY_TAB))
        assertEquals("toggle", panel.focusedWidget?.id)

        click(panel, "toggle")
        panel.keyPressed(key(GLFW.GLFW_KEY_TAB))
        assertEquals("inner", panel.focusedWidget?.id)
    }

    /**
     * Verifies that a single accordion keeps one item open, and its open item cannot be closed
     * unless the accordion is collapsible.
     */
    @Test
    fun `single accordion keeps one item open`() {
        val panel = panel(accordion())

        click(panel, "trigger_a")
        click(panel, "trigger_b")
        assertEquals("b", widget(panel, "accordion").inputValue)
        assertTrue(widget(panel, "content_a").hidden)
        assertFalse(widget(panel, "content_b").hidden)
        click(panel, "trigger_b")

        assertEquals("b", widget(panel, "accordion").inputValue)
        assertEquals(listOf("accordion=a", "accordion=b"), changes)
    }

    /**
     * Verifies that the open item of a collapsible single accordion closes on its trigger.
     */
    @Test
    fun `collapsible single accordion closes its item`() {
        val panel = panel(accordion(collapsible = true, value = listOf("a")))
        assertFalse(widget(panel, "content_a").hidden)

        click(panel, "trigger_a")

        assertEquals("", widget(panel, "accordion").inputValue)
        assertTrue(widget(panel, "content_a").hidden)
    }

    /**
     * Verifies that a multiple accordion opens several items and ignores disabled ones.
     */
    @Test
    fun `multiple accordion opens several items`() {
        val panel = panel(accordion(type = AccordionType.MULTIPLE))

        click(panel, "trigger_b")
        click(panel, "trigger_a")
        click(panel, "trigger_c")

        assertEquals("a,b", widget(panel, "accordion").inputValue)
        assertTrue(widget(panel, "content_c").hidden)
    }

    /**
     * Verifies that Up and Down move between the enabled accordion triggers, and Enter toggles
     * the focused item.
     */
    @Test
    fun `keyboard moves between accordion triggers`() {
        val panel = panel(accordion())
        panel.keyPressed(key(GLFW.GLFW_KEY_TAB))
        assertEquals("trigger_a", panel.focusedWidget?.id)

        panel.keyPressed(key(GLFW.GLFW_KEY_DOWN))
        assertEquals("trigger_b", panel.focusedWidget?.id)
        panel.keyPressed(key(GLFW.GLFW_KEY_DOWN))
        assertEquals("trigger_a", panel.focusedWidget?.id)
        panel.keyPressed(key(GLFW.GLFW_KEY_ENTER))

        assertEquals("a", widget(panel, "accordion").inputValue)
    }

    /**
     * Verifies that a value set by the server opens the named items.
     */
    @Test
    fun `accordion takes server values`() {
        val panel = panel(accordion(type = AccordionType.MULTIPLE))

        widget(panel, "accordion").applyValue("a,c")

        assertEquals("a,c", widget(panel, "accordion").inputValue)
        assertFalse(widget(panel, "content_c").hidden)
    }
}
