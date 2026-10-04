package dev.slne.surf.roleplay.fabric.ui.widget

/**
 * Decides which texts an input field accepts while the player types.
 */
interface TextFilter {
    /**
     * The largest number of characters the field holds, or `null` for no limit. Inserted text is
     * cut to fit before [accepts] is asked.
     */
    val maxLength: Int? get() = null

    /**
     * Checks whether the field may hold a text.
     *
     * @param candidate the text the field would hold after an edit
     * @return whether the edit is allowed
     */
    fun accepts(candidate: String): Boolean

    /**
     * Holds the common filters.
     */
    companion object {
        /**
         * The filter that accepts every text.
         */
        val NONE: TextFilter = object : TextFilter {
            /**
             * Accepts every text.
             *
             * @param candidate the text
             * @return `true`
             */
            override fun accepts(candidate: String): Boolean = true
        }

        /**
         * Creates a filter that limits the number of characters.
         *
         * @param length the largest number of characters, or `null` for no limit
         * @return the filter
         */
        fun maxLength(length: Int?): TextFilter = object : TextFilter {
            override val maxLength: Int? = length

            /**
             * Accepts texts no longer than the limit.
             *
             * @param candidate the text
             * @return whether the text fits
             */
            override fun accepts(candidate: String): Boolean = length == null || candidate.length <= length
        }
    }
}

/**
 * The filter of a whole-number field.
 *
 * It accepts digits and, when negative numbers are allowed, a leading minus. It refuses edits that
 * would move the number beyond the bound it is typed towards: above [max] for positive numbers and
 * below [min] for negative numbers. Numbers between zero and [min] are accepted as intermediate
 * input and reported by [isValid].
 *
 * @property min the smallest allowed number, or `null` for no lower bound
 * @property max the largest allowed number, or `null` for no upper bound
 */
class NumberFilter(val min: Long?, val max: Long?) : TextFilter {

    /**
     * Whether a leading minus is allowed.
     */
    private val allowsNegative: Boolean = min == null || min < 0

    /**
     * Accepts an empty text, a lone allowed minus, and numbers within the bound they are typed
     * towards.
     *
     * @param candidate the text
     * @return whether the text is acceptable while typing
     */
    override fun accepts(candidate: String): Boolean {
        if (candidate.isEmpty()) return true
        if (candidate == "-") return allowsNegative
        if (!NUMBER.matches(candidate)) return false
        if (candidate.startsWith("-") && !allowsNegative) return false
        val value = candidate.toLongOrNull() ?: return false
        if (max != null && value > max) return false
        if (min != null && value < 0 && value < min) return false
        return true
    }

    /**
     * Checks whether a text is a valid value of the field.
     *
     * @param text the text
     * @param required whether an empty text is invalid
     * @return whether the text is empty and optional, or a number within the bounds
     */
    fun isValid(text: String, required: Boolean): Boolean {
        if (text.isEmpty()) return !required
        val value = text.toLongOrNull() ?: return false
        return (min == null || value >= min) && (max == null || value <= max)
    }

    /**
     * Holds the number pattern.
     */
    private companion object {
        /**
         * An optional minus followed by digits.
         */
        val NUMBER = Regex("-?[0-9]+")
    }
}

/**
 * The text, cursor and selection of an input field, edited under a [TextFilter].
 *
 * The selection runs between the [anchor] and the [cursor]; it is empty while both are equal.
 * Typing, deleting and pasting replace a non-empty selection.
 *
 * @param initial the initial text, which is not checked by the filter
 * @property filter the filter that every edit by the player must pass
 */
class TextEditState(initial: String = "", val filter: TextFilter = TextFilter.NONE) {

    /**
     * The text of the field. Setting it replaces the text without asking the filter and moves the
     * cursor to its end, clearing the selection.
     */
    var text: String = initial
        set(value) {
            field = value
            cursor = value.length
        }

    /**
     * The fixed end of the selection, from `0` to the text length.
     */
    var anchor: Int = initial.length
        private set

    /**
     * The cursor position, from `0` (before the first character) to the text length. Setting it
     * clears the selection.
     */
    var cursor: Int = initial.length
        set(value) {
            field = value.coerceIn(0, text.length)
            anchor = field
        }

    /**
     * The start of the selection, the smaller of [anchor] and [cursor].
     */
    val selectionStart: Int get() = minOf(anchor, cursor)

    /**
     * The end of the selection, the larger of [anchor] and [cursor].
     */
    val selectionEnd: Int get() = maxOf(anchor, cursor)

    /**
     * Whether at least one character is selected.
     */
    val hasSelection: Boolean get() = anchor != cursor

    /**
     * The selected characters, empty without a selection.
     */
    val selectedText: String get() = text.substring(selectionStart, selectionEnd)

    /**
     * Selects a range, with the anchor at [anchorAt] and the cursor at [cursorAt]. Both are kept
     * within the text.
     *
     * @param anchorAt the fixed end of the selection
     * @param cursorAt the moving end of the selection, where the cursor is drawn
     */
    fun select(anchorAt: Int, cursorAt: Int) {
        cursor = cursorAt
        anchor = anchorAt.coerceIn(0, text.length)
    }

    /**
     * Selects the whole text, with the cursor at its end.
     */
    fun selectAll() {
        select(0, text.length)
    }

    /**
     * Moves the cursor to a position, keeping it within the text.
     *
     * @param position the new cursor position
     * @param extend whether to keep the anchor and so extend the selection; otherwise the
     *        selection is cleared
     */
    fun moveCursorTo(position: Int, extend: Boolean = false) {
        if (extend) select(anchor, position) else cursor = position
    }

    /**
     * Inserts text at the cursor, replacing the selection, if the filter accepts the result. The
     * text is cut to the filter's maximum length first, counting the replaced selection as free
     * room.
     *
     * @param inserted the text to insert
     * @return whether the text changed
     */
    fun insert(inserted: String): Boolean {
        val start = selectionStart
        val end = selectionEnd
        val kept = text.length - (end - start)
        val room = filter.maxLength?.let { (it - kept).coerceAtLeast(0) } ?: inserted.length
        val clipped = inserted.take(room)
        if (clipped.isEmpty()) return false
        return replace(start, end, clipped)
    }

    /**
     * Removes the selection, or else the character before the cursor, if the filter accepts the
     * result.
     *
     * @return whether the text changed
     */
    fun backspace(): Boolean = when {
        hasSelection -> replace(selectionStart, selectionEnd, "")
        cursor == 0 -> false
        else -> replace(cursor - 1, cursor, "")
    }

    /**
     * Removes the selection, or else the character after the cursor, if the filter accepts the
     * result.
     *
     * @return whether the text changed
     */
    fun delete(): Boolean = when {
        hasSelection -> replace(selectionStart, selectionEnd, "")
        cursor >= text.length -> false
        else -> replace(cursor, cursor + 1, "")
    }

    /**
     * Removes the selection, or else everything from the previous word boundary to the cursor,
     * if the filter accepts the result.
     *
     * @return whether the text changed
     */
    fun deleteWordBackward(): Boolean = when {
        hasSelection -> replace(selectionStart, selectionEnd, "")
        cursor == 0 -> false
        else -> replace(TextBoundaries.previousWord(text, cursor), cursor, "")
    }

    /**
     * Removes the selection, or else everything from the cursor to the next word boundary, if
     * the filter accepts the result.
     *
     * @return whether the text changed
     */
    fun deleteWordForward(): Boolean = when {
        hasSelection -> replace(selectionStart, selectionEnd, "")
        cursor >= text.length -> false
        else -> replace(cursor, TextBoundaries.nextWord(text, cursor), "")
    }

    /**
     * Replaces a range of the text if the filter accepts the result, and places the cursor after
     * the replacement.
     *
     * @param start the index of the first replaced character
     * @param end the index after the last replaced character
     * @param replacement the new characters
     * @return whether the filter accepted the edit
     */
    private fun replace(start: Int, end: Int, replacement: String): Boolean {
        val candidate = text.substring(0, start) + replacement + text.substring(end)
        if (!filter.accepts(candidate)) return false
        text = candidate
        cursor = start + replacement.length
        return true
    }

    /**
     * Moves the cursor by characters, keeping it within the text. Without [extend], a selection
     * is cleared instead, leaving the cursor at the selection's edge in the direction of the move.
     *
     * @param delta the number of characters to move; negative moves left
     * @param extend whether to extend the selection
     */
    fun moveCursor(delta: Int, extend: Boolean = false) {
        when {
            extend -> moveCursorTo(cursor + delta, extend = true)
            hasSelection && delta < 0 -> cursor = selectionStart
            hasSelection && delta > 0 -> cursor = selectionEnd
            else -> cursor += delta
        }
    }

    /**
     * Moves the cursor to the previous or next word boundary, as found by [TextBoundaries].
     *
     * @param forward whether to move to the next boundary; otherwise to the previous one
     * @param extend whether to extend the selection; otherwise the selection is cleared
     */
    fun moveWord(forward: Boolean, extend: Boolean = false) {
        val target = if (forward) TextBoundaries.nextWord(text, cursor) else TextBoundaries.previousWord(text, cursor)
        moveCursorTo(target, extend)
    }
}
