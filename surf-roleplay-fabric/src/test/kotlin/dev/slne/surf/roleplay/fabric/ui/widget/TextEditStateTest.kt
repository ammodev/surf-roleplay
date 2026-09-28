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
}
