package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import java.util.UUID

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

    /**
     * The title of a popover.
     */
    POPOVER_TITLE,

    /**
     * The description of a popover, muted.
     */
    POPOVER_DESCRIPTION,

    /**
     * The title of a dialog or alert dialog, large and bold.
     */
    DIALOG_TITLE,

    /**
     * The description of a dialog or alert dialog, muted.
     */
    DIALOG_DESCRIPTION,

    /**
     * The title of a sheet or drawer, bold.
     */
    SHEET_TITLE,

    /**
     * The description of a sheet or drawer, muted.
     */
    SHEET_DESCRIPTION,
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
 * The size of an avatar.
 */
enum class AvatarSize {
    /**
     * The regular size.
     */
    DEFAULT,

    /**
     * A small size.
     */
    SM,

    /**
     * A large size.
     */
    LG,
}

/**
 * Where the picture of an avatar comes from.
 */
sealed interface AvatarSource {
    /**
     * The face of a player's skin.
     *
     * @property playerId the UUID of the player
     */
    data class Player(val playerId: UUID) : AvatarSource

    /**
     * A texture from a resource pack.
     *
     * @property texture the identifier of the texture
     */
    data class Texture(val texture: Key) : AvatarSource
}

/**
 * A round picture of a person, with a fallback text while no picture is available.
 *
 * @property id the id of this element
 * @property fallback the text shown while no picture is available, such as initials
 * @property source where the picture comes from, or `null` to always show the fallback
 * @property size the size of the avatar
 * @property badge whether a small badge is drawn at the bottom right
 * @property badgeIcon the Lucide name of an icon in the badge, or `null` for a plain dot
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AvatarElement(
    override val id: String,
    val fallback: Component,
    val source: AvatarSource? = null,
    val size: AvatarSize = AvatarSize.DEFAULT,
    val badge: Boolean = false,
    val badgeIcon: String? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A row of overlapping avatars.
 *
 * @property id the id of this element
 * @property children the avatars and an optional count, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AvatarGroupElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The count of further people at the end of an avatar group.
 *
 * @property id the id of this element
 * @property text the count, such as `+3`
 * @property icon the Lucide name of an icon shown instead of the text, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AvatarGroupCountElement(
    override val id: String,
    val text: Component,
    val icon: String? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The look of an alert.
 */
enum class AlertVariant {
    /**
     * A neutral alert on the card colour.
     */
    DEFAULT,

    /**
     * An alert whose icon and texts are drawn in the destructive colour.
     */
    DESTRUCTIVE,
}

/**
 * A bordered callout with an optional icon beside its title and description.
 *
 * @property id the id of this element
 * @property children the title, description and further content
 * @property variant the look of the alert
 * @property icon the Lucide name of the icon, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AlertElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val variant: AlertVariant = AlertVariant.DEFAULT,
    val icon: String? = null,
    override val width: ElementSize = ElementSize.grow(),
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A bordered surface that stacks a header, content and footer.
 *
 * @property id the id of this element
 * @property children the header, content and footer
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CardElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The header of a card, with its title and description and an optional action at the top right.
 *
 * @property id the id of this element
 * @property children the title, description and action
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CardHeaderElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The action of a card header, placed at its top right.
 *
 * @property id the id of this element
 * @property children the content of the action
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CardActionElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a card.
 *
 * @property id the id of this element
 * @property children the content
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CardContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The footer of a card, a row centered on one line.
 *
 * @property id the id of this element
 * @property children the content
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CardFooterElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The look of the media of an empty state.
 */
enum class EmptyMediaVariant {
    /**
     * The content as it is, without a background.
     */
    DEFAULT,

    /**
     * An icon on a muted rounded square.
     */
    ICON,
}

/**
 * The look of an item.
 */
enum class ItemVariant {
    /**
     * Without a border or background.
     */
    DEFAULT,

    /**
     * With a border.
     */
    OUTLINE,

    /**
     * On a muted background.
     */
    MUTED,
}

/**
 * The size of an item.
 */
enum class ItemSize {
    /**
     * The regular padding and gaps.
     */
    DEFAULT,

    /**
     * Smaller padding and gaps.
     */
    SM,
}

/**
 * The look of the media of an item.
 */
enum class ItemMediaVariant {
    /**
     * The content as it is, without a background.
     */
    DEFAULT,

    /**
     * An icon on a small muted bordered square.
     */
    ICON,

    /**
     * The content clipped to a square, such as an image.
     */
    IMAGE,
}

/**
 * An empty state: a centered stack of a header and content that says there is nothing to show.
 *
 * @property id the id of this element
 * @property children the header and content, in order
 * @property outline whether a dashed border is drawn around the state
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class EmptyElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val outline: Boolean = false,
    override val width: ElementSize = ElementSize.grow(),
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The header of an empty state: its media, title and description, centered.
 *
 * @property id the id of this element
 * @property children the media, title and description, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class EmptyHeaderElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The media of an empty state, such as an icon or avatars.
 *
 * @property id the id of this element
 * @property children the content of the default variant
 * @property variant the look of the media
 * @property icon the Lucide name of the icon of the icon variant, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class EmptyMediaElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val variant: EmptyMediaVariant = EmptyMediaVariant.DEFAULT,
    val icon: String? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of an empty state, such as buttons, centered below its header.
 *
 * @property id the id of this element
 * @property children the content, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class EmptyContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A row of media, content and actions, with an optional header above and footer below; a clickable
 * item fires a widget action.
 *
 * @property id the id of this element
 * @property children the header, media, content, actions and footer
 * @property variant the look of the item
 * @property size the size of the item
 * @property onClick the handler run when the player clicks the item, or `null` for an item that
 *           cannot be clicked
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ItemElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val variant: ItemVariant = ItemVariant.DEFAULT,
    val size: ItemSize = ItemSize.DEFAULT,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The media at the start of an item, such as an icon, avatar or image.
 *
 * @property id the id of this element
 * @property children the content of the default and image variants
 * @property variant the look of the media
 * @property icon the Lucide name of the icon of the icon variant, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ItemMediaElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val variant: ItemMediaVariant = ItemMediaVariant.DEFAULT,
    val icon: String? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The stacked title and description of an item, which takes the space its media and actions leave.
 *
 * @property id the id of this element
 * @property children the title, description and further texts, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ItemContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The actions at the end of an item, such as buttons.
 *
 * @property id the id of this element
 * @property children the content, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ItemActionsElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A line above the row of an item; its first part is placed at the start and the others at the end.
 *
 * @property id the id of this element
 * @property children the content, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ItemHeaderElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A line below the row of an item; its first part is placed at the start and the others at the end.
 *
 * @property id the id of this element
 * @property children the content, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ItemFooterElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A stack of items and separators, each as wide as the group.
 *
 * @property id the id of this element
 * @property children the items and separators, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ItemGroupElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement
