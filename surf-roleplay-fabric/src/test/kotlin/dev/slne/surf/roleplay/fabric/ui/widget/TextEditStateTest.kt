package dev.slne.surf.roleplay.fabric.ui.widget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [TextEditState] and its filters.
 */
class TextEditStateTest {

    /**
     * Verifies that typing inserts at the cursor and moves it.
     */
    @Test
    fun `insert places text at the cursor`() {
        val state = TextEditState("ac")
        state.cursor = 1

        state.insert("b")

        assertEquals("abc", state.text)
        assertEquals(2, state.cursor)
    }

    /**
     * Verifies that backspace and delete remove the character before and after the cursor.
     */
    @Test
    fun `backspace and delete remove around the cursor`() {
        val state = TextEditState("abcd")
        state.cursor = 2

        state.backspace()
        state.delete()

        assertEquals("ad", state.text)
        assertEquals(1, state.cursor)
    }

    /**
     * Verifies that the cursor stays within the text when moved.
     */
    @Test
    fun `cursor moves stay within the text`() {
        val state = TextEditState("ab")

        state.moveCursor(5)
        assertEquals(2, state.cursor)
        state.moveCursor(-9)
        assertEquals(0, state.cursor)
    }

    /**
     * Verifies that a maximum length truncates inserted text to what still fits.
     */
    @Test
    fun `max length truncates insertion`() {
        val state = TextEditState("abc", TextFilter.maxLength(5))

        state.insert("defgh")

        assertEquals("abcde", state.text)
        assertEquals(5, state.cursor)
    }

    /**
     * Verifies that the number filter refuses letters and allows a leading minus only when
     * negative numbers are allowed.
     */
    @Test
    fun `number filter accepts only digits and an allowed leading minus`() {
        val positive = TextEditState("", NumberFilter(min = 0, max = null))
        positive.insert("-")
        positive.insert("1a")
        assertEquals("", positive.text)
        positive.insert("12")
        assertEquals("12", positive.text)

        val signed = TextEditState("", NumberFilter(min = -10, max = 10))
        signed.insert("-")
        signed.insert("5")
        assertEquals("-5", signed.text)
    }

    /**
     * Verifies that the number filter refuses edits that would exceed the maximum, while it still
     * allows values below the minimum as intermediate input.
     */
    @Test
    fun `number filter blocks values above max but allows below min`() {
        val state = TextEditState("", NumberFilter(min = 18, max = 99))

        state.insert("1")
        assertEquals("1", state.text)
        state.insert("00")
        assertEquals("1", state.text)
        state.insert("9")
        assertEquals("19", state.text)
    }

    /**
     * Verifies that setting the text from the server bypasses the filter.
     */
    @Test
    fun `setting the text replaces it and moves the cursor to the end`() {
        val state = TextEditState("abc", TextFilter.maxLength(2))
        state.cursor = 1

        state.text = "hello"

        assertEquals("hello", state.text)
        assertEquals(5, state.cursor)
    }

    /**
     * Verifies the validity rules of number inputs: required, minimum, maximum and a lone minus.
     */
    @Test
    fun `number validity checks required and range`() {
        val filter = NumberFilter(min = 18, max = 99)

        assertFalse(filter.isValid("", required = true))
        assertTrue(filter.isValid("", required = false))
        assertFalse(filter.isValid("17", required = false))
        assertTrue(filter.isValid("18", required = false))
        assertFalse(NumberFilter(min = -5, max = 5).isValid("-", required = false))
    }

    /**
     * Verifies that select all spans the whole text and that typing replaces the selection.
     */
    @Test
    fun `typing replaces the selection`() {
        val state = TextEditState("Hallo Welt")

        state.selectAll()
        assertEquals("Hallo Welt", state.selectedText)
        state.insert("X")

        assertEquals("X", state.text)
        assertEquals(1, state.cursor)
        assertFalse(state.hasSelection)
    }

    /**
     * Verifies that backspace and delete remove a selection instead of a single character.
     */
    @Test
    fun `backspace and delete remove the selection`() {
        val state = TextEditState("abcdef")
        state.select(1, 4)
        state.backspace()
        assertEquals("aef", state.text)
        assertEquals(1, state.cursor)

        state.select(3, 1)
        state.delete()
        assertEquals("a", state.text)
        assertEquals(1, state.cursor)
    }

    /**
     * Verifies that moving with extend keeps the anchor and that setting the cursor collapses the
     * selection.
     */
    @Test
    fun `extending moves keep the anchor`() {
        val state = TextEditState("abcdef")
        state.cursor = 2

        state.moveCursor(2, extend = true)
        assertEquals(2, state.selectionStart)
        assertEquals(4, state.selectionEnd)
        state.moveCursorTo(0, extend = true)
        assertEquals("ab", state.selectedText)
        assertEquals(0, state.cursor)

        state.cursor = 3
        assertFalse(state.hasSelection)
    }

    /**
     * Verifies that an arrow move without extend collapses a selection to its edge in the
     * direction of the move.
     */
    @Test
    fun `plain arrow moves collapse the selection to its edge`() {
        val state = TextEditState("abcdef")

        state.select(1, 4)
        state.moveCursor(-1)
        assertEquals(1, state.cursor)
        assertFalse(state.hasSelection)

        state.select(4, 1)
        state.moveCursor(1)
        assertEquals(4, state.cursor)
    }

    /**
     * Verifies that word moves jump to word boundaries and extend the selection on request.
     */
    @Test
    fun `word moves jump to boundaries`() {
        val state = TextEditState("Hallo schöne Welt")

        state.moveWord(forward = false)
        assertEquals(13, state.cursor)
        state.moveWord(forward = false, extend = true)
        assertEquals("schöne ", state.selectedText)
        state.cursor = 0
        state.moveWord(forward = true)
        assertEquals(6, state.cursor)
    }

    /**
     * Verifies that deleting by word removes up to the previous or next word boundary.
     */
    @Test
    fun `word deletion removes to the boundary`() {
        val state = TextEditState("Hallo schöne Welt")

        assertTrue(state.deleteWordBackward())
        assertEquals("Hallo schöne ", state.text)
        state.cursor = 0
        assertTrue(state.deleteWordForward())
        assertEquals("schöne ", state.text)
        assertEquals(0, state.cursor)
        assertFalse(state.deleteWordBackward())
    }

    /**
     * Verifies that a filter can refuse removing a selection.
     */
    @Test
    fun `filter guards removal of a selection`() {
        val state = TextEditState("12", object : TextFilter {
            /**
             * Accepts every text but the empty one.
             *
             * @param candidate the text
             * @return whether the text is not empty
             */
            override fun accepts(candidate: String): Boolean = candidate.isNotEmpty()
        })

        state.selectAll()
        assertFalse(state.backspace())
        assertEquals("12", state.text)
    }

    /**
     * Verifies that a maximum length counts the selection that typing replaces as free room.
     */
    @Test
    fun `max length counts the replaced selection as room`() {
        val state = TextEditState("abcde", TextFilter.maxLength(5))

        state.select(1, 3)
        state.insert("XYZ")

        assertEquals("aXYde", state.text)
    }
}
