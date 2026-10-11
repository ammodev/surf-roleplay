package dev.slne.surf.roleplay.fabric.interaction

import dev.slne.surf.roleplay.fabric.ui.layout.Rect

/**
 * Places the option list of the target eye next to the crosshair.
 */
object OptionListLayout {

    /**
     * The gap between the crosshair and the list.
     */
    const val GAP: Int = 12

    /**
     * Places the rows: right of the anchor, centred vertically on it, and left of it if the list
     * would leave the window.
     *
     * @param count the number of options
     * @param rowHeight the height of a row
     * @param width the width of the list
     * @param anchorX the crosshair x position
     * @param anchorY the crosshair y position
     * @param window the GUI window area
     * @return the row areas in option order
     */
    fun place(count: Int, rowHeight: Int, width: Int, anchorX: Int, anchorY: Int, window: Rect): List<Rect> {
        val height = count * rowHeight
        val x = if (anchorX + GAP + width <= window.x + window.width) anchorX + GAP else anchorX - GAP - width
        val top = (anchorY - height / 2).coerceIn(window.y, (window.y + window.height - height).coerceAtLeast(window.y))
        return List(count) { Rect(x, top + it * rowHeight, width, rowHeight) }
    }

    /**
     * Returns the row under a point.
     *
     * @param rows the row areas
     * @param x the x position
     * @param y the y position
     * @return the option index, or `null` if no row is under the point
     */
    fun indexAt(rows: List<Rect>, x: Double, y: Double): Int? = rows.indexOfFirst { it.contains(x, y) }.takeIf { it >= 0 }
}
