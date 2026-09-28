package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.CarouselContentNode
import dev.slne.surf.roleplay.protocol.screen.CarouselItemNode
import dev.slne.surf.roleplay.protocol.screen.CarouselNextNode
import dev.slne.surf.roleplay.protocol.screen.CarouselNode
import dev.slne.surf.roleplay.protocol.screen.CarouselPreviousNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for carousels in the mod.
 */
class CarouselWidgetsTest {

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
     * Returns the widget with an id.
     *
     * @param panel the panel
     * @param id the id
     * @return the widget
     */
    private fun widget(panel: ScreenPanel, id: String): Widget = WidgetTree.find(panel.root, id)!!

    /**
     * Creates a carousel 200 wide with five slides of a basis.
     *
     * @param basis the share of the content every slide takes
     * @param loop whether the last slide is followed by the first
     * @param orientation whether the slides move sideways or up and down
     * @return the node
     */
    private fun carousel(basis: Double = 100.0, loop: Boolean = false, orientation: Orientation = Orientation.HORIZONTAL) = CarouselNode(
        "carousel",
        width = Sizing.fixed(200),
        height = if (orientation == Orientation.VERTICAL) Sizing.fixed(120) else Sizing.FIT,
        orientation = orientation,
        loop = loop,
        notifyChange = true,
        children = listOf(
            CarouselContentNode("content", children = (0 until 5).map { CarouselItemNode("slide$it", basis = basis, children = listOf(LabelNode("text$it", text = "$it"))) }),
            CarouselPreviousNode("prev"),
            CarouselNextNode("next"),
        ),
    )

    /**
     * Verifies that the buttons sit beside the content, and that the next button shows the next
     * slide in the place of the first without firing an action.
     */
    @Test
    fun `next shows the next slide`() {
        val panel = panel(carousel())
        val content = widget(panel, "content").bounds
        assertTrue(widget(panel, "prev").bounds.right <= content.x)
        assertTrue(widget(panel, "next").bounds.x >= content.right)
        assertEquals(content.x, widget(panel, "slide0").bounds.x)

        click(panel, "next")

        assertEquals("1", widget(panel, "carousel").inputValue)
        assertEquals(content.x, widget(panel, "slide1").bounds.x)
        assertEquals(listOf("carousel=1"), changes)
        assertEquals(emptyList(), actions)
    }

    /**
     * Verifies that without loop the previous button is disabled on the first slide and the next
     * button on the last one.
     */
    @Test
    fun `buttons stop at the ends`() {
        val panel = panel(carousel(basis = 50.0))
        assertFalse(widget(panel, "prev").enabled)

        repeat(5) { click(panel, "next") }

        assertEquals("3", widget(panel, "carousel").inputValue)
        assertFalse(widget(panel, "next").enabled)
        assertEquals(widget(panel, "content").bounds.right, widget(panel, "slide4").bounds.right)
    }

    /**
     * Verifies that a looping carousel wraps from the first slide to the last one.
     */
    @Test
    fun `loop wraps around`() {
        val panel = panel(carousel(loop = true))
        assertTrue(widget(panel, "prev").enabled)

        click(panel, "prev")

        assertEquals("4", widget(panel, "carousel").inputValue)
    }

    /**
     * Verifies that Left and Right move the carousel while a widget inside it has the focus.
     */
    @Test
    fun `arrow keys move the carousel`() {
        val panel = panel(carousel())
        panel.focus(widget(panel, "next"))

        panel.keyPressed(KeyEvent(GLFW.GLFW_KEY_RIGHT, 0, 0))
        panel.keyPressed(KeyEvent(GLFW.GLFW_KEY_RIGHT, 0, 0))
        panel.keyPressed(KeyEvent(GLFW.GLFW_KEY_LEFT, 0, 0))

        assertEquals("1", widget(panel, "carousel").inputValue)
    }

    /**
     * Verifies that a vertical carousel stacks its slides at their share of its height.
     */
    @Test
    fun `vertical carousels move up and down`() {
        val panel = panel(carousel(basis = 50.0, orientation = Orientation.VERTICAL))
        val content = widget(panel, "content").bounds
        assertTrue(widget(panel, "prev").bounds.bottom <= content.y)
        val slide = widget(panel, "slide0").bounds

        click(panel, "next")

        assertEquals(content.y, widget(panel, "slide1").bounds.y)
        assertTrue(slide.height > 0 && slide.height < content.height)
    }

    /**
     * Verifies that an index set by the server is applied within the slides.
     */
    @Test
    fun `server index is applied`() {
        val panel = panel(carousel())

        widget(panel, "carousel").applyValue("3")
        panel.requestLayout()
        panel.layoutIfNeeded(measurer, 400, 300)

        assertEquals("3", widget(panel, "carousel").inputValue)
        assertEquals(widget(panel, "content").bounds.x, widget(panel, "slide3").bounds.x)
    }
}
