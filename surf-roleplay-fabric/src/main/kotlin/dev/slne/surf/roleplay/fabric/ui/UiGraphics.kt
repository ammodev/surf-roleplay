package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.icon.LucideIndex
import dev.slne.surf.roleplay.fabric.ui.text.ScreenText
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeTokens
import dev.slne.surf.roleplay.fabric.ui.text.TextBlock
import dev.slne.surf.roleplay.fabric.ui.text.TextWrap
import dev.slne.surf.roleplay.fabric.ui.widget.PlainText
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.locale.Language
import net.minecraft.network.chat.FormattedText
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import dev.slne.surf.roleplay.fabric.mixin.GuiGraphicsExtractorAccessor
import org.joml.Matrix3x2f
import kotlin.math.roundToInt

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

    /**
     * Measures the widest word of a text given as component JSON, which is the narrowest width
     * the text can wrap to without breaking a word.
     *
     * @param json the component JSON
     * @return the width of the widest word
     */
    fun longestWordWidth(json: String): Int =
        PlainText.of(json).split(' ', '\n').maxOfOrNull(::plainWidth) ?: 0
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

    /**
     * Measures the widest word of a text with the font, in the text's outer style.
     *
     * @param json the component JSON
     * @return the width of the widest word
     */
    override fun longestWordWidth(json: String): Int {
        val component = ScreenText.parse(json)
        val style = component.style
        return component.string.split(' ', '\n').maxOfOrNull { font.width(FormattedText.of(it, style)) } ?: 0
    }
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
     * Fills shapes as single GUI elements with the current pose and scissor, and counts them.
     */
    internal val shapes: ShapeFills = ShapeFills { data, count -> fills(data, count, 0, 0) }

    /**
     * Fills a rectangle with rounded corners.
     *
     * @param rect the rectangle
     * @param color the ARGB colour
     * @param radius the corner radius
     * @param corners the corners that are rounded
     */
    fun fillRounded(rect: Rect, color: Int, radius: Int = tokens.radius, corners: Corners = Corners.ALL) {
        shapes.rounded(rect, color, radius, corners)
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
        shapes.roundedBorder(rect, color, radius, corners)
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
            fillRects(List(minOf(rect.width, rect.height)) { step -> Rect(rect.x + step, rect.y + step, 1, 1) }, color)
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
     * The clips in effect, which culling reads.
     */
    private val clips = ClipStack()

    /**
     * The area drawing is currently clipped to, the overlap of every clip in effect, or `null`
     * while nothing clips the drawing. Inside [clippedRound] it is the whole rounded rectangle.
     */
    val clip: Rect? get() = clips.current

    /**
     * Runs drawing code that is clipped to a rectangle, within any clip already in effect.
     *
     * @param rect the rectangle to clip to
     * @param block the drawing code
     */
    fun clipped(rect: Rect, block: () -> Unit) {
        clips.push(rect) { scissored(rect, block) }
    }

    /**
     * Runs drawing code that is clipped to a rectangle with rounded corners, once for every run
     * of rows of the rectangle with the same columns that lies within the clip already in effect.
     *
     * @param rect the rectangle to clip to
     * @param radius the corner radius; half the side of a square clips to a circle
     * @param block the drawing code, run once for every run of rows
     */
    fun clippedRound(rect: Rect, radius: Int, block: () -> Unit) {
        clips.roundedRows(rect, radius) { row -> scissored(row, block) }
    }

    /**
     * Runs drawing code with the GPU scissor set to a rectangle within the scissor already in
     * effect, without changing [clip].
     *
     * @param rect the rectangle
     * @param block the drawing code
     */
    private fun scissored(rect: Rect, block: () -> Unit) {
        graphics.enableScissor(rect.x, rect.y, rect.right, rect.bottom)
        try {
            block()
        } finally {
            graphics.disableScissor()
        }
    }

    /**
     * Draws a dashed one-pixel border along the inside of a rectangle.
     *
     * @param rect the rectangle
     * @param color the ARGB colour
     * @param dash the length of a dash
     * @param space the length of the space between two dashes
     */
    fun dashedBorder(rect: Rect, color: Int, dash: Int = 3, space: Int = 2) {
        shapes.dashedBorder(rect, color, dash, space)
    }

    /**
     * Fills rectangles in one colour as one GUI element, in order, which looks the same as
     * filling them one by one.
     *
     * @param rects the rectangles
     * @param color the ARGB colour
     */
    fun fillRects(rects: List<Rect>, color: Int) {
        shapes.rects(rects, color)
    }

    /**
     * The number of screen pixels per GUI pixel, at least 1.
     */
    val pixelScale: Int
        get() = runCatching {
            val window = Minecraft.getInstance().window
            (window.width.toDouble() / window.guiScaledWidth).roundToInt().coerceAtLeast(1)
        }.getOrDefault(1)

    /**
     * Runs drawing code at screen-pixel resolution, for shapes such as chart lines that would be
     * coarse on whole GUI pixels. The code gets the scale and draws with [fineFill].
     *
     * @param block the drawing code, given the number of screen pixels per GUI pixel
     */
    fun fine(block: (Int) -> Unit) {
        val scale = pixelScale
        val pose = graphics.pose()
        pose.pushMatrix()
        try {
            pose.scale(1f / scale, 1f / scale)
            block(scale)
        } finally {
            pose.popMatrix()
        }
    }

    /**
     * Fills a rectangle given in screen pixels, inside [fine].
     *
     * @param x0 the left edge
     * @param y0 the top edge
     * @param x1 the right edge, exclusive
     * @param y1 the bottom edge, exclusive
     * @param color the ARGB colour
     */
    fun fineFill(x0: Int, y0: Int, x1: Int, y1: Int, color: Int) {
        if (x1 <= x0 || y1 <= y0) return
        graphics.fill(x0, y0, x1, y1, color)
    }

    /**
     * Fills many rectangles as one GUI element, with the current pose and scissor, which looks
     * the same as filling them one by one in order.
     *
     * @param data the rectangles, five numbers each: left, top, right, bottom and the ARGB colour
     * @param count the number of rectangles in [data]
     * @param dx the horizontal offset added to every rectangle
     * @param dy the vertical offset added to every rectangle
     */
    fun fills(data: IntArray, count: Int, dx: Int, dy: Int) {
        if (count <= 0) return
        val access = graphics as GuiGraphicsExtractorAccessor
        val scissor = access.`surfRoleplay$scissorStack`().peek()
        access.`surfRoleplay$guiRenderState`().addGuiElement(FillBatchRenderState(data, count, dx, dy, Matrix3x2f(graphics.pose()), scissor))
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

/**
 * The clips in effect while drawing, as the overlap of every clip pushed so far.
 */
class ClipStack {
    /**
     * The overlap of every clip in effect, or `null` while none is.
     */
    var current: Rect? = null
        private set

    /**
     * Runs code with a rectangle added to the clips in effect.
     *
     * @param rect the rectangle
     * @param block the code
     */
    fun push(rect: Rect, block: () -> Unit) {
        val outer = current
        current = outer?.intersection(rect) ?: rect
        try {
            block()
        } finally {
            current = outer
        }
    }

    /**
     * Runs code once for every run of rows of a rectangle with rounded corners that lies within
     * the clips in effect, where a run joins consecutive rows that start and end at the same
     * columns. Throughout, the current clip is the whole rectangle within the outer clips, so
     * that what is culled does not depend on the run.
     *
     * @param rect the rectangle
     * @param radius the corner radius
     * @param row the code, given the run of rows
     */
    fun roundedRows(rect: Rect, radius: Int, row: (Rect) -> Unit) {
        val outer = current
        push(rect) {
            RoundedShape.merge(RoundedShape.spans(rect, radius)).forEach { run ->
                if (outer == null || run.intersects(outer)) row(run)
            }
        }
    }
}
