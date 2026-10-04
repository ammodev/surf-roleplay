package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for placing the cursor and selecting with single, double, triple and Shift clicks in text
 * fields and textareas.
 */
class TextFieldClicksTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        /**
         * The height of one line.
         */
        override val lineHeight: Int = 9

        /**
         * Measures component JSON as 5 pixels per character.
         *
         * @param json the text
         * @return the width
         */
        override fun width(json: String): Int = json.length * 5

        /**
         * Measures plain text as 5 pixels per character.
         *
         * @param text the text
         * @return the width
         */
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * A context that tracks the focus, reports whether Shift is held during a click, and has a
     * clock the tests set.
     */
    private val context = object : UiContext {
        /**
         * The focused widget.
         */
        override var focusedWidget: Widget? = null

        /**
         * Whether Shift is held during the click.
         */
        override var shiftClick: Boolean = false

        /**
         * The current time in milliseconds, which only the tests move.
         */
        override var timeMillis: Long = 1_000

        /**
         * Records the focused widget.
         *
         * @param widget the widget, or `null` to clear the focus
         */
        override fun focus(widget: Widget?) {
            focusedWidget = widget
        }

        /**
         * Ignores layout requests.
         */
        override fun requestLayout() = Unit

        /**
         * Ignores widget actions.
         *
         * @param widget the widget
         * @param submitsInput whether the action submits the input
         */
        override fun actionTriggered(widget: Widget, submitsInput: Boolean) = Unit

        /**
         * Finds no widgets.
         *
         * @param id the id
         * @return `null`
         */
        override fun widget(id: String): Widget? = null

        /**
         * The clipboard text.
         */
        override var clipboard: String = ""
    }

    /**
     * Verifies that a click after the double-click interval counts as a single click.
     */
    @Test
    fun `slow clicks stay single clicks`() {
        val field = field("Hallo schöne Welt")

        clickAt(field, 8)
        context.timeMillis += ClickCounter.INTERVAL_MILLIS + 1
        clickAt(field, 8)

        assertFalse(field.edit.hasSelection)
        assertEquals(8, field.edit.cursor)
    }

    /**
     * Verifies that a key press between two clicks makes the second a single click.
     */
    @Test
    fun `a key press starts a new click count`() {
        val field = field("Hallo schöne Welt")

        clickAt(field, 8)
        field.keyPressed(context, TestKeys.key(GLFW.GLFW_KEY_RIGHT))
        clickAt(field, 8)

        assertFalse(field.edit.hasSelection)
        assertEquals(8, field.edit.cursor)
    }

    /**
     * Creates a laid-out single-line field.
     *
     * @param text the text of the field
     * @return the field
     */
    private fun field(text: String): TextInputWidget = TextInputWidget("t", TextEditState(text)).apply {
        contentSize(measurer)
        bounds = Rect(0, 0, 200, 20)
    }

    /**
     * Clicks a single-line field at a character index.
     *
     * @param field the field
     * @param index the index of the clicked character; the click lands two pixels into it
     */
    private fun clickAt(field: TextInputWidget, index: Int) {
        field.mouseClicked(context, UiMetrics.WIDGET_PADDING + index * 5 + 2.0, 10.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
    }

    /**
     * Verifies that the character index under a horizontal offset is found within a line.
     */
    @Test
    fun `char index is the character under the offset`() {
        val line = TextLines.Line(2, 7)

        assertEquals(2, TextLines.charIndexAt("xxHallo", line, -3, measurer::plainWidth))
        assertEquals(2, TextLines.charIndexAt("xxHallo", line, 4, measurer::plainWidth))
        assertEquals(3, TextLines.charIndexAt("xxHallo", line, 5, measurer::plainWidth))
        assertEquals(6, TextLines.charIndexAt("xxHallo", line, 99, measurer::plainWidth))
        assertEquals(4, TextLines.charIndexAt("abcd", TextLines.Line(4, 4), 10, measurer::plainWidth))
    }

    /**
     * Verifies that a click focuses the field and places the cursor at the clicked position.
     */
    @Test
    fun `a click places the cursor where it lands`() {
        val field = field("Hallo schöne Welt")

        clickAt(field, 8)

        assertSame(field, context.focusedWidget)
        assertEquals(8, field.edit.cursor)
        assertFalse(field.edit.hasSelection)
    }

    /**
     * Verifies that a double click selects the word, a triple click the whole text, and a fourth
     * click places the cursor again.
     */
    @Test
    fun `double and triple clicks select the word and the text`() {
        val field = field("Hallo schöne Welt")

        clickAt(field, 8)
        clickAt(field, 8)
        assertEquals("schöne", field.edit.selectedText)
        clickAt(field, 8)
        assertEquals("Hallo schöne Welt", field.edit.selectedText)
        clickAt(field, 8)
        assertFalse(field.edit.hasSelection)
    }

    /**
     * Verifies that a click with Shift extends the selection from the cursor.
     */
    @Test
    fun `shift click extends the selection`() {
        val field = field("Hallo schöne Welt")
        field.edit.cursor = 0

        context.shiftClick = true
        clickAt(field, 5)

        assertEquals("Hallo", field.edit.selectedText)
    }

    /**
     * Verifies that pressing in a field and dragging selects from the pressed position to the
     * mouse, and that dragging before the text extends the selection to its start.
     */
    @Test
    fun `dragging selects text in a field`() {
        val field = field("Hallo schöne Welt")
        assertTrue(field.draggable)

        clickAt(field, 2)
        field.mouseDragged(context, UiMetrics.WIDGET_PADDING + 8 * 5 + 2.0, 10.0)
        assertEquals("llo sc", field.edit.selectedText)

        field.mouseDragged(context, -20.0, 10.0)
        assertEquals("Ha", field.edit.selectedText)
        assertEquals(0, field.edit.cursor)
    }

    /**
     * Verifies that dragging in a textarea selects across lines, and that dragging above the
     * first line reaches it.
     */
    @Test
    fun `dragging selects across textarea lines`() {
        val area = TextareaWidget("a", TextEditState("eins zwei\ndrei vier"))
        area.bounds = Rect(0, 0, 200, 40)
        area.layoutLines(measurer)
        assertTrue(area.draggable)

        area.mouseClicked(context, UiMetrics.WIDGET_PADDING + 5 * 5 + 1.0, 4 + 2.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        area.mouseDragged(context, UiMetrics.WIDGET_PADDING + 4 * 5 + 1.0, 4 + 9 + 2.0)
        assertEquals("zwei\ndrei", area.edit.selectedText)

        area.mouseDragged(context, UiMetrics.WIDGET_PADDING + 1.0, -5.0)
        assertEquals("eins ", area.edit.selectedText)
    }

    /**
     * Verifies that a double click in a textarea selects the word and a triple click its line
     * between line breaks.
     */
    @Test
    fun `textarea double and triple clicks select the word and the line`() {
        val area = TextareaWidget("a", TextEditState("eins zwei\ndrei vier"))
        area.bounds = Rect(0, 0, 200, 40)
        area.layoutLines(measurer)
        val x = UiMetrics.WIDGET_PADDING + 2 * 5 + 2.0
        val y = 4 + 9 + 2.0

        area.mouseClicked(context, x, y, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        assertEquals(12, area.edit.cursor)
        area.mouseClicked(context, x, y, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        assertEquals("drei", area.edit.selectedText)
        area.mouseClicked(context, x, y, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        assertEquals("drei vier", area.edit.selectedText)
    }
}
