package dev.slne.surf.roleplay.fabric.ui.widget

/**
 * A range of characters of a text: from [start] up to, but not including, [end].
 *
 * @property start the index of the first character
 * @property end the index after the last character
 */
data class TextRange(val start: Int, val end: Int)

/**
 * Finds word and line boundaries in a text the way a browser text field on Windows does.
 *
 * Characters fall into three classes: whitespace, word characters and punctuation, which is
 * everything else. Word characters are letters, digits, the underscore, combining marks, and
 * apostrophes between two letters, as in "geht's". A word is a run of characters of one class
 * other than whitespace.
 */
object TextBoundaries {

    /**
     * The classes a character can belong to.
     */
    private enum class CharClass {
        /**
         * Spaces, tabs and line breaks.
         */
        SPACE,

        /**
         * Letters, digits, the underscore, combining marks and apostrophes between letters.
         */
        WORD,

        /**
         * Every other character.
         */
        PUNCTUATION,
    }

    /**
     * The apostrophes that join the letters around them into one word.
     */
    private const val APOSTROPHES: String = "'’"

    /**
     * Returns the class of a character in a text.
     *
     * @param text the text
     * @param index the index of the character
     * @return the class
     */
    private fun classOf(text: String, index: Int): CharClass {
        val char = text[index]
        return when {
            char.isWhitespace() -> CharClass.SPACE
            char.isLetterOrDigit() || char == '_' || isCombiningMark(char) -> CharClass.WORD
            char in APOSTROPHES && text.getOrNull(index - 1)?.isLetter() == true && text.getOrNull(index + 1)?.isLetter() == true -> CharClass.WORD
            else -> CharClass.PUNCTUATION
        }
    }

    /**
     * Checks whether a character is a combining mark, which belongs to the character before it.
     *
     * @param char the character
     * @return whether it is a non-spacing, spacing or enclosing combining mark
     */
    private fun isCombiningMark(char: Char): Boolean = when (Character.getType(char).toByte()) {
        Character.NON_SPACING_MARK, Character.COMBINING_SPACING_MARK, Character.ENCLOSING_MARK -> true
        else -> false
    }

    /**
     * Finds the boundary a word-wise move to the left stops at: it skips the whitespace before
     * the position, then the run of word characters or punctuation before that.
     *
     * @param text the text
     * @param from the position to move from
     * @return the position of the boundary, `0` at the start of the text
     */
    fun previousWord(text: String, from: Int): Int {
        var position = from.coerceIn(0, text.length)
        while (position > 0 && classOf(text, position - 1) == CharClass.SPACE) position--
        if (position == 0) return 0
        val run = classOf(text, position - 1)
        while (position > 0 && classOf(text, position - 1) == run) position--
        return position
    }

    /**
     * Finds the boundary a word-wise move to the right stops at: it skips the rest of the run of
     * word characters or punctuation the position is in, then the whitespace after it, and so
     * stops at the start of the next word.
     *
     * @param text the text
     * @param from the position to move from
     * @return the position of the boundary, the text length at the end of the text
     */
    fun nextWord(text: String, from: Int): Int {
        var position = from.coerceIn(0, text.length)
        if (position < text.length) {
            val run = classOf(text, position)
            if (run != CharClass.SPACE) {
                while (position < text.length && classOf(text, position) == run) position++
            }
        }
        while (position < text.length && classOf(text, position) == CharClass.SPACE) position++
        return position
    }

    /**
     * Finds the run of characters of one class that contains a character, as a double click
     * selects it. An index at the end of the text stands for the last character.
     *
     * @param text the text
     * @param index the index of the character
     * @return the run, empty for an empty text
     */
    fun wordAt(text: String, index: Int): TextRange {
        if (text.isEmpty()) return TextRange(0, 0)
        val at = index.coerceIn(0, text.length - 1)
        val run = classOf(text, at)
        var start = at
        while (start > 0 && classOf(text, start - 1) == run) start--
        var end = at + 1
        while (end < text.length && classOf(text, end) == run) end++
        return TextRange(start, end)
    }

    /**
     * Finds the line between line breaks that a cursor position is on. The line breaks belong to
     * no line.
     *
     * @param text the text; line breaks are `\n`
     * @param position the cursor position
     * @return the line
     */
    fun lineAt(text: String, position: Int): TextRange {
        val at = position.coerceIn(0, text.length)
        val start = if (at == 0) 0 else text.lastIndexOf('\n', at - 1) + 1
        val end = text.indexOf('\n', at).let { if (it < 0) text.length else it }
        return TextRange(start, end)
    }
}
