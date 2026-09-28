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
 * Which corners of a rectangle are rounded.
 *
 * @property topLeft whether the top left corner is rounded
 * @property topRight whether the top right corner is rounded
 * @property bottomLeft whether the bottom left corner is rounded
 * @property bottomRight whether the bottom right corner is rounded
 */
data class Corners(val topLeft: Boolean, val topRight: Boolean, val bottomLeft: Boolean, val bottomRight: Boolean) {
    /**
     * Holds the common corner sets.
     */
    companion object {
        /**
         * Every corner is rounded.
         */
        val ALL: Corners = Corners(true, true, true, true)

        /**
         * No corner is rounded.
         */
        val NONE: Corners = Corners(false, false, false, false)

        /**
         * Only the two left corners are rounded.
         */
        val LEFT: Corners = Corners(true, false, true, false)

        /**
         * Only the two right corners are rounded.
         */
        val RIGHT: Corners = Corners(false, true, false, true)

        /**
         * Only the two top corners are rounded.
         */
        val TOP: Corners = Corners(true, true, false, false)

        /**
         * Only the two bottom corners are rounded.
         */
        val BOTTOM: Corners = Corners(false, false, true, true)
    }
}

/**
 * Computes the pixel rows of rectangles with rounded corners.
 */
object RoundedShape {

    /**
     * Returns the rows that fill a rounded rectangle, one span per row from top to bottom.
     *
     * @param rect the rectangle
     * @param radius the corner radius, limited to half the shorter side
     * @param corners the corners that are rounded
     * @return the spans
     */
    fun spans(rect: Rect, radius: Int, corners: Corners = Corners.ALL): List<Span> {
        if (rect.width <= 0 || rect.height <= 0) return emptyList()
        val r = radius.coerceAtMost(minOf(rect.width, rect.height) / 2).coerceAtLeast(0)
        return (0 until rect.height).map { row ->
            val inset = inset(row, rect.height, r)
            val top = row < rect.height / 2
            val left = if (if (top) corners.topLeft else corners.bottomLeft) inset else 0
            val right = if (if (top) corners.topRight else corners.bottomRight) inset else 0
            Span(rect.y + row, rect.x + left, rect.right - right)
        }
    }

    /**
     * Returns the spans of a one-pixel border along the inside of a rounded rectangle.
     *
     * @param rect the rectangle
     * @param radius the corner radius of the outer edge
     * @param corners the corners that are rounded
     * @return the spans, row by row, left before right
     */
    fun borderSpans(rect: Rect, radius: Int, corners: Corners = Corners.ALL): List<Span> {
        val outer = spans(rect, radius, corners)
        val innerRect = Rect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2)
        val inner = spans(innerRect, (radius - 1).coerceAtLeast(0), corners).associateBy { it.y }
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
