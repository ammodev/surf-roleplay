package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.ResizableHandleNode
import dev.slne.surf.roleplay.protocol.screen.ResizablePanelGroupNode
import dev.slne.surf.roleplay.protocol.screen.ResizablePanelNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for resizable panel groups in the mod.
 */
class ResizableWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * The inputs whose changes reached the listener, with their value, in order.
     */
    private val changes = mutableListOf<String>()

    /**
     * A listener that records changes.
     */
    private val listener = object : ScreenPanelListener {
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) = Unit
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
     * Returns the widget with an id.
     *
     * @param panel the panel
     * @param id the id
     * @return the widget
     */
    private fun widget(panel: ScreenPanel, id: String): Widget = WidgetTree.find(panel.root, id)!!

    /**
     * Creates a group 201 wide and 60 tall: a panel of 25 percent (between 10 and 50), a handle,
     * and a panel that takes the rest.
     *
     * @param orientation whether the panels are side by side or stacked
     * @return the node
     */
    private fun group(orientation: Orientation = Orientation.HORIZONTAL) = ResizablePanelGroupNode(
        "group",
        width = Sizing.fixed(if (orientation == Orientation.HORIZONTAL) 201 else 60),
        height = Sizing.fixed(if (orientation == Orientation.HORIZONTAL) 60 else 201),
        orientation = orientation,
        notifyChange = true,
        children = listOf(
            ResizablePanelNode("one", defaultSize = 25.0, minSize = 10.0, maxSize = 50.0, children = listOf(LabelNode("a", text = "A"))),
            ResizableHandleNode("handle", withHandle = true),
            ResizablePanelNode("two", children = listOf(LabelNode("b", text = "B"))),
        ),
    )

    /**
     * Verifies that the panels take their shares of the group beside the handle, and that a
     * panel without a default size takes what the others leave.
     */
    @Test
    fun `panels take their shares`() {
        val panel = panel(group())

        assertEquals("25,75", widget(panel, "group").inputValue)
        assertEquals(50, widget(panel, "one").bounds.width)
        assertEquals(widget(panel, "one").bounds.right, widget(panel, "handle").bounds.x)
        assertEquals(150, widget(panel, "two").bounds.width)
        assertEquals(widget(panel, "group").bounds.right, widget(panel, "two").bounds.right)
    }

    /**
     * Verifies that dragging the handle resizes the panels on both sides within their limits and
     * reports the new sizes.
     */
    @Test
    fun `dragging the handle resizes within limits`() {
        val panel = panel(group())
        val handle = widget(panel, "handle").bounds
        val y = handle.y + handle.height / 2.0

        panel.mouseClicked(handle.x + 0.5, y, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        panel.mouseDragged(handle.x + 0.5 + 20, y)
        assertEquals("35,65", widget(panel, "group").inputValue)
        panel.mouseDragged(handle.x + 0.5 + 150, y)
        panel.mouseReleased()

        assertEquals("50,50", widget(panel, "group").inputValue)
        panel.layoutIfNeeded(measurer, 400, 300)
        assertEquals(100, widget(panel, "one").bounds.width)
    }

    /**
     * Verifies that the arrow keys move a focused handle, and Home and End move it to the limits.
     */
    @Test
    fun `arrow keys move the handle`() {
        val panel = panel(group())
        panel.focus(widget(panel, "handle"))

        panel.keyPressed(KeyEvent(GLFW.GLFW_KEY_RIGHT, 0, 0))
        assertEquals("30,70", widget(panel, "group").inputValue)
        panel.keyPressed(KeyEvent(GLFW.GLFW_KEY_HOME, 0, 0))

        assertEquals("10,90", widget(panel, "group").inputValue)
        assertEquals(listOf("group=30,70", "group=10,90"), changes)
    }

    /**
     * Verifies that a vertical group stacks its panels and resizes them with Up and Down.
     */
    @Test
    fun `vertical groups stack their panels`() {
        val panel = panel(group(Orientation.VERTICAL))
        assertEquals(50, widget(panel, "one").bounds.height)
        panel.focus(widget(panel, "handle"))

        panel.keyPressed(KeyEvent(GLFW.GLFW_KEY_DOWN, 0, 0))

        assertEquals("30,70", widget(panel, "group").inputValue)
    }

    /**
     * Verifies that sizes set by the server are applied within the limits of the panels.
     */
    @Test
    fun `server sizes are applied`() {
        val panel = panel(group())

        widget(panel, "group").applyValue("40,60")

        assertEquals("40,60", widget(panel, "group").inputValue)
        assertEquals(emptyList(), changes)
    }
}
