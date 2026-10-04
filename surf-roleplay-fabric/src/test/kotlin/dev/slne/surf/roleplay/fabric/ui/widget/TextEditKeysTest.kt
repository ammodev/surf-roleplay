package dev.slne.surf.roleplay.fabric.ui.widget

import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Tests for the editing keys of [TextEditKeys].
 */
class TextEditKeysTest {

    /**
     * Returns a key event.
     *
     * @param key the GLFW key code
     * @param ctrl whether Control is held
     * @param shift whether Shift is held
     * @return the event
     */
    private fun key(key: Int, ctrl: Boolean = false, shift: Boolean = false) =
        KeyEvent(key, 0, (if (ctrl) GLFW.GLFW_MOD_CONTROL else 0) or (if (shift) GLFW.GLFW_MOD_SHIFT else 0))

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
        assertEquals(12, edit.cursor)
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
        assertEquals(" schöne ", edit.text)
        assertEquals(TextEditKeys.Result.MOVED, TextEditKeys.handle(edit, key(GLFW.GLFW_KEY_BACKSPACE)))
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
