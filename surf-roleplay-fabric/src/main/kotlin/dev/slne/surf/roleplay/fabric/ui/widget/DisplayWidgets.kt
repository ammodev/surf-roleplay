package dev.slne.surf.roleplay.fabric.ui.widget

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import dev.slne.surf.roleplay.fabric.ui.TextAlign
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.text.TextBlock
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeTokens
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.BadgeVariant
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.TextKind
import kotlin.math.ceil

/**
 * How one kind of text is drawn.
 *
 * @property scale the factor the font is drawn larger by
 * @property bold whether the text is bold
 * @property italic whether the text is italic
 * @property muted whether the text is drawn in the muted foreground colour
 * @property maxLines the largest number of lines shown, or `0` for no limit
 * @property align how the lines are placed, unless the node asks for another placement
 * @property padding the space around the text inside the widget
 */
data class TextStyle(
    val scale: Float = 1f,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val muted: Boolean = false,
    val maxLines: Int = 0,
    val align: TextAlign = TextAlign.START,
    val padding: Insets = Insets.NONE,
) {
    /**
     * Holds the style of every text kind.
     */
    companion object {
        /**
         * The width of the line in front of a blockquote.
         */
        const val QUOTE_LINE: Int = 2

        /**
         * Returns the style of a text kind.
         *
         * @param kind the kind
         * @return its style
         */
        fun of(kind: TextKind): TextStyle = when (kind) {
            TextKind.P, TextKind.SMALL, TextKind.ITEM_TITLE, TextKind.POPOVER_TITLE -> TextStyle()
            TextKind.H1 -> TextStyle(scale = 2f, bold = true)
            TextKind.H2 -> TextStyle(scale = 1.75f, bold = true, padding = Insets(bottom = 4))
            TextKind.H3 -> TextStyle(scale = 1.5f, bold = true)
            TextKind.H4 -> TextStyle(scale = 1.25f, bold = true)
            TextKind.LEAD -> TextStyle(scale = 1.25f, muted = true)
            TextKind.LARGE, TextKind.CARD_TITLE -> TextStyle(bold = true)
            TextKind.MUTED, TextKind.ALERT_DESCRIPTION, TextKind.CARD_DESCRIPTION, TextKind.POPOVER_DESCRIPTION -> TextStyle(muted = true)
            TextKind.BLOCKQUOTE -> TextStyle(italic = true, padding = Insets(left = 12))
            TextKind.INLINE_CODE -> TextStyle(padding = Insets(1, 3, 1, 3))
            TextKind.ALERT_TITLE -> TextStyle(maxLines = 1)
            TextKind.EMPTY_TITLE -> TextStyle(scale = 1.25f, align = TextAlign.CENTER)
            TextKind.EMPTY_DESCRIPTION -> TextStyle(muted = true, align = TextAlign.CENTER)
            TextKind.ITEM_DESCRIPTION -> TextStyle(muted = true, maxLines = 2)
        }

        /**
         * Wraps component JSON in a component that makes it bold or italic.
         *
         * @param json the component JSON
         * @param bold whether to make it bold
         * @param italic whether to make it italic
         * @return the wrapped JSON, or [json] itself if neither is asked for
         */
        fun styled(json: String, bold: Boolean, italic: Boolean): String {
            if (!bold && !italic) return json
            val inner = try {
                JsonParser.parseString(json)
            } catch (exception: RuntimeException) {
                JsonPrimitive(json)
            }
            val wrapper = JsonObject()
            wrapper.addProperty("text", "")
            if (bold) wrapper.addProperty("bold", true)
            if (italic) wrapper.addProperty("italic", true)
            wrapper.add("extra", JsonArray().apply { add(inner) })
            return wrapper.toString()
        }
    }
}

/**
 * A text drawn in the style of its kind, wrapped to the width it gets.
 *
 * @param id the id of the widget
 * @property kind the style of the text
 * @property text the text as component JSON
 * @property maxLines the largest number of lines shown, or `0` for the style's default
 * @property align the placement of the lines asked for by the node
 */
class TextWidget(id: String, val kind: TextKind, var text: String, val maxLines: Int = 0, val align: Align = Align.START) : Widget(id) {

    /**
     * The style of the text's kind.
     */
    val style: TextStyle = TextStyle.of(kind)

    /**
     * The colour a surrounding component draws the text in, or `null` for the style's colour.
     */
    var tint: Int? = null

    /**
     * The line limit in effect: the node's, or the style's default.
     */
    private val lineLimit: Int get() = if (maxLines > 0) maxLines else style.maxLines

    /**
     * The text with the style's bold and italic applied.
     */
    private val styledText: String get() = TextStyle.styled(text, style.bold, style.italic)

    /**
     * Returns the size of the text on as few lines as it needs.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = wrappedSize(measurer, FlexLayout.UNBOUNDED)

    /**
     * Creates a layout box whose text wraps to the width the widget gets.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        val word = ceil(measurer.longestWordWidth(styledText) * style.scale).toInt() + style.padding.left + style.padding.right
        return wrappingLayout(measurer, word) { wrappedSize(measurer, it) }
    }

    /**
     * Computes the size of the text wrapped to a width, with the style's scale and padding.
     *
     * @param measurer the text measurer
     * @param maxWidth the largest width of the widget
     * @return the size
     */
    private fun wrappedSize(measurer: TextMeasurer, maxWidth: Int): Size {
        val padding = style.padding
        val inner = if (maxWidth >= FlexLayout.UNBOUNDED) FlexLayout.UNBOUNDED else ((maxWidth - padding.left - padding.right) / style.scale).toInt().coerceAtLeast(1)
        val block = TextBlock.size(measurer, styledText, inner, lineLimit)
        return Size(
            ceil(block.width * style.scale).toInt() + padding.left + padding.right,
            ceil(block.height * style.scale).toInt() + padding.top + padding.bottom,
        )
    }

    /**
     * Draws the text with its kind's decoration: the line under a section heading, the line in
     * front of a quotation, or the background of inline code.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        when (kind) {
            TextKind.H2 -> ui.fill(Rect(bounds.x, bounds.bottom - 1, bounds.width, 1), tokens.border)
            TextKind.BLOCKQUOTE -> ui.fill(Rect(bounds.x, bounds.y, TextStyle.QUOTE_LINE, bounds.height), tokens.border)
            TextKind.INLINE_CODE -> ui.fillRounded(bounds, tokens.muted, 2)
            else -> Unit
        }
        val padding = style.padding
        val placement = when (align) {
            Align.CENTER -> TextAlign.CENTER
            Align.END -> TextAlign.END
            Align.START, Align.STRETCH -> style.align
        }
        val color = tint ?: if (style.muted) tokens.mutedForeground else tokens.foreground
        ui.wrappedText(
            styledText, bounds.x + padding.left, bounds.y + padding.top, bounds.width - padding.left - padding.right,
            color, lineLimit, placement, style.scale,
        )
    }

    /**
     * Replaces the text.
     *
     * @param json the new text as component JSON
     */
    override fun applyText(json: String) {
        text = json
    }
}

/**
 * A bulleted or numbered list whose items wrap beside their markers.
 *
 * @param id the id of the widget
 * @property items the items as component JSON
 * @property ordered whether the items are numbered
 */
class TextListWidget(id: String, val items: List<String>, val ordered: Boolean) : Widget(id) {

    /**
     * Returns the size of the items on as few lines as they need.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = wrappedSize(measurer, FlexLayout.UNBOUNDED)

    /**
     * Creates a layout box whose items wrap to the width the list gets.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox =
        wrappingLayout(measurer, INDENT + (items.maxOfOrNull { measurer.longestWordWidth(it) } ?: 0)) { wrappedSize(measurer, it) }

    /**
     * Computes the size of the list with its items wrapped beside the indent.
     *
     * @param measurer the text measurer
     * @param maxWidth the largest width of the list
     * @return the size
     */
    private fun wrappedSize(measurer: TextMeasurer, maxWidth: Int): Size {
        val inner = if (maxWidth >= FlexLayout.UNBOUNDED) FlexLayout.UNBOUNDED else (maxWidth - INDENT).coerceAtLeast(1)
        val blocks = items.map { TextBlock.size(measurer, it, inner) }
        val height = blocks.sumOf { it.height } + ITEM_GAP * (blocks.size - 1).coerceAtLeast(0)
        return Size(INDENT + (blocks.maxOfOrNull { it.width } ?: 0), height)
    }

    /**
     * Draws every item after its bullet or number.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val color = ui.tokens.foreground
        val width = (bounds.width - INDENT).coerceAtLeast(1)
        var y = bounds.y
        items.forEachIndexed { index, item ->
            val marker = if (ordered) "${index + 1}." else BULLET
            ui.plainText(marker, bounds.x + INDENT - MARKER_GAP - ui.plainWidth(marker), y, color)
            ui.wrappedText(item, bounds.x + INDENT, y, width, color)
            y += TextBlock.height(ui, ui.lineWidths(item, width).size) + ITEM_GAP
        }
    }

    /**
     * Holds the list metrics.
     */
    companion object {
        /**
         * The indent of the items, which holds their markers.
         */
        const val INDENT: Int = 14

        /**
         * The space between two items.
         */
        const val ITEM_GAP: Int = 4

        /**
         * The space between a marker and its item.
         */
        private const val MARKER_GAP: Int = 3

        /**
         * The marker of a bulleted item.
         */
        private const val BULLET: String = "•"
    }
}

/**
 * A one-pixel line in the border colour, stretched along its orientation.
 *
 * @param id the id of the widget
 * @property orientation whether the line runs horizontally or vertically
 */
class SeparatorWidget(id: String, val orientation: Orientation) : Widget(id) {

    /**
     * Returns one pixel across the orientation and nothing along it.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = if (orientation == Orientation.HORIZONTAL) Size(0, 1) else Size(1, 0)

    /**
     * Draws the line through the middle of the widget.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val line = if (orientation == Orientation.HORIZONTAL) {
            Rect(bounds.x, bounds.y + bounds.height / 2, bounds.width, 1)
        } else {
            Rect(bounds.x + bounds.width / 2, bounds.y, 1, bounds.height)
        }
        ui.fill(line, ui.tokens.border)
    }
}

/**
 * A keyboard key drawn as a small muted key cap. It never wraps.
 *
 * @param id the id of the widget
 * @property text the key as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 */
class KbdWidget(id: String, var text: String, val icon: String?) : Widget(id) {

    /**
     * Returns the size of the cap: at least square, and wide enough for the icon and text.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        val textWidth = measurer.width(text)
        return Size(maxOf(HEIGHT, iconSpace(icon, textWidth, ICON) + textWidth + 2 * PADDING), HEIGHT)
    }

    /**
     * Draws the cap with the icon and text centered in it.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        ui.fillRounded(bounds, tokens.muted, 2)
        drawIconAndText(ui, bounds, icon, ICON, text, tokens.mutedForeground)
    }

    /**
     * Replaces the key text.
     *
     * @param json the new text as component JSON
     */
    override fun applyText(json: String) {
        text = json
    }

    /**
     * Holds the key metrics.
     */
    companion object {
        /**
         * The height of a key, and its smallest width.
         */
        const val HEIGHT: Int = 12

        /**
         * The space left and right of the key text.
         */
        private const val PADDING: Int = 3

        /**
         * The size of a key icon.
         */
        private const val ICON: Int = 7
    }
}

/**
 * A small rounded label in one of the badge variants. It never wraps.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property variant the look of the badge
 */
class BadgeWidget(id: String, var text: String, val icon: String?, val variant: BadgeVariant) : Widget(id) {

    /**
     * Returns the size of the icon and text with the badge padding.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        val textWidth = measurer.width(text)
        return Size(iconSpace(icon, textWidth, ICON) + textWidth + 2 * PADDING, HEIGHT)
    }

    /**
     * Draws the pill in the variant's colours with the icon and text centered in it.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val colors = colors(ui.tokens, variant)
        val radius = bounds.height / 2
        colors.background?.let { ui.fillRounded(bounds, it, radius) }
        colors.border?.let { ui.borderRounded(bounds, it, radius) }
        drawIconAndText(ui, bounds, icon, ICON, text, colors.foreground)
    }

    /**
     * Replaces the text.
     *
     * @param json the new text as component JSON
     */
    override fun applyText(json: String) {
        text = json
    }

    /**
     * Holds the badge metrics and colours.
     */
    companion object {
        /**
         * The height of a badge.
         */
        const val HEIGHT: Int = 12

        /**
         * The space left and right of the badge content.
         */
        private const val PADDING: Int = 5

        /**
         * The size of a badge icon.
         */
        private const val ICON: Int = 7

        /**
         * The opacity of the destructive fill in dark themes.
         */
        private const val DARK_DESTRUCTIVE_ALPHA: Float = 0.6f

        /**
         * Returns the colours of a badge variant.
         *
         * @param tokens the theme tokens
         * @param variant the variant
         * @return the colours
         */
        fun colors(tokens: ThemeTokens, variant: BadgeVariant): ButtonStyle.Colors = when (variant) {
            BadgeVariant.DEFAULT -> ButtonStyle.Colors(tokens.primary, tokens.primaryForeground, null)
            BadgeVariant.SECONDARY -> ButtonStyle.Colors(tokens.secondary, tokens.secondaryForeground, null)
            BadgeVariant.DESTRUCTIVE -> ButtonStyle.Colors(
                if (ThemeColors.isDark(tokens.background)) ThemeColors.withAlpha(tokens.destructive, DARK_DESTRUCTIVE_ALPHA) else tokens.destructive,
                WHITE,
                null,
            )
            BadgeVariant.OUTLINE -> ButtonStyle.Colors(null, tokens.foreground, tokens.border)
            BadgeVariant.GHOST -> ButtonStyle.Colors(null, tokens.foreground, null)
            BadgeVariant.LINK -> ButtonStyle.Colors(null, tokens.primary, null)
        }

        /**
         * Opaque white, the text colour of destructive badges.
         */
        private const val WHITE: Int = -0x1
    }
}

/**
 * A horizontal row of keys and texts, centered on one line.
 *
 * @param id the id of the widget
 */
class KbdGroupWidget(id: String) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = 4
        crossAlign = Align.CENTER
    }
}

/**
 * Returns the space an icon of a size takes before a text.
 *
 * @param icon the icon name, or `null` for none
 * @param textWidth the width of the text after the icon
 * @param size the icon size
 * @return the icon size and gap, or nothing without an icon
 */
internal fun iconSpace(icon: String?, textWidth: Int, size: Int): Int = when {
    icon == null -> 0
    textWidth == 0 -> size
    else -> size + ICON_TEXT_GAP
}

/**
 * The space between a small icon and its text.
 */
private const val ICON_TEXT_GAP: Int = 3

/**
 * Draws an icon of a size and a text after it, centered together in an area.
 *
 * @param ui the graphics to draw with
 * @param area the area
 * @param icon the icon name, or `null` for none
 * @param size the icon size
 * @param text the text as component JSON
 * @param color the colour of icon and text
 */
internal fun drawIconAndText(ui: UiGraphics, area: Rect, icon: String?, size: Int, text: String, color: Int) {
    val textWidth = ui.width(text)
    val total = iconSpace(icon, textWidth, size) + textWidth
    var x = area.x + (area.width - total) / 2
    if (icon != null) {
        ui.icon(icon, Rect(x, area.y + (area.height - size) / 2, size, size), color)
        x += iconSpace(icon, textWidth, size)
    }
    ui.text(text, x, area.y + (area.height - ui.lineHeight + 1) / 2, color)
}
