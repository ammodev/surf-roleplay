package dev.slne.surf.roleplay.protocol.screen

/**
 * The view of a data table: how it is sorted, filtered and paged, and which rows are selected.
 * It travels as the table's input value, a JSON object such as
 * `{"sort":"name","desc":false,"filter":"rtw","page":0,"selected":["r1","r4"]}`.
 *
 * @property sort the key of the column the rows are sorted by, or `null` for the order the
 *           server sent
 * @property desc whether the rows are sorted descending
 * @property filter the filter text, empty for none
 * @property page the index of the shown page, from 0
 * @property selected the ids of the selected rows
 */
data class DataTableView(
    val sort: String? = null,
    val desc: Boolean = false,
    val filter: String = "",
    val page: Int = 0,
    val selected: List<String> = emptyList(),
) {
    /**
     * Writes the view as a JSON object with every field.
     *
     * @return the JSON text
     */
    fun toJson(): String = buildString {
        append("{\"sort\":")
        if (sort == null) append("null") else appendString(sort)
        append(",\"desc\":").append(desc)
        append(",\"filter\":").appendString(filter)
        append(",\"page\":").append(page)
        append(",\"selected\":[")
        selected.forEachIndexed { index, id ->
            if (index > 0) append(',')
            appendString(id)
        }
        append("]}")
    }

    /**
     * Reads data table views.
     */
    companion object {
        /**
         * The longest text accepted as a view.
         */
        const val MAX_LENGTH: Int = 8192

        /**
         * Reads a view from a JSON object. Missing fields take their defaults; unknown fields,
         * fields of the wrong type, negative pages, nested arrays, texts longer than
         * [MAX_LENGTH] and anything that is not one JSON object make the text invalid. An empty
         * text is the default view.
         *
         * @param json the JSON text
         * @return the view, or `null` if the text is not a valid view
         */
        fun parse(json: String): DataTableView? {
            if (json.length > MAX_LENGTH) return null
            if (json.isBlank()) return DataTableView()
            val fields = JsonReader(json).readObjectOrNull() ?: return null
            var view = DataTableView()
            for ((key, value) in fields) {
                view = when (key) {
                    "sort" -> when (value) {
                        null -> view.copy(sort = null)
                        is String -> view.copy(sort = value)
                        else -> return null
                    }
                    "desc" -> view.copy(desc = value as? Boolean ?: return null)
                    "filter" -> view.copy(filter = value as? String ?: return null)
                    "page" -> view.copy(page = (value as? Long)?.takeIf { it in 0..Int.MAX_VALUE }?.toInt() ?: return null)
                    "selected" -> view.copy(selected = (value as? List<*>)?.map { it as? String ?: return null } ?: return null)
                    else -> return null
                }
            }
            return view
        }
    }
}

/**
 * Appends a text as a JSON string with quotes and escapes.
 *
 * @param text the text
 * @return this builder
 */
private fun StringBuilder.appendString(text: String): StringBuilder {
    append('"')
    for (char in text) {
        when {
            char == '"' -> append("\\\"")
            char == '\\' -> append("\\\\")
            char == '\n' -> append("\\n")
            char == '\r' -> append("\\r")
            char == '\t' -> append("\\t")
            char < ' ' -> append("\\u").append(char.code.toString(16).padStart(4, '0'))
            else -> append(char)
        }
    }
    return append('"')
}

/**
 * Reads the small subset of JSON a data table view uses: one object whose values are strings,
 * booleans, whole numbers, `null`, or arrays of strings and `null`; arrays do not nest.
 *
 * @property text the JSON text
 */
private class JsonReader(private val text: String) {

    /**
     * The position of the next character to read.
     */
    private var position = 0

    /**
     * Reads the whole text as one object.
     *
     * @return the fields of the object in order, or `null` if the text is not one such object
     */
    fun readObjectOrNull(): List<Pair<String, Any?>>? = try {
        skipSpace()
        val fields = readObject()
        skipSpace()
        if (position != text.length) null else fields
    } catch (_: IllegalArgumentException) {
        null
    }

    /**
     * Reads an object.
     *
     * @return its fields in order
     */
    private fun readObject(): List<Pair<String, Any?>> {
        expect('{')
        val fields = mutableListOf<Pair<String, Any?>>()
        skipSpace()
        if (peek() == '}') {
            position++
            return fields
        }
        while (true) {
            skipSpace()
            val key = readString()
            skipSpace()
            expect(':')
            fields += key to readValue()
            skipSpace()
            when (next()) {
                ',' -> continue
                '}' -> return fields
                else -> throw IllegalArgumentException("expected , or }")
            }
        }
    }

    /**
     * Reads a string, a boolean, a whole number, `null` or an array.
     *
     * @return the value
     */
    private fun readValue(): Any? {
        skipSpace()
        return when (val char = peek()) {
            '"' -> readString()
            '[' -> readArray()
            't' -> literal("true", true)
            'f' -> literal("false", false)
            'n' -> literal("null", null)
            else -> if (char == '-' || char in '0'..'9') readNumber() else throw IllegalArgumentException("unexpected $char")
        }
    }

    /**
     * Reads an array.
     *
     * @return its values
     */
    private fun readArray(): List<Any?> {
        expect('[')
        val values = mutableListOf<Any?>()
        skipSpace()
        if (peek() == ']') {
            position++
            return values
        }
        while (true) {
            skipSpace()
            if (peek() == '[' || peek() == '{') throw IllegalArgumentException("nested value")
            values += readValue()
            skipSpace()
            when (next()) {
                ',' -> continue
                ']' -> return values
                else -> throw IllegalArgumentException("expected , or ]")
            }
        }
    }

    /**
     * Reads a whole number.
     *
     * @return the number
     */
    private fun readNumber(): Long {
        val start = position
        if (peek() == '-') position++
        while (position < text.length && text[position] in '0'..'9') position++
        return text.substring(start, position).toLongOrNull() ?: throw IllegalArgumentException("bad number")
    }

    /**
     * Reads a string with its escapes.
     *
     * @return the string
     */
    private fun readString(): String {
        expect('"')
        val result = StringBuilder()
        while (true) {
            val char = next()
            when (char) {
                '"' -> return result.toString()
                '\\' -> when (val escape = next()) {
                    '"', '\\', '/' -> result.append(escape)
                    'b' -> result.append('\b')
                    'f' -> result.append('\u000C')
                    'n' -> result.append('\n')
                    'r' -> result.append('\r')
                    't' -> result.append('\t')
                    'u' -> {
                        if (position + 4 > text.length) throw IllegalArgumentException("short escape")
                        val digits = text.substring(position, position + 4)
                        if (!digits.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) throw IllegalArgumentException("bad escape")
                        result.append(digits.toInt(16).toChar())
                        position += 4
                    }
                    else -> throw IllegalArgumentException("bad escape")
                }
                else -> result.append(char)
            }
        }
    }

    /**
     * Reads a fixed word.
     *
     * @param word the word
     * @param value the value it stands for
     * @return the value
     */
    private fun literal(word: String, value: Any?): Any? {
        if (!text.startsWith(word, position)) throw IllegalArgumentException("expected $word")
        position += word.length
        return value
    }

    /**
     * Skips white space.
     */
    private fun skipSpace() {
        while (position < text.length && text[position].isWhitespace()) position++
    }

    /**
     * Returns the next character without reading it.
     *
     * @return the character
     */
    private fun peek(): Char = text.getOrNull(position) ?: throw IllegalArgumentException("unexpected end")

    /**
     * Reads the next character.
     *
     * @return the character
     */
    private fun next(): Char = peek().also { position++ }

    /**
     * Reads a character that must come next.
     *
     * @param char the character
     */
    private fun expect(char: Char) {
        if (next() != char) throw IllegalArgumentException("expected $char")
    }
}
