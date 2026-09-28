package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component

/**
 * The style of a text: a typography style, or the title or description of a display component.
 */
enum class TextKind {
    /**
     * A paragraph of running text.
     */
    P,

    /**
     * The largest heading.
     */
    H1,

    /**
     * A section heading with a line below it.
     */
    H2,

    /**
     * A subsection heading.
     */
    H3,

    /**
     * The smallest heading.
     */
    H4,

    /**
     * A large muted introduction.
     */
    LEAD,

    /**
     * A large, bold text.
     */
    LARGE,

    /**
     * A small text.
     */
    SMALL,

    /**
     * A muted text.
     */
    MUTED,

    /**
     * A quotation, italic and indented behind a line.
     */
    BLOCKQUOTE,

    /**
     * A piece of code on a muted background.
     */
    INLINE_CODE,

    /**
     * The title of an alert, on one line.
     */
    ALERT_TITLE,

    /**
     * The description of an alert, muted.
     */
    ALERT_DESCRIPTION,

    /**
     * The title of a card, bold.
     */
    CARD_TITLE,

    /**
     * The description of a card, muted.
     */
    CARD_DESCRIPTION,

    /**
     * The title of an empty state, large and centered.
     */
    EMPTY_TITLE,

    /**
     * The description of an empty state, muted and centered.
     */
    EMPTY_DESCRIPTION,

    /**
     * The title of an item.
     */
    ITEM_TITLE,

    /**
     * The description of an item, muted and on at most two lines.
     */
    ITEM_DESCRIPTION,
}

/**
 * The look of a badge.
 */
enum class BadgeVariant {
    /**
     * Filled in the primary colour.
     */
    DEFAULT,

    /**
     * Filled in the secondary colour.
     */
    SECONDARY,

    /**
     * Filled in the destructive colour.
     */
    DESTRUCTIVE,

    /**
     * Bordered, without a fill.
     */
    OUTLINE,

    /**
     * Without a fill or border.
     */
    GHOST,

    /**
     * In the primary colour, like a link.
     */
    LINK,
}

/**
 * A text that wraps to the width it gets.
 *
 * @property id the id of this element
 * @property text the text
 * @property kind the style of the text
 * @property maxLines the largest number of lines shown, or `0` for the style's default
 * @property align how the lines are placed within the text's width
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TextElement(
    override val id: String,
    val text: Component,
    val kind: TextKind = TextKind.P,
    val maxLines: Int = 0,
    val align: Alignment = Alignment.START,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A bulleted or numbered list of texts.
 *
 * @property id the id of this element
 * @property items the items, in order
 * @property ordered whether the items are numbered instead of bulleted
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TextListElement(
    override val id: String,
    val items: List<Component>,
    val ordered: Boolean = false,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A one-pixel line that separates content.
 *
 * @property id the id of this element
 * @property orientation whether the line runs horizontally or vertically
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SeparatorElement(
    override val id: String,
    val orientation: Orientation = Orientation.HORIZONTAL,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A keyboard key, drawn as a small muted key cap.
 *
 * @property id the id of this element
 * @property text the key
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class KbdElement(
    override val id: String,
    val text: Component = Component.empty(),
    val icon: String? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A row of keyboard keys, such as a shortcut.
 *
 * @property id the id of this element
 * @property children the keys and texts, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class KbdGroupElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A small rounded label that marks something, such as a status.
 *
 * @property id the id of this element
 * @property text the text
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property variant the look of the badge
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class BadgeElement(
    override val id: String,
    val text: Component,
    val icon: String? = null,
    val variant: BadgeVariant = BadgeVariant.DEFAULT,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * Builds the children of a container element.
 *
 * @param children the builder of the children
 * @return the built children
 */
private fun build(children: ElementsBuilder.() -> Unit): List<ScreenElement> = ElementsBuilder().apply(children).elements.toList()

/**
 * Adds a text.
 *
 * @param id the id of the text
 * @param text the text
 * @param kind the style of the text
 * @param maxLines the largest number of lines shown, or `0` for the style's default
 * @param align how the lines are placed within the text's width
 * @param width how wide the text is laid out
 */
fun ElementsBuilder.text(
    id: String,
    text: Component,
    kind: TextKind = TextKind.P,
    maxLines: Int = 0,
    align: Alignment = Alignment.START,
    width: ElementSize = ElementSize.FIT,
) {
    elements += TextElement(id, text, kind, maxLines, align, width)
}

/**
 * Adds a bulleted or numbered list.
 *
 * @param id the id of the list
 * @param items the items, in order
 * @param ordered whether the items are numbered
 */
fun ElementsBuilder.textList(id: String, items: List<Component>, ordered: Boolean = false) {
    elements += TextListElement(id, items, ordered)
}

/**
 * Adds a separator that grows along its orientation.
 *
 * @param id the id of the separator
 * @param orientation whether the line runs horizontally or vertically
 */
fun ElementsBuilder.separator(id: String, orientation: Orientation = Orientation.HORIZONTAL) {
    elements += if (orientation == Orientation.HORIZONTAL) {
        SeparatorElement(id, orientation, width = ElementSize.grow())
    } else {
        SeparatorElement(id, orientation, height = ElementSize.grow())
    }
}

/**
 * Adds a keyboard key.
 *
 * @param id the id of the key
 * @param text the key
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 */
fun ElementsBuilder.kbd(id: String, text: Component = Component.empty(), icon: String? = null) {
    elements += KbdElement(id, text, icon)
}

/**
 * Adds a row of keyboard keys.
 *
 * @param id the id of the group
 * @param children the builder of the keys and texts
 */
fun ElementsBuilder.kbdGroup(id: String, children: ElementsBuilder.() -> Unit) {
    elements += KbdGroupElement(id, build(children))
}

/**
 * Adds a badge.
 *
 * @param id the id of the badge
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param variant the look of the badge
 */
fun ElementsBuilder.badge(id: String, text: Component, icon: String? = null, variant: BadgeVariant = BadgeVariant.DEFAULT) {
    elements += BadgeElement(id, text, icon, variant)
}

/**
 * A placeholder that pulses while content is loading.
 *
 * @property id the id of this element
 * @property round whether the placeholder is a circle or pill instead of a rounded rectangle
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SkeletonElement(
    override val id: String,
    val round: Boolean = false,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A turning loading indicator.
 *
 * @property id the id of this element
 * @property size the side length of the indicator when it fits its content
 * @property tint the theme colour the indicator is drawn in
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SpinnerElement(
    override val id: String,
    val size: Int = 10,
    val tint: IconTint = IconTint.FOREGROUND,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A box whose height follows its width in a fixed ratio, filled by its content.
 *
 * @property id the id of this element
 * @property ratio the width divided by the height
 * @property children the content, drawn over the whole box
 * @property width how wide this element is laid out
 * @property height ignored, since the ratio sets the height
 */
data class AspectRatioElement(
    override val id: String,
    val ratio: Float,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.grow(),
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * Adds a pulsing placeholder.
 *
 * @param id the id of the placeholder
 * @param width how wide the placeholder is laid out
 * @param height how tall the placeholder is laid out
 * @param round whether the placeholder is a circle or pill
 */
fun ElementsBuilder.skeleton(id: String, width: ElementSize, height: ElementSize, round: Boolean = false) {
    elements += SkeletonElement(id, round, width, height)
}

/**
 * Adds a loading indicator.
 *
 * @param id the id of the indicator
 * @param size the side length of the indicator
 * @param tint the theme colour the indicator is drawn in
 */
fun ElementsBuilder.spinner(id: String, size: Int = 10, tint: IconTint = IconTint.FOREGROUND) {
    elements += SpinnerElement(id, size, tint)
}

/**
 * Adds a box whose height follows its width.
 *
 * @param id the id of the box
 * @param ratio the width divided by the height
 * @param width how wide the box is laid out
 * @param children the builder of the content
 */
fun ElementsBuilder.aspectRatio(id: String, ratio: Float, width: ElementSize = ElementSize.grow(), children: ElementsBuilder.() -> Unit) {
    require(ratio > 0f) { "An aspect ratio must be positive, got $ratio" }
    elements += AspectRatioElement(id, ratio, build(children), width)
}
