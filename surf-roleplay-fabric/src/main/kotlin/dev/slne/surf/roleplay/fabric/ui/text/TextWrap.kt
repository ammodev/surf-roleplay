package dev.slne.surf.roleplay.fabric.ui.text

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.Size

/**
 * Breaks plain strings into lines of a maximum width.
 */
object TextWrap {

    /**
     * Breaks a string into lines no wider than a width. Lines break at spaces, and the spaces at
     * a break are dropped. A word wider than the width is broken between characters, and every
     * line break in the string starts a new line.
     *
     * @param text the string
     * @param maxWidth the largest width of a line
     * @param width measures a string
     * @return the lines, at least one
     */
    fun lines(text: String, maxWidth: Int, width: (String) -> Int): List<String> {
        val lines = mutableListOf<String>()
        for (paragraph in text.split('\n')) {
            var line = ""
            for (word in paragraph.split(' ')) {
                val candidate = if (line.isEmpty()) word else "$line $word"
                if (width(candidate) <= maxWidth) {
                    line = candidate
                    continue
                }
                if (line.isNotEmpty()) lines += line
                line = word
                while (line.length > 1 && width(line) > maxWidth) {
                    var fits = 1
                    while (fits < line.length && width(line.substring(0, fits + 1)) <= maxWidth) fits++
                    lines += line.substring(0, fits)
                    line = line.substring(fits)
                }
            }
            lines += line
        }
        return lines
    }
}

/**
 * Measures blocks of wrapped text.
 */
object TextBlock {

    /**
     * The space between two lines of a wrapped text, in GUI pixels.
     */
    const val LINE_GAP: Int = 2

    /**
     * Computes the size of a text wrapped to a width: the width of its widest line, and the
     * height of its lines with a gap between each two.
     *
     * @param measurer the text measurer
     * @param json the text as component JSON
     * @param maxWidth the largest width of a line
     * @param maxLines the largest number of lines shown, or `0` for no limit
     * @return the size
     */
    fun size(measurer: TextMeasurer, json: String, maxWidth: Int, maxLines: Int = 0): Size {
        val widths = measurer.lineWidths(json, maxWidth)
        val shown = if (maxLines > 0 && widths.size > maxLines) widths.take(maxLines) else widths
        val count = shown.size.coerceAtLeast(1)
        return Size(shown.maxOrNull() ?: 0, height(measurer, count))
    }

    /**
     * Computes the height of a number of lines with a gap between each two.
     *
     * @param measurer the text measurer
     * @param lines the number of lines
     * @return the height
     */
    fun height(measurer: TextMeasurer, lines: Int): Int = lines * measurer.lineHeight + (lines - 1).coerceAtLeast(0) * LINE_GAP
}
