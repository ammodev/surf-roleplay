package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.Rect

/**
 * Receives filled rectangles that are drawn together as one GUI element.
 */
fun interface FillTarget {
    /**
     * Draws filled rectangles as one GUI element.
     *
     * @param data the rectangles, five numbers each: left, top, right, bottom and the ARGB colour
     * @param count the number of rectangles in [data], at least one
     */
    fun submit(data: IntArray, count: Int)
}

/**
 * Fills shapes made of many rectangles, each shape as a single GUI element, and counts the
 * elements it submits.
 *
 * @property target the receiver of the rectangles
 */
class ShapeFills(private val target: FillTarget) {

    /**
     * The number of GUI elements submitted so far.
     */
    var elements: Int = 0
        private set

    /**
     * Fills rectangles in one colour as one GUI element. Empty rectangles are skipped, and
     * nothing is submitted when none is left.
     *
     * @param rects the rectangles
     * @param color the ARGB colour
     */
    fun rects(rects: List<Rect>, color: Int) {
        val data = IntArray(rects.size * FillBatchRenderState.FIELDS)
        var count = 0
        for (rect in rects) {
            if (rect.width <= 0 || rect.height <= 0) continue
            val at = count * FillBatchRenderState.FIELDS
            data[at] = rect.x
            data[at + 1] = rect.y
            data[at + 2] = rect.right
            data[at + 3] = rect.bottom
            data[at + 4] = color
            count++
        }
        if (count == 0) return
        elements++
        target.submit(data, count)
    }

    /**
     * Fills a rectangle with rounded corners as one GUI element.
     *
     * @param rect the rectangle
     * @param color the ARGB colour
     * @param radius the corner radius
     * @param corners the corners that are rounded
     */
    fun rounded(rect: Rect, color: Int, radius: Int, corners: Corners = Corners.ALL) {
        rects(RoundedShape.merge(RoundedShape.spans(rect, radius, corners)), color)
    }

    /**
     * Draws a one-pixel border with rounded corners along the inside of a rectangle as one GUI
     * element.
     *
     * @param rect the rectangle
     * @param color the ARGB colour
     * @param radius the corner radius
     * @param corners the corners that are rounded
     */
    fun roundedBorder(rect: Rect, color: Int, radius: Int, corners: Corners = Corners.ALL) {
        rects(RoundedShape.merge(RoundedShape.borderSpans(rect, radius, corners)), color)
    }

    /**
     * Draws a dashed one-pixel border along the inside of a rectangle as one GUI element: the
     * dashes along the top and bottom edges from left to right, then those along the left and
     * right edges from top to bottom.
     *
     * @param rect the rectangle
     * @param color the ARGB colour
     * @param dash the length of a dash
     * @param space the length of the space between two dashes
     */
    fun dashedBorder(rect: Rect, color: Int, dash: Int, space: Int) {
        if (rect.width <= 0 || rect.height <= 0) return
        val step = (dash + space).coerceAtLeast(1)
        val dashes = mutableListOf<Rect>()
        var x = rect.x
        while (x < rect.right) {
            val length = minOf(dash, rect.right - x)
            dashes += Rect(x, rect.y, length, 1)
            dashes += Rect(x, rect.bottom - 1, length, 1)
            x += step
        }
        var y = rect.y
        while (y < rect.bottom) {
            val length = minOf(dash, rect.bottom - y)
            dashes += Rect(rect.x, y, 1, length)
            dashes += Rect(rect.right - 1, y, 1, length)
            y += step
        }
        rects(dashes, color)
    }
}
