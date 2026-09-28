package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.icon.LucideIndex
import dev.slne.surf.roleplay.fabric.ui.text.ScreenText
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeTokens
import dev.slne.surf.roleplay.fabric.ui.text.TextBlock
import dev.slne.surf.roleplay.fabric.ui.text.TextWrap
import dev.slne.surf.roleplay.fabric.ui.widget.PlainText
import net.minecraft.client.gui.Font
import net.minecraft.locale.Language
import net.minecraft.network.chat.FormattedText
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier

/**
 * How the lines of a text are placed within its width.
 */
enum class TextAlign {
    /**
     * At the left edge.
     */
    START,

    /**
     * Centered.
     */
    CENTER,

    /**
     * At the right edge.
     */
    END,
}

/**
 * Measures texts for layout.
 */
interface TextMeasurer {
    /**
     * The height of one line of text, in GUI pixels.
     */
    val lineHeight: Int

    /**
     * Measures a text given as component JSON.
     *
     * @param json the component JSON
     * @return the width in GUI pixels
     */
    fun width(json: String): Int

    /**
     * Measures a plain string.
     *
     * @param text the string
     * @return the width in GUI pixels
     */
    fun plainWidth(text: String): Int

    /**
     * Measures the lines of a text given as component JSON wrapped to a width.
     *
     * @param json the component JSON
     * @param maxWidth the largest width of a line
     * @return the width of every line, at least one
     */
    fun lineWidths(json: String, maxWidth: Int): List<Int> =
        TextWrap.lines(PlainText.of(json), maxWidth, ::plainWidth).map(::plainWidth)
}

/**
 * A [TextMeasurer] backed by a Minecraft font.
 *
 * @property font the font
 */
class FontTextMeasurer(private val font: Font) : TextMeasurer {
    /**
     * The height of one line of the font.
     */
    override val lineHeight: Int get() = font.lineHeight

    /**
     * Measures a text given as component JSON with the font.
     *
     * @param json the component JSON
     * @return the width in GUI pixels
     */
    override fun width(json: String): Int = font.width(ScreenText.parse(json))

    /**
     * Measures a plain string with the font.
     *
     * @param text the string
     * @return the width in GUI pixels
     */
    override fun plainWidth(text: String): Int = font.width(text)

    /**
     * Measures the lines of a text wrapped to a width by the font.
     *
     * @param json the component JSON
     * @param maxWidth the largest width of a line
     * @return the width of every line, at least one
     */
    override fun lineWidths(json: String, maxWidth: Int): List<Int> =
        font.splitIgnoringLanguage(ScreenText.parse(json), maxWidth.coerceAtLeast(1)).map { font.width(it) }.ifEmpty { listOf(0) }
}

/**
 * The drawing operations of roleplay widgets, on top of Minecraft's GUI graphics.
 *
 * @property graphics the Minecraft GUI graphics of the current frame
 * @property font the font texts are drawn with
 * @property tokens the design tokens of the screen being drawn
 */
class UiGraphics(val graphics: GuiGraphicsExtractor, val font: Font, val tokens: ThemeTokens) : TextMeasurer by FontTextMeasurer(font) {

    /**
     * Fills a rectangle with rounded corners.
     *
     * @param rect the rectangle
     * @param color the ARGB colour
     * @param radius the corner radius
     * @param corners the corners that are rounded
     */
    fun fillRounded(rect: Rect, color: Int, radius: Int = tokens.radius, corners: Corners = Corners.ALL) {
        RoundedShape.spans(rect, radius, corners).forEach { graphics.fill(it.x0, it.y, it.x1, it.y + 1, color) }
    }

    /**
     * Draws a one-pixel border with rounded corners along the inside of a rectangle.
     *
     * @param rect the rectangle
     * @param color the ARGB colour
     * @param radius the corner radius
     * @param corners the corners that are rounded
     */
    fun borderRounded(rect: Rect, color: Int, radius: Int = tokens.radius, corners: Corners = Corners.ALL) {
        RoundedShape.borderSpans(rect, radius, corners).forEach { graphics.fill(it.x0, it.y, it.x1, it.y + 1, color) }
    }

    /**
     * Returns a colour at the opacity used for disabled widgets.
     *
     * @param color the colour of the enabled widget
     * @return the dimmed colour
     */
    fun disabled(color: Int): Int = ThemeColors.withAlpha(color, ((color ushr 24) / 255f) * 0.5f)

    /**
     * Fills a rectangle.
     *
     * @param rect the rectangle
     * @param color the ARGB color
     */
    fun fill(rect: Rect, color: Int) {
        if (rect.width <= 0 || rect.height <= 0) return
        graphics.fill(rect.x, rect.y, rect.right, rect.bottom, color)
    }

    /**
     * Draws a one-pixel border along the inside of a rectangle.
     *
     * @param rect the rectangle
     * @param color the ARGB color
     */
    fun border(rect: Rect, color: Int) {
        if (rect.width <= 0 || rect.height <= 0) return
        graphics.outline(rect.x, rect.y, rect.width, rect.height, color)
    }

    /**
     * Draws a text given as component JSON with its top-left corner at a point.
     *
     * @param json the component JSON
     * @param x the left edge
     * @param y the top edge
     * @param color the ARGB color of unstyled parts
     */
    fun text(json: String, x: Int, y: Int, color: Int) {
        graphics.text(font, ScreenText.parse(json), x, y, color, false)
    }

    /**
     * Draws a text given as component JSON wrapped to a width, line by line from a top edge. With
     * a line limit, the last shown line of a longer text ends with an ellipsis.
     *
     * @param json the component JSON
     * @param x the left edge
     * @param y the top edge
     * @param maxWidth the largest width of a line
     * @param color the ARGB color of unstyled parts
     * @param maxLines the largest number of lines drawn, or `0` for no limit
     * @param align how each line is placed within the width
     * @param scale the factor the font is drawn larger by; the text wraps at the width divided by
     *        it
     */
    fun wrappedText(json: String, x: Int, y: Int, maxWidth: Int, color: Int, maxLines: Int = 0, align: TextAlign = TextAlign.START, scale: Float = 1f) {
        if (scale != 1f) {
            val pose = graphics.pose()
            pose.pushMatrix()
            pose.translate(x.toFloat(), y.toFloat())
            pose.scale(scale, scale)
            try {
                wrappedText(json, 0, 0, (maxWidth / scale).toInt(), color, maxLines, align)
            } finally {
                pose.popMatrix()
            }
            return
        }
        val width = maxWidth.coerceAtLeast(1)
        val lines = font.splitIgnoringLanguage(ScreenText.parse(json), width)
        val clamped = maxLines > 0 && lines.size > maxLines
        val shown = if (clamped) lines.take(maxLines) else lines
        shown.forEachIndexed { index, line ->
            val text = if (clamped && index == shown.lastIndex) ellipsized(line, width) else line
            val sequence = Language.getInstance().getVisualOrder(text)
            val lineWidth = font.width(sequence)
            val lineX = when (align) {
                TextAlign.START -> x
                TextAlign.CENTER -> x + (width - lineWidth) / 2
                TextAlign.END -> x + width - lineWidth
            }
            graphics.text(font, sequence, lineX, y + index * (lineHeight + TextBlock.LINE_GAP), color, false)
        }
    }

    /**
     * Shortens a line so that it fits a width together with an ellipsis, and appends the
     * ellipsis.
     *
     * @param line the line
     * @param width the width
     * @return the shortened line with the ellipsis
     */
    private fun ellipsized(line: FormattedText, width: Int): FormattedText =
        FormattedText.composite(font.substrByWidth(line, (width - font.width(ELLIPSIS)).coerceAtLeast(0)), FormattedText.of(ELLIPSIS))

    /**
     * Draws a text given as component JSON centered in a rectangle.
     *
     * @param json the component JSON
     * @param rect the rectangle
     * @param color the ARGB color of unstyled parts
     */
    fun centeredText(json: String, rect: Rect, color: Int) {
        text(json, rect.x + (rect.width - width(json)) / 2, rect.y + (rect.height - lineHeight + 1) / 2, color)
    }

    /**
     * Draws a plain string with its top-left corner at a point.
     *
     * @param text the string
     * @param x the left edge
     * @param y the top edge
     * @param color the ARGB color
     */
    fun plainText(text: String, x: Int, y: Int, color: Int) {
        graphics.text(font, text, x, y, color, false)
    }

    /**
     * Draws a texture stretched over a rectangle. An identifier that cannot be parsed is drawn as
     * a placeholder in the destructive colour.
     *
     * @param texture the texture identifier
     * @param rect the rectangle
     */
    fun image(texture: String, rect: Rect) {
        val id = Identifier.tryParse(texture)
        if (id == null) {
            fill(rect, tokens.destructive)
            return
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, id, rect.x, rect.y, 0f, 0f, rect.width, rect.height, rect.width, rect.height)
    }

    /**
     * Draws a Lucide icon tinted with a colour, stretched over a square. An unknown icon name is
     * drawn as a crossed-out square.
     *
     * @param name the Lucide name of the icon
     * @param rect the area to draw the icon in
     * @param color the ARGB tint
     */
    fun icon(name: String, rect: Rect, color: Int) {
        val index = LucideIndex.bundled
        val cell = index.find(name)
        if (cell == null) {
            border(rect, color)
            for (step in 0 until minOf(rect.width, rect.height)) graphics.fill(rect.x + step, rect.y + step, rect.x + step + 1, rect.y + step + 1, color)
            return
        }
        graphics.blit(
            RenderPipelines.GUI_TEXTURED, LucideIndex.ATLAS, rect.x, rect.y, cell.u, cell.v,
            rect.width, rect.height, index.cell, index.cell, index.width, index.height, color,
        )
    }

    /**
     * Draws a Lucide icon tinted with a colour, turned around the centre of its square.
     *
     * @param name the Lucide name of the icon
     * @param rect the area to draw the icon in
     * @param color the ARGB tint
     * @param degrees the angle in degrees clockwise
     */
    fun rotatedIcon(name: String, rect: Rect, color: Int, degrees: Float) {
        val pose = graphics.pose()
        pose.pushMatrix()
        try {
            pose.translate(rect.x + rect.width / 2f, rect.y + rect.height / 2f)
            pose.rotate(Math.toRadians(degrees.toDouble()).toFloat())
            pose.translate(-rect.width / 2f, -rect.height / 2f)
            icon(name, Rect(0, 0, rect.width, rect.height), color)
        } finally {
            pose.popMatrix()
        }
    }

    /**
     * Runs drawing code that is clipped to a rectangle.
     *
     * @param rect the rectangle to clip to
     * @param block the drawing code
     */
    fun clipped(rect: Rect, block: () -> Unit) {
        graphics.enableScissor(rect.x, rect.y, rect.right, rect.bottom)
        try {
            block()
        } finally {
            graphics.disableScissor()
        }
    }

    /**
     * Runs drawing code that is clipped to a rectangle with rounded corners, one row at a time.
     *
     * @param rect the rectangle to clip to
     * @param radius the corner radius; half the side of a square clips to a circle
     * @param block the drawing code, run once for every row
     */
    fun clippedRound(rect: Rect, radius: Int, block: () -> Unit) {
        RoundedShape.spans(rect, radius).forEach { span -> clipped(Rect(span.x0, span.y, span.x1 - span.x0, 1), block) }
    }

    /**
     * Starts a new drawing layer on top of everything drawn so far.
     */
    fun nextLayer() {
        graphics.nextStratum()
    }

    /**
     * Holds the ellipsis of clamped texts.
     */
    private companion object {
        /**
         * The ellipsis that ends a clamped text.
         */
        const val ELLIPSIS: String = "…"
    }

}
