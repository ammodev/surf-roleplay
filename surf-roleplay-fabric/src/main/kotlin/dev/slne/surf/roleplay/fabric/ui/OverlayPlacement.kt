package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.OverlaySide

/**
 * Places overlays next to the area they belong to.
 */
object OverlayPlacement {

    /**
     * The space kept between an overlay and the window edges.
     */
    const val WINDOW_MARGIN: Int = 4

    /**
     * The default space between an overlay and its anchor.
     */
    const val DEFAULT_OFFSET: Int = 4

    /**
     * Places an overlay on a side of an anchor. The overlay moves to the opposite side if it does
     * not fit on the asked side and fits better there, and it is then kept inside the window.
     *
     * @param anchor the area the overlay belongs to, such as its trigger
     * @param size the size of the overlay
     * @param window the window area
     * @param side the side of the anchor the overlay opens on
     * @param align how the overlay is aligned along that side: with the anchor's start, centre or
     *        end
     * @param offset the space between the anchor and the overlay
     * @return the overlay's area
     */
    fun place(anchor: Rect, size: Size, window: Rect, side: OverlaySide, align: Align, offset: Int = DEFAULT_OFFSET): Rect {
        val chosen = flipped(anchor, size, window, side, offset)
        val vertical = chosen == OverlaySide.TOP || chosen == OverlaySide.BOTTOM
        var x: Int
        var y: Int
        if (vertical) {
            x = aligned(anchor.x, anchor.width, size.width, align)
            y = if (chosen == OverlaySide.BOTTOM) anchor.bottom + offset else anchor.y - offset - size.height
        } else {
            y = aligned(anchor.y, anchor.height, size.height, align)
            x = if (chosen == OverlaySide.RIGHT) anchor.right + offset else anchor.x - offset - size.width
        }
        x = x.coerceIn(window.x + WINDOW_MARGIN, (window.right - WINDOW_MARGIN - size.width).coerceAtLeast(window.x + WINDOW_MARGIN))
        y = y.coerceIn(window.y + WINDOW_MARGIN, (window.bottom - WINDOW_MARGIN - size.height).coerceAtLeast(window.y + WINDOW_MARGIN))
        return Rect(x, y, size.width, size.height)
    }

    /**
     * Returns the side an overlay opens on: the asked side, or the opposite one if the overlay
     * does not fit on the asked side and the opposite side has more room.
     *
     * @param anchor the anchor
     * @param size the size of the overlay
     * @param window the window area
     * @param side the asked side
     * @param offset the space between anchor and overlay
     * @return the side
     */
    private fun flipped(anchor: Rect, size: Size, window: Rect, side: OverlaySide, offset: Int): OverlaySide {
        val room = { s: OverlaySide ->
            when (s) {
                OverlaySide.BOTTOM -> window.bottom - WINDOW_MARGIN - anchor.bottom - offset
                OverlaySide.TOP -> anchor.y - offset - window.y - WINDOW_MARGIN
                OverlaySide.RIGHT -> window.right - WINDOW_MARGIN - anchor.right - offset
                OverlaySide.LEFT -> anchor.x - offset - window.x - WINDOW_MARGIN
            }
        }
        val needed = if (side == OverlaySide.TOP || side == OverlaySide.BOTTOM) size.height else size.width
        val opposite = opposite(side)
        return if (room(side) < needed && room(opposite) > room(side)) opposite else side
    }

    /**
     * Returns the opposite side.
     *
     * @param side the side
     * @return the side across the anchor
     */
    fun opposite(side: OverlaySide): OverlaySide = when (side) {
        OverlaySide.BOTTOM -> OverlaySide.TOP
        OverlaySide.TOP -> OverlaySide.BOTTOM
        OverlaySide.RIGHT -> OverlaySide.LEFT
        OverlaySide.LEFT -> OverlaySide.RIGHT
    }

    /**
     * Aligns an overlay with an anchor along one axis.
     *
     * @param start the start of the anchor
     * @param length the length of the anchor
     * @param size the length of the overlay
     * @param align the alignment
     * @return the start of the overlay
     */
    private fun aligned(start: Int, length: Int, size: Int, align: Align): Int = when (align) {
        Align.START, Align.STRETCH -> start
        Align.CENTER -> start + (length - size) / 2
        Align.END -> start + length - size
    }
}
