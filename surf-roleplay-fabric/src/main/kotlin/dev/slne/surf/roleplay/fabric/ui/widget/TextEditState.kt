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
 * The text and cursor of an input field, edited under a [TextFilter].
 *
 * @param initial the initial text, which is not checked by the filter
 * @property filter the filter that every edit by the player must pass
 */
class TextEditState(initial: String = "", val filter: TextFilter = TextFilter.NONE) {

    /**
     * The text of the field. Setting it replaces the text without asking the filter and moves the
     * cursor to its end.
     */
    var text: String = initial
        set(value) {
            field = value
            cursor = value.length
        }

    /**
     * The cursor position, from `0` (before the first character) to the text length.
     */
    var cursor: Int = initial.length
        set(value) {
            field = value.coerceIn(0, text.length)
        }

    /**
     * Inserts text at the cursor if the filter accepts the result, cutting it to the filter's
     * maximum length first.
     *
     * @param inserted the text to insert
     * @return whether the text changed
     */
    fun insert(inserted: String): Boolean {
        val room = filter.maxLength?.let { (it - text.length).coerceAtLeast(0) } ?: inserted.length
        val clipped = inserted.take(room)
        if (clipped.isEmpty()) return false
        val candidate = text.substring(0, cursor) + clipped + text.substring(cursor)
        if (!filter.accepts(candidate)) return false
        val newCursor = cursor + clipped.length
        text = candidate
        cursor = newCursor
        return true
    }

    /**
     * Removes the character before the cursor if the filter accepts the result.
     *
     * @return whether the text changed
     */
    fun backspace(): Boolean {
        if (cursor == 0) return false
        val candidate = text.substring(0, cursor - 1) + text.substring(cursor)
        if (!filter.accepts(candidate)) return false
        val newCursor = cursor - 1
        text = candidate
        cursor = newCursor
        return true
    }

    /**
     * Removes the character after the cursor if the filter accepts the result.
     *
     * @return whether the text changed
     */
    fun delete(): Boolean {
        if (cursor >= text.length) return false
        val candidate = text.substring(0, cursor) + text.substring(cursor + 1)
        if (!filter.accepts(candidate)) return false
        val newCursor = cursor
        text = candidate
        cursor = newCursor
        return true
    }

    /**
     * Moves the cursor, keeping it within the text.
     *
     * @param delta the number of characters to move; negative moves left
     */
    fun moveCursor(delta: Int) {
        cursor += delta
    }
}
