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
     * Verifies that the next boundary skips the rest of the word and the whitespace after it,
     * stopping at the start of the next word.
     */
    @Test
    fun `next word stops at the start of the next word`() {
        val text = "Hallo schöne  Welt"

        assertEquals(6, TextBoundaries.nextWord(text, 0))
        assertEquals(6, TextBoundaries.nextWord(text, 5))
        assertEquals(14, TextBoundaries.nextWord(text, 6))
        assertEquals(14, TextBoundaries.nextWord(text, 7))
        assertEquals(18, TextBoundaries.nextWord(text, 14))
        assertEquals(18, TextBoundaries.nextWord(text, 18))
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
        assertEquals(11, TextBoundaries.nextWord(text, 7))
        assertEquals(7, TextBoundaries.previousWord(text, 10))
        assertEquals(5, TextBoundaries.previousWord(text, 7))
        assertEquals(0, TextBoundaries.previousWord(text, 5))
    }

    /**
     * Verifies that an apostrophe between letters belongs to the word, while one at the edge of
     * a word is punctuation.
     */
    @Test
    fun `apostrophes between letters stay inside the word`() {
        val text = "Er geht's heim"

        assertEquals(3, TextBoundaries.previousWord(text, 9))
        assertEquals(10, TextBoundaries.nextWord(text, 3))
        assertEquals(TextRange(3, 9), TextBoundaries.wordAt(text, 7))
        assertEquals(TextRange(3, 9), TextBoundaries.wordAt("Er geht’s heim", 7))
        assertEquals(TextRange(0, 1), TextBoundaries.wordAt("'hallo'", 0))
        assertEquals(TextRange(1, 6), TextBoundaries.wordAt("'hallo'", 3))
    }

    /**
     * Verifies that combining marks belong to the word they follow.
     */
    @Test
    fun `combining marks stay inside the word`() {
        val text = "éte x"

        assertEquals(TextRange(0, 4), TextBoundaries.wordAt(text, 1))
        assertEquals(5, TextBoundaries.nextWord(text, 0))
        assertEquals(0, TextBoundaries.previousWord(text, 4))
    }

    /**
     * Verifies that line breaks count as whitespace.
     */
    @Test
    fun `line breaks are whitespace`() {
        val text = "eins\nzwei"

        assertEquals(5, TextBoundaries.nextWord(text, 0))
        assertEquals(5, TextBoundaries.nextWord(text, 4))
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
