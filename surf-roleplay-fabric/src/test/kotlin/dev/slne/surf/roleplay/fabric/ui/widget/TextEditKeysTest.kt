package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import dev.slne.surf.roleplay.protocol.screen.TextInputType
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Tests for the editing keys of [TextEditKeys].
 */
class TextEditKeysTest {

    /**
     * A context that tracks the focus and holds a clipboard text.
     */
    private val context = object : UiContext {
        override var focusedWidget: Widget? = null

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
        override var clipboard: String = ""
    }

    /**
     * Returns a key event with the platform's editing shortcut modifier for [ctrl].
     *
     * @param key the GLFW key code
     * @param ctrl whether the editing shortcut modifier is held
     * @param shift whether Shift is held
     * @return the event
     */
    private fun key(key: Int, ctrl: Boolean = false, shift: Boolean = false) = TestKeys.key(key, ctrl, shift)

    /**
     * Verifies that Control+A selects the whole text.
     */
    @Test
    fun `control a selects all`() {
        val edit = TextEditState("Hallo Welt")
        edit.cursor = 2

        assertEquals(TextEditKeys.Result.MOVED, TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_A, ctrl = true)))
        assertEquals("Hallo Welt", edit.selectedText)
    }

    /**
     * Verifies that Control with the arrows jumps by word and Shift extends the selection.
     */
    @Test
    fun `control arrows jump by word and shift extends`() {
        val edit = TextEditState("Hallo schöne Welt")

        TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_LEFT, ctrl = true))
        assertEquals(13, edit.cursor)
        TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_LEFT, ctrl = true, shift = true))
        assertEquals("schöne ", edit.selectedText)
        TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_RIGHT, ctrl = true))
        assertEquals(13, edit.cursor)
        assertFalse(edit.hasSelection)
    }

    /**
     * Verifies that Shift with the arrows, Home and End extends the selection, and plain Home and
     * End move the cursor.
     */
    @Test
    fun `shift arrows home and end extend the selection`() {
        val edit = TextEditState("abcdef")
        edit.cursor = 3

        TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_LEFT, shift = true))
        assertEquals("c", edit.selectedText)
        TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_HOME, shift = true))
        assertEquals("abc", edit.selectedText)
        TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_END))
        assertEquals(6, edit.cursor)
        assertFalse(edit.hasSelection)
        TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_HOME))
        assertEquals(0, edit.cursor)
        TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_END, shift = true))
        assertEquals("abcdef", edit.selectedText)
    }

    /**
     * Verifies that Control with Backspace and Delete removes by word and reports the change.
     */
    @Test
    fun `control backspace and delete remove by word`() {
        val edit = TextEditState("Hallo schöne Welt")

        assertEquals(TextEditKeys.Result.CHANGED, TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_BACKSPACE, ctrl = true)))
        assertEquals("Hallo schöne ", edit.text)
        edit.cursor = 0
        assertEquals(TextEditKeys.Result.CHANGED, TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_DELETE, ctrl = true)))
        assertEquals("schöne ", edit.text)
        assertEquals(TextEditKeys.Result.MOVED, TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_BACKSPACE)))
    }

    /**
     * Verifies that a text handled as one word, as in password fields, makes word moves go to
     * the start and end and word deletion remove to the start and end.
     */
    @Test
    fun `one word texts move and delete to the ends`() {
        val edit = TextEditState("geheim wort")
        edit.cursor = 8

        TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_LEFT, ctrl = true), oneWord = true)
        assertEquals(0, edit.cursor)
        TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_RIGHT, ctrl = true, shift = true), oneWord = true)
        assertEquals("geheim wort", edit.selectedText)

        edit.cursor = 8
        assertEquals(TextEditKeys.Result.CHANGED, TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_BACKSPACE, ctrl = true), oneWord = true))
        assertEquals("ort", edit.text)
        edit.cursor = 1
        TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_DELETE, ctrl = true), oneWord = true)
        assertEquals("o", edit.text)
    }

    /**
     * Verifies that password fields treat their text as one word.
     */
    @Test
    fun `password fields jump over the whole text`() {
        val field = WidgetFactory.create(TextInputNode("p", value = "geheim wort", inputType = TextInputType.PASSWORD)) as TextInputWidget
        field.keyPressed(context, key(GLFW.GLFW_KEY_LEFT, ctrl = true))

        assertEquals(0, field.edit.cursor)
    }

    /**
     * Verifies that keys without an editing meaning are left to the widget.
     */
    @Test
    fun `other keys are ignored`() {
        val edit = TextEditState("abc")

        assertEquals(TextEditKeys.Result.IGNORED, TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_TAB)))
        assertEquals(TextEditKeys.Result.IGNORED, TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_ENTER)))
        assertEquals(TextEditKeys.Result.IGNORED, TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_K, ctrl = true)))
    }
}
