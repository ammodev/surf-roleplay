package dev.slne.surf.roleplay.fabric.ui.widget

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the word and line boundaries of [TextBoundaries].
 */
class TextBoundariesTest {

    /**
     * Verifies that the previous boundary skips whitespace and then one word.
     */
    @Test
    fun `previous word skips whitespace then a word`() {
        val text = "Hallo schöne  Welt"

        assertEquals(14, TextBoundaries.previousWord(text, 18))
        assertEquals(6, TextBoundaries.previousWord(text, 14))
        assertEquals(6, TextBoundaries.previousWord(text, 9))
        assertEquals(0, TextBoundaries.previousWord(text, 6))
        assertEquals(0, TextBoundaries.previousWord(text, 0))
    }

    /**
     * Verifies that the next boundary skips whitespace and then one word.
     */
    @Test
    fun `next word skips whitespace then a word`() {
        val text = "Hallo schöne  Welt"

        assertEquals(5, TextBoundaries.nextWord(text, 0))
        assertEquals(12, TextBoundaries.nextWord(text, 5))
        assertEquals(18, TextBoundaries.nextWord(text, 12))
        assertEquals(18, TextBoundaries.nextWord(text, 18))
        assertEquals(12, TextBoundaries.nextWord(text, 7))
    }

    /**
     * Verifies that a run of punctuation is its own word and that underscores and digits belong to
     * words.
     */
    @Test
    fun `punctuation is its own run`() {
        val text = "foo_1.,bar baz"

        assertEquals(5, TextBoundaries.nextWord(text, 0))
        assertEquals(7, TextBoundaries.nextWord(text, 5))
        assertEquals(10, TextBoundaries.nextWord(text, 7))
        assertEquals(7, TextBoundaries.previousWord(text, 10))
        assertEquals(5, TextBoundaries.previousWord(text, 7))
        assertEquals(0, TextBoundaries.previousWord(text, 5))
    }

    /**
     * Verifies that line breaks count as whitespace.
     */
    @Test
    fun `line breaks are whitespace`() {
        val text = "eins\nzwei"

        assertEquals(9, TextBoundaries.nextWord(text, 4))
        assertEquals(0, TextBoundaries.previousWord(text, 5))
    }

    /**
     * Verifies that the word at a character is the run of its class around it.
     */
    @Test
    fun `word at a character is the run around it`() {
        val text = "Hallo, liebe  Welt"

        assertEquals(TextRange(0, 5), TextBoundaries.wordAt(text, 2))
        assertEquals(TextRange(5, 6), TextBoundaries.wordAt(text, 5))
        assertEquals(TextRange(7, 12), TextBoundaries.wordAt(text, 7))
        assertEquals(TextRange(12, 14), TextBoundaries.wordAt(text, 13))
        assertEquals(TextRange(14, 18), TextBoundaries.wordAt(text, 18))
        assertEquals(TextRange(0, 0), TextBoundaries.wordAt("", 0))
    }

    /**
     * Verifies that the line at a character spans between the surrounding line breaks.
     */
    @Test
    fun `line at a character spans between line breaks`() {
        val text = "eins\nzwei drei\n\nvier"

        assertEquals(TextRange(0, 4), TextBoundaries.lineAt(text, 2))
        assertEquals(TextRange(5, 14), TextBoundaries.lineAt(text, 5))
        assertEquals(TextRange(5, 14), TextBoundaries.lineAt(text, 14))
        assertEquals(TextRange(15, 15), TextBoundaries.lineAt(text, 15))
        assertEquals(TextRange(16, 20), TextBoundaries.lineAt(text, 20))
    }
}
