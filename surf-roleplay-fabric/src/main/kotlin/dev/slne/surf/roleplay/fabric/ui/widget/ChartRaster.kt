package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.Corners
import dev.slne.surf.roleplay.fabric.ui.RoundedShape
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeTokens

/**
 * A list of filled rectangles in screen pixels, drawn in order. A rectangle that continues the
 * previous one in the same colour, side by side at the same height or stacked at the same width,
 * is merged into it, which covers the same pixels with fewer fills.
 */
class FillList {
    /**
     * The rectangles, five numbers each: left, top, right, bottom and the ARGB colour.
     */
    private var data = IntArray(INITIAL * FIELDS)

    /**
     * The number of rectangles.
     */
    var count: Int = 0
        private set

    /**
     * Adds a rectangle, merging it into the previous one where it continues it. Empty
     * rectangles are left out.
     *
     * @param x0 the left edge
     * @param y0 the top edge
     * @param x1 the right edge, exclusive
     * @param y1 the bottom edge, exclusive
     * @param color the ARGB colour
     */
    fun add(x0: Int, y0: Int, x1: Int, y1: Int, color: Int) {
        if (x1 <= x0 || y1 <= y0) return
        if (count > 0) {
            val last = (count - 1) * FIELDS
            if (data[last + 4] == color) {
                if (data[last + 1] == y0 && data[last + 3] == y1 && data[last + 2] == x0) {
                    data[last + 2] = x1
                    return
                }
                if (data[last] == x0 && data[last + 2] == x1 && data[last + 3] == y0) {
                    data[last + 3] = y1
                    return
                }
            }
        }
        if ((count + 1) * FIELDS > data.size) data = data.copyOf(data.size * 2)
        val at = count * FIELDS
        data[at] = x0
        data[at + 1] = y0
        data[at + 2] = x1
        data[at + 3] = y1
        data[at + 4] = color
        count++
    }

    /**
     * Adds a rectangle given in GUI pixels at a scale.
     *
     * @param rect the rectangle in GUI pixels
     * @param scale the number of screen pixels per GUI pixel
     * @param originX the left edge of the origin in screen pixels, subtracted from every position
     * @param originY the top edge of the origin in screen pixels
     * @param color the ARGB colour
     */
    fun addScaled(rect: Rect, scale: Int, originX: Int, originY: Int, color: Int) =
        add(rect.x * scale - originX, rect.y * scale - originY, rect.right * scale - originX, rect.bottom * scale - originY, color)

    /**
     * Adds a rectangle with rounded corners given in GUI pixels at a scale, one row of GUI pixels
     * at a time, as [UiGraphics.fillRounded] fills it.
     *
     * @param rect the rectangle in GUI pixels
     * @param radius the corner radius
     * @param corners the corners that are rounded
     * @param scale the number of screen pixels per GUI pixel
     * @param originX the left edge of the origin in screen pixels, subtracted from every position
     * @param originY the top edge of the origin in screen pixels
     * @param color the ARGB colour
     */
    fun addRounded(rect: Rect, radius: Int, corners: Corners, scale: Int, originX: Int, originY: Int, color: Int) {
        RoundedShape.spans(rect, radius, corners).forEach { span ->
            add(span.x0 * scale - originX, span.y * scale - originY, span.x1 * scale - originX, (span.y + 1) * scale - originY, color)
        }
    }

    /**
     * Returns a rectangle.
     *
     * @param index the index of the rectangle
     * @return its left, top, right and bottom edge and its colour
     */
    internal fun fill(index: Int): List<Int> = (0 until FIELDS).map { data[index * FIELDS + it] }

    /**
     * Draws every rectangle moved by an offset as one GUI element, inside [UiGraphics.fine].
     *
     * @param ui the graphics to draw with
     * @param dx the horizontal offset in screen pixels
     * @param dy the vertical offset in screen pixels
     */
    fun draw(ui: UiGraphics, dx: Int, dy: Int) = ui.fills(data, count, dx, dy)

    /**
     * Holds the storage layout.
     */
    private companion object {
        /**
         * The numbers stored per rectangle.
         */
        const val FIELDS: Int = 5

        /**
         * The number of rectangles room is made for at first.
         */
        const val INITIAL: Int = 64
    }
}

/**
 * The drawn shapes of a chart that do not change while the mouse moves, in screen pixels relative
 * to the chart's top-left corner, in two layers so that hover marks can be drawn between them.
 *
 * @property under the shapes drawn below the hover marks, such as the grid
 * @property over the shapes drawn above the hover marks, such as the series
 */
class ChartLayers(val under: FillList, val over: FillList)

/**
 * Everything the drawn shapes of a chart depend on besides its data, which a chart never
 * changes: its size, the pixel scale, the theme and the placement of its plot.
 *
 * @property width the width of the chart
 * @property height the height of the chart
 * @property scale the number of screen pixels per GUI pixel
 * @property tokens the design tokens
 * @property shape the numbers that place the plot relative to the chart, which follow the
 *           measured labels
 */
data class ChartRasterKey(val width: Int, val height: Int, val scale: Int, val tokens: ThemeTokens, val shape: List<Number>)
