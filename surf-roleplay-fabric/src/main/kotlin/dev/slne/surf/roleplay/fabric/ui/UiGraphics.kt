package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.text.ScreenText
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier

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
}

/**
 * A [TextMeasurer] backed by a Minecraft font.
 *
 * @property font the font
 */
class FontTextMeasurer(private val font: Font) : TextMeasurer {
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
}

/**
 * The drawing operations of roleplay widgets, on top of Minecraft's GUI graphics.
 *
 * @property graphics the Minecraft GUI graphics of the current frame
 * @property font the font texts are drawn with
 */
class UiGraphics(val graphics: GuiGraphicsExtractor, val font: Font) : TextMeasurer by FontTextMeasurer(font) {

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
     * a magenta placeholder.
     *
     * @param texture the texture identifier
     * @param rect the rectangle
     */
    fun image(texture: String, rect: Rect) {
        val id = Identifier.tryParse(texture)
        if (id == null) {
            fill(rect, MISSING_TEXTURE)
            return
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, id, rect.x, rect.y, 0f, 0f, rect.width, rect.height, rect.width, rect.height)
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
     * Starts a new drawing layer on top of everything drawn so far.
     */
    fun nextLayer() {
        graphics.nextStratum()
    }

    /**
     * Holds the placeholder color.
     */
    private companion object {
        /**
         * The color of a texture whose identifier cannot be parsed.
         */
        const val MISSING_TEXTURE: Int = 0xFFFF00FF.toInt()
    }
}
