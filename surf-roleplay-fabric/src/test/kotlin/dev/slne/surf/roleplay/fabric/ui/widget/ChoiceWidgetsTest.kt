package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.RadioGroupNode
import dev.slne.surf.roleplay.protocol.screen.RadioOption
import dev.slne.surf.roleplay.protocol.screen.SliderNode
import dev.slne.surf.roleplay.protocol.screen.SwitchNode
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for switches, radio groups and sliders in the mod.
 */
class ChoiceWidgetsTest {

    /**
     * The widgets reported as changed, in order.
     */
    private val changes = mutableListOf<String>()

    /**
     * A context that records focus and change reports.
     */
    private val context = object : UiContext {
        override var focusedWidget: Widget? = null
        override fun focus(widget: Widget?) {
            focusedWidget = widget
        }
        override fun requestLayout() = Unit
        override fun actionTriggered(widget: Widget, submitsInput: Boolean) = Unit
        override fun valueChanged(widget: Widget, immediate: Boolean) {
            changes += widget.id
        }
        override var clipboard: String = ""
    }

    /**
     * Returns a key event for a key without modifiers.
     *
     * @param key the GLFW key code
     * @return the event
     */
    private fun key(key: Int) = KeyEvent(key, 0, 0)

    /**
     * Verifies that a switch toggles on Space and on a click on a label that targets it.
     */
    @Test
    fun `switches toggle`() {
        val switch = WidgetFactory.create(SwitchNode("s", notifyChange = true)) as SwitchWidget

        switch.keyPressed(context, key(GLFW.GLFW_KEY_SPACE))
        assertEquals("true", switch.inputValue)
        switch.labelClicked(context)
        assertEquals("false", switch.inputValue)
        assertEquals(listOf("s", "s"), changes)
    }

    /**
     * Verifies that the arrow keys move a radio group's selection, skipping disabled options and
     * wrapping at the ends.
     */
    @Test
    fun `radio groups move their selection`() {
        val radio = WidgetFactory.create(
            RadioGroupNode("r", options = listOf(RadioOption("a"), RadioOption("b", enabled = false), RadioOption("c"))),
        ) as RadioGroupWidget

        radio.keyPressed(context, key(GLFW.GLFW_KEY_DOWN))
        assertEquals("a", radio.inputValue)
        radio.keyPressed(context, key(GLFW.GLFW_KEY_DOWN))
        assertEquals("c", radio.inputValue)
        radio.keyPressed(context, key(GLFW.GLFW_KEY_DOWN))
        assertEquals("a", radio.inputValue)
        radio.keyPressed(context, key(GLFW.GLFW_KEY_UP))
        assertEquals("c", radio.inputValue)
        radio.select(1, context)
        assertEquals("c", radio.inputValue)
    }

    /**
     * Verifies that pressing the track moves the thumb to the snapped value, and that the keys
     * move it by steps, by ten steps and to the ends.
     */
    @Test
    fun `sliders snap and step`() {
        val slider = WidgetFactory.create(SliderNode("v", values = listOf(0.0), step = 10.0)) as SliderWidget
        slider.bounds = Rect(0, 0, 110, 12)

        slider.press(context, 5.0 + 37, 6.0)
        assertEquals("40", slider.inputValue)
        slider.keyPressed(context, key(GLFW.GLFW_KEY_RIGHT))
        assertEquals("50", slider.inputValue)
        slider.keyPressed(context, key(GLFW.GLFW_KEY_PAGE_DOWN))
        assertEquals("0", slider.inputValue)
        slider.keyPressed(context, key(GLFW.GLFW_KEY_END))
        assertEquals("100", slider.inputValue)
        slider.keyPressed(context, key(GLFW.GLFW_KEY_UP))
        assertEquals("100", slider.inputValue)
    }

    /**
     * Verifies that a range slider moves the thumb closest to a press, keeps its thumbs in order
     * while dragging, and switches the keyboard thumb with Space.
     */
    @Test
    fun `range sliders keep their thumbs in order`() {
        val slider = WidgetFactory.create(SliderNode("v", values = listOf(20.0, 80.0))) as SliderWidget
        slider.bounds = Rect(0, 0, 110, 12)

        slider.press(context, 5.0 + 70, 6.0)
        assertEquals("20,70", slider.inputValue)
        slider.mouseDragged(context, 5.0 + 10, 6.0)
        assertEquals("20,20", slider.inputValue)
        slider.keyPressed(context, key(GLFW.GLFW_KEY_SPACE))
        assertEquals(0, slider.activeThumb)
        slider.keyPressed(context, key(GLFW.GLFW_KEY_LEFT))
        assertEquals("19,20", slider.inputValue)
    }

    /**
     * Verifies that a vertical slider has its minimum at the bottom.
     */
    @Test
    fun `vertical sliders grow upwards`() {
        val slider = WidgetFactory.create(SliderNode("v", values = listOf(0.0), orientation = Orientation.VERTICAL)) as SliderWidget
        slider.bounds = Rect(0, 0, 12, 110)

        slider.press(context, 6.0, 5.0 + 25)

        assertEquals("75", slider.inputValue)
        assertTrue(slider.draggable)
    }
}
