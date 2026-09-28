package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.text.TextBlock
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.OverlaySide

/**
 * A tooltip asked for during one frame.
 *
 * @property json the text as component JSON
 * @property anchor the area the tooltip belongs to
 * @property side the side of the anchor the tooltip is shown on
 */
data class TooltipRequest(val json: String, val anchor: Rect, val side: OverlaySide)

/**
 * Draws tooltips: a text in the background colour on a rounded box in the foreground colour, with
 * an arrow that points at the anchor.
 */
object TooltipPainter {

    /**
     * The space left and right of the text.
     */
    const val PADDING_X: Int = 6

    /**
     * The space above and below the text.
     */
    const val PADDING_Y: Int = 3

    /**
     * The widest a tooltip's text may be before it wraps.
     */
    const val MAX_TEXT_WIDTH: Int = 160

    /**
     * The length of the arrow from the box to its tip.
     */
    const val ARROW: Int = 3

    /**
     * Computes the size of a tooltip's box.
     *
     * @param measurer the text measurer
     * @param json the text as component JSON
     * @return the size
     */
    fun size(measurer: TextMeasurer, json: String): Size {
        val block = TextBlock.size(measurer, json, MAX_TEXT_WIDTH)
        return Size(block.width + 2 * PADDING_X, block.height + 2 * PADDING_Y)
    }

    /**
     * Computes the area of a tooltip's box next to its anchor.
     *
     * @param measurer the text measurer
     * @param request the tooltip
     * @param window the window area
     * @return the box area
     */
    fun area(measurer: TextMeasurer, request: TooltipRequest, window: Rect): Rect =
        OverlayPlacement.place(request.anchor, size(measurer, request.json), window, request.side, Align.CENTER, ARROW + 1)

    /**
     * Draws a tooltip with its arrow.
     *
     * @param ui the graphics to draw with
     * @param request the tooltip
     * @param window the window area
     */
    fun draw(ui: UiGraphics, request: TooltipRequest, window: Rect) {
        val tokens = ui.tokens
        val box = area(ui, request, window)
        ui.fillRounded(box, tokens.foreground)
        drawArrow(ui, box, request.anchor, tokens.foreground)
        ui.wrappedText(request.json, box.x + PADDING_X, box.y + PADDING_Y, box.width - 2 * PADDING_X, tokens.background)
    }

    /**
     * Draws the arrow on the side of the box that faces the anchor.
     *
     * @param ui the graphics to draw with
     * @param box the tooltip box
     * @param anchor the anchor
     * @param color the colour of the arrow
     */
    private fun drawArrow(ui: UiGraphics, box: Rect, anchor: Rect, color: Int) {
        val below = box.y >= anchor.bottom
        val above = box.bottom <= anchor.y
        val centerX = (anchor.x + anchor.width / 2).coerceIn(box.x + ARROW + 1, box.right - ARROW - 1)
        val centerY = (anchor.y + anchor.height / 2).coerceIn(box.y + ARROW + 1, box.bottom - ARROW - 1)
        for (step in 0 until ARROW) {
            val half = ARROW - step
            when {
                below -> ui.fill(Rect(centerX - half, box.y - step - 1, 2 * half, 1), color)
                above -> ui.fill(Rect(centerX - half, box.bottom + step, 2 * half, 1), color)
                box.x >= anchor.right -> ui.fill(Rect(box.x - step - 1, centerY - half, 1, 2 * half), color)
                else -> ui.fill(Rect(box.right + step, centerY - half, 1, 2 * half), color)
            }
        }
    }
}
