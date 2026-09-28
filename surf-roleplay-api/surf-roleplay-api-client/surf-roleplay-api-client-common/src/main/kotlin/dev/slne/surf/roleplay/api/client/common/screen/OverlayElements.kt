package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component

/**
 * The side of its trigger an overlay opens on.
 */
enum class OverlaySide {
    /**
     * Below the trigger.
     */
    BOTTOM,

    /**
     * Above the trigger.
     */
    TOP,

    /**
     * To the right of the trigger.
     */
    RIGHT,

    /**
     * To the left of the trigger.
     */
    LEFT,
}

/**
 * A popover: its triggers, and a content that opens next to them when a trigger is clicked.
 *
 * @property id the id of this element
 * @property children the triggers and one popover content
 * @property open whether the popover is open
 * @property side the side of the triggers the content opens on
 * @property align how the content is aligned along that side
 * @property onChange the handler run whenever the player opens or closes the overlay, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PopoverElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val side: OverlaySide = OverlaySide.BOTTOM,
    val align: Alignment = Alignment.CENTER,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a popover, on a bordered surface.
 *
 * @property id the id of this element
 * @property children the content, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PopoverContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The header of a popover content: its title and description, stacked.
 *
 * @property id the id of this element
 * @property children the title and description
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PopoverHeaderElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * Adds the header of a popover content: its title and description, stacked.
 *
 * @param id the id of the header
 * @param children the builder of the title and description
 */
fun ElementsBuilder.popoverHeader(id: String, children: ElementsBuilder.() -> Unit) {
    elements += PopoverHeaderElement(id, build(children))
}

/**
 * A hover card: its triggers, and a content that opens next to them while the mouse rests on them.
 *
 * @property id the id of this element
 * @property children the triggers and one hover card content
 * @property open whether the hover card is open
 * @property side the side of the triggers the content opens on
 * @property align how the content is aligned along that side
 * @property openDelay how long the mouse must rest on a trigger before the content opens, in
 *           milliseconds
 * @property closeDelay how long after the mouse left the triggers and the content it closes, in
 *           milliseconds
 * @property onChange the handler run whenever the player opens or closes the overlay, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class HoverCardElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val side: OverlaySide = OverlaySide.BOTTOM,
    val align: Alignment = Alignment.CENTER,
    val openDelay: Int = 700,
    val closeDelay: Int = 300,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a hover card, on a bordered surface.
 *
 * @property id the id of this element
 * @property children the content, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class HoverCardContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A tooltip: its triggers, and a short text shown next to them while a trigger is hovered or
 * focused.
 *
 * @property id the id of this element
 * @property children the triggers
 * @property text the text as component JSON
 * @property side the side of the triggers the text is shown on
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TooltipElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val text: Component = Component.empty(),
    val side: OverlaySide = OverlaySide.TOP,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * Builds the children of a container element.
 *
 * @param children the builder of the children
 * @return the built children
 */
private fun build(children: ElementsBuilder.() -> Unit): List<ScreenElement> = ElementsBuilder().apply(children).elements.toList()

/**
 * Adds a popover. Its children are its triggers, which open the popover when clicked instead of
 * firing their own actions, and one [popoverContent].
 *
 * @param id the id of the popover
 * @param side the side of the triggers the content opens on
 * @param align how the content is aligned along that side
 * @param open whether the popover starts open
 * @param onChange the handler run whenever the player opens or closes the popover, or `null` for
 *        none
 * @param children the builder of the triggers and the content
 */
fun ElementsBuilder.popover(
    id: String,
    side: OverlaySide = OverlaySide.BOTTOM,
    align: Alignment = Alignment.CENTER,
    open: Boolean = false,
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    elements += PopoverElement(id, build(children), open, side, align, onChange)
}

/**
 * Adds the content of a popover.
 *
 * @param id the id of the content
 * @param width how wide the content is laid out
 * @param children the builder of the content
 */
fun ElementsBuilder.popoverContent(id: String, width: ElementSize = ElementSize.fixed(POPOVER_WIDTH), children: ElementsBuilder.() -> Unit) {
    elements += PopoverContentElement(id, build(children), width)
}

/**
 * Adds the title of a popover.
 *
 * @param id the id of the title
 * @param text the title
 */
fun ElementsBuilder.popoverTitle(id: String, text: Component) {
    elements += TextElement(id, text, TextKind.POPOVER_TITLE)
}

/**
 * Adds the description of a popover.
 *
 * @param id the id of the description
 * @param text the description
 */
fun ElementsBuilder.popoverDescription(id: String, text: Component) {
    elements += TextElement(id, text, TextKind.POPOVER_DESCRIPTION)
}

/**
 * Adds a hover card. Its children are its triggers and one [hoverCardContent], which opens while
 * the mouse rests on a trigger.
 *
 * @param id the id of the hover card
 * @param side the side of the triggers the content opens on
 * @param align how the content is aligned along that side
 * @param openDelay how long the mouse must rest on a trigger before the content opens, in
 *        milliseconds
 * @param closeDelay how long after the mouse left the triggers and the content it closes, in
 *        milliseconds
 * @param onChange the handler run whenever the hover card opens or closes, or `null` for none
 * @param children the builder of the triggers and the content
 */
fun ElementsBuilder.hoverCard(
    id: String,
    side: OverlaySide = OverlaySide.BOTTOM,
    align: Alignment = Alignment.CENTER,
    openDelay: Int = 700,
    closeDelay: Int = 300,
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    require(openDelay >= 0 && closeDelay >= 0) { "Hover card delays must not be negative" }
    elements += HoverCardElement(id, build(children), false, side, align, openDelay, closeDelay, onChange)
}

/**
 * Adds the content of a hover card.
 *
 * @param id the id of the content
 * @param width how wide the content is laid out
 * @param children the builder of the content
 */
fun ElementsBuilder.hoverCardContent(id: String, width: ElementSize = ElementSize.fixed(HOVER_CARD_WIDTH), children: ElementsBuilder.() -> Unit) {
    elements += HoverCardContentElement(id, build(children), width)
}

/**
 * Adds a tooltip around triggers.
 *
 * @param id the id of the tooltip
 * @param text the text shown while a trigger is hovered or focused
 * @param side the side of the triggers the text is shown on
 * @param children the builder of the triggers
 */
fun ElementsBuilder.tooltip(id: String, text: Component, side: OverlaySide = OverlaySide.TOP, children: ElementsBuilder.() -> Unit) {
    elements += TooltipElement(id, build(children), text, side)
}

/**
 * The default width of popover contents, in GUI pixels.
 */
const val POPOVER_WIDTH: Int = 144

/**
 * The default width of hover card contents, in GUI pixels.
 */
const val HOVER_CARD_WIDTH: Int = 128
