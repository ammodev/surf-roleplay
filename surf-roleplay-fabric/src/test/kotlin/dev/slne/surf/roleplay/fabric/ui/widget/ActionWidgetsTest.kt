package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.Corners
import dev.slne.surf.roleplay.fabric.ui.RoundedShape
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.ToggleGroupItem
import dev.slne.surf.roleplay.protocol.screen.ToggleGroupNode
import dev.slne.surf.roleplay.protocol.screen.ToggleNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for corner masks, button groups, toggles and toggle groups in the mod.
 */
class ActionWidgetsTest {

    /**
     * The widgets whose actions were triggered, in order.
     */
    private val actions = mutableListOf<String>()

    /**
     * A context that records actions.
     */
    private val context = object : UiContext {
        override val focusedWidget: Widget? = null
        override fun focus(widget: Widget?) = Unit
        override fun openDropdown(dropdown: DropdownWidget) = Unit
        override fun closeDropdown() = Unit
        override fun requestLayout() = Unit
        override fun actionTriggered(widget: Widget, submitsInput: Boolean) {
            actions += "${widget.id}:$submitsInput"
        }
        override var clipboard: String = ""
    }

    /**
     * Verifies that a corner mask rounds only the chosen corners.
     */
    @Test
    fun `corner masks round only chosen corners`() {
        val rect = Rect(0, 0, 20, 10)

        val left = RoundedShape.spans(rect, 3, Corners.LEFT)
        assertTrue(left.first().x0 > 0)
        assertEquals(20, left.first().x1)
        val none = RoundedShape.spans(rect, 3, Corners.NONE)
        assertTrue(none.all { it.x0 == 0 && it.x1 == 20 })
    }

    /**
     * Verifies that a horizontal button group rounds only the outer corners of its first and last
     * part, and a vertical group the top and bottom.
     */
    @Test
    fun `button groups join their parts`() {
        val horizontal = WidgetFactory.create(
            ButtonGroupNode("g", children = listOf(ButtonNode("a"), ButtonGroupSeparatorNode("s"), ButtonNode("b"), ButtonNode("c"))),
        ) as ButtonGroupWidget
        horizontal.assignCorners()
        assertEquals(listOf(Corners.LEFT, Corners.NONE, Corners.NONE, Corners.RIGHT), horizontal.children.map { it.corners })

        val vertical = WidgetFactory.create(ButtonGroupNode("v", orientation = Orientation.VERTICAL, children = listOf(ButtonNode("a"), ButtonNode("b")))) as ButtonGroupWidget
        vertical.assignCorners()
        assertEquals(listOf(Corners.TOP, Corners.BOTTOM), vertical.children.map { it.corners })
    }

    /**
     * Verifies that pressing a toggle switches it and triggers an action that does not submit
     * input, and that its value is its state.
     */
    @Test
    fun `toggles switch and trigger actions`() {
        val toggle = WidgetFactory.create(ToggleNode("bold")) as ToggleWidget

        toggle.press(context)

        assertEquals("true", toggle.inputValue)
        assertEquals(listOf("bold:false"), actions)
    }

    /**
     * Verifies that a single toggle group keeps at most one item on, a multiple one several, and
     * that disabled items do not switch.
     */
    @Test
    fun `toggle groups switch their items`() {
        val items = listOf(ToggleGroupItem("a"), ToggleGroupItem("b"), ToggleGroupItem("c", enabled = false))
        val single = WidgetFactory.create(ToggleGroupNode("s", items = items)) as ToggleGroupWidget
        val multiple = WidgetFactory.create(ToggleGroupNode("m", items = items, multiple = true)) as ToggleGroupWidget

        single.switch(0, context)
        single.switch(1, context)
        assertEquals("b", single.inputValue)
        single.switch(1, context)
        assertEquals("", single.inputValue)

        multiple.switch(1, context)
        multiple.switch(0, context)
        multiple.switch(2, context)
        assertEquals("a,b", multiple.inputValue)
    }

    /**
     * Verifies that the arrow keys move the highlighted item of a toggle group, skipping nothing
     * and stopping at the ends.
     */
    @Test
    fun `toggle group highlight moves with the arrow keys`() {
        val group = WidgetFactory.create(ToggleGroupNode("g", items = listOf(ToggleGroupItem("a"), ToggleGroupItem("b")))) as ToggleGroupWidget

        group.moveHighlight(1)
        assertEquals(1, group.highlighted)
        group.moveHighlight(1)
        assertEquals(1, group.highlighted)
        group.moveHighlight(-5)
        assertEquals(0, group.highlighted)
    }
}
