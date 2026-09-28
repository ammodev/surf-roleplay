package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * One horizontal run of pixels.
 *
 * @property y the row
 * @property x0 the first column, inclusive
 * @property x1 the last column, exclusive
 */
data class Span(val y: Int, val x0: Int, val x1: Int)

/**
 * Computes the pixel rows of rectangles with rounded corners.
 */
object RoundedShape {

    /**
     * Returns the rows that fill a rounded rectangle, one span per row from top to bottom.
     *
     * @param rect the rectangle
     * @param radius the corner radius, limited to half the shorter side
     * @return the spans
     */
    fun spans(rect: Rect, radius: Int): List<Span> {
        if (rect.width <= 0 || rect.height <= 0) return emptyList()
        val r = radius.coerceAtMost(minOf(rect.width, rect.height) / 2).coerceAtLeast(0)
        return (0 until rect.height).map { row ->
            val inset = inset(row, rect.height, r)
            Span(rect.y + row, rect.x + inset, rect.right - inset)
        }
    }

    /**
     * Returns the spans of a one-pixel border along the inside of a rounded rectangle.
     *
     * @param rect the rectangle
     * @param radius the corner radius of the outer edge
     * @return the spans, row by row, left before right
     */
    fun borderSpans(rect: Rect, radius: Int): List<Span> {
        val outer = spans(rect, radius)
        val innerRect = Rect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2)
        val inner = spans(innerRect, (radius - 1).coerceAtLeast(0)).associateBy { it.y }
        return outer.flatMap { row ->
            val hole = inner[row.y]
            if (hole == null || hole.x0 >= hole.x1) {
                listOf(row)
            } else {
                listOfNotNull(
                    Span(row.y, row.x0, hole.x0).takeIf { it.x0 < it.x1 },
                    Span(row.y, hole.x1, row.x1).takeIf { it.x0 < it.x1 },
                )
            }
        }
    }

    /**
     * Computes how far a row of a rounded rectangle is inset from the sides.
     *
     * @param row the row, from `0` at the top
     * @param height the height of the rectangle
     * @param radius the corner radius
     * @return the inset in pixels
     */
    private fun inset(row: Int, height: Int, radius: Int): Int {
        if (radius == 0) return 0
        val distance = when {
            row < radius -> radius - row - 0.5
            row >= height - radius -> row - (height - radius) + 0.5
            else -> return 0
        }
        return (radius - sqrt((radius * radius - distance * distance).coerceAtLeast(0.0))).roundToInt()
    }
}
