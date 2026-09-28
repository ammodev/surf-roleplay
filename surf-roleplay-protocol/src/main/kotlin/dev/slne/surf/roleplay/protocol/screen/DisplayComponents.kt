package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The style of a text: a typography style, or the title or description of a display component.
 */
@Serializable
enum class TextKind {
    /**
     * A paragraph of running text.
     */
    @ProtoNumber(0)
    P,

    /**
     * The largest heading.
     */
    @ProtoNumber(1)
    H1,

    /**
     * A section heading with a line below it.
     */
    @ProtoNumber(2)
    H2,

    /**
     * A subsection heading.
     */
    @ProtoNumber(3)
    H3,

    /**
     * The smallest heading.
     */
    @ProtoNumber(4)
    H4,

    /**
     * A large muted introduction.
     */
    @ProtoNumber(5)
    LEAD,

    /**
     * A large, bold text.
     */
    @ProtoNumber(6)
    LARGE,

    /**
     * A small text.
     */
    @ProtoNumber(7)
    SMALL,

    /**
     * A muted text.
     */
    @ProtoNumber(8)
    MUTED,

    /**
     * A quotation, italic and indented behind a line.
     */
    @ProtoNumber(9)
    BLOCKQUOTE,

    /**
     * A piece of code on a muted background.
     */
    @ProtoNumber(10)
    INLINE_CODE,

    /**
     * The title of an alert, on one line.
     */
    @ProtoNumber(11)
    ALERT_TITLE,

    /**
     * The description of an alert, muted.
     */
    @ProtoNumber(12)
    ALERT_DESCRIPTION,

    /**
     * The title of a card, bold.
     */
    @ProtoNumber(13)
    CARD_TITLE,

    /**
     * The description of a card, muted.
     */
    @ProtoNumber(14)
    CARD_DESCRIPTION,

    /**
     * The title of an empty state, large and centered.
     */
    @ProtoNumber(15)
    EMPTY_TITLE,

    /**
     * The description of an empty state, muted and centered.
     */
    @ProtoNumber(16)
    EMPTY_DESCRIPTION,

    /**
     * The title of an item.
     */
    @ProtoNumber(17)
    ITEM_TITLE,

    /**
     * The description of an item, muted and on at most two lines.
     */
    @ProtoNumber(18)
    ITEM_DESCRIPTION,
}

/**
 * The look of a badge.
 */
@Serializable
enum class BadgeVariant {
    /**
     * Filled in the primary colour.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * Filled in the secondary colour.
     */
    @ProtoNumber(1)
    SECONDARY,

    /**
     * Filled in the destructive colour.
     */
    @ProtoNumber(2)
    DESTRUCTIVE,

    /**
     * Bordered, without a fill.
     */
    @ProtoNumber(3)
    OUTLINE,

    /**
     * Without a fill or border.
     */
    @ProtoNumber(4)
    GHOST,

    /**
     * In the primary colour, like a link.
     */
    @ProtoNumber(5)
    LINK,
}

/**
 * A text that wraps to the width it gets.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property kind the style of the text
 * @property text the text as component JSON
 * @property maxLines the largest number of lines shown, or `0` for the style's default
 * @property align how the lines are placed within the text's width; [Align.STRETCH] places them
 *           at the start
 */
@Serializable
@SerialName("text")
data class TextNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val kind: TextKind = TextKind.P,
    @ProtoNumber(5) val text: String = "",
    @ProtoNumber(6) val maxLines: Int = 0,
    @ProtoNumber(7) val align: Align = Align.START,
) : ScreenNode

/**
 * A bulleted or numbered list of texts.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property items the items as component JSON, in order
 * @property ordered whether the items are numbered instead of bulleted
 */
@Serializable
@SerialName("text_list")
data class TextListNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val items: List<String> = emptyList(),
    @ProtoNumber(5) val ordered: Boolean = false,
) : ScreenNode

/**
 * A one-pixel line that separates content.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property orientation whether the line runs horizontally or vertically
 */
@Serializable
@SerialName("separator")
data class SeparatorNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val orientation: Orientation = Orientation.HORIZONTAL,
) : ScreenNode

/**
 * A keyboard key, drawn as a small muted key cap.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the key as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 */
@Serializable
@SerialName("kbd")
data class KbdNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
) : ScreenNode

/**
 * A row of keyboard keys, such as a shortcut.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the keys and texts, in order
 */
@Serializable
@SerialName("kbd_group")
data class KbdGroupNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this group with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): KbdGroupNode = copy(children = children)
}

/**
 * A small rounded label that marks something, such as a status.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property variant the look of the badge
 */
@Serializable
@SerialName("badge")
data class BadgeNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
    @ProtoNumber(6) val variant: BadgeVariant = BadgeVariant.DEFAULT,
) : ScreenNode

/**
 * A placeholder that pulses while content is loading.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property round whether the placeholder is a circle or pill instead of a rounded rectangle
 */
@Serializable
@SerialName("skeleton")
data class SkeletonNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val round: Boolean = false,
) : ScreenNode

/**
 * A turning loading indicator.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property size the side length of the indicator when it fits its content
 * @property color the theme colour the indicator is drawn in
 */
@Serializable
@SerialName("spinner")
data class SpinnerNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val size: Int = 10,
    @ProtoNumber(5) val color: IconColor = IconColor.FOREGROUND,
) : ScreenNode

/**
 * A box whose height follows its width in a fixed ratio, filled by its content.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out; ignored, since the ratio sets it
 * @property children the content, drawn over the whole box
 * @property ratio the width divided by the height
 */
@Serializable
@SerialName("aspect_ratio")
data class AspectRatioNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val ratio: Float = 1f,
) : ContainerNode {
    /**
     * Returns a copy of this box with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): AspectRatioNode = copy(children = children)
}

/**
 * The size of an avatar.
 */
@Serializable
enum class AvatarSize {
    /**
     * The regular size.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * A small size.
     */
    @ProtoNumber(1)
    SM,

    /**
     * A large size.
     */
    @ProtoNumber(2)
    LG,
}

/**
 * A round picture of a person: the face of a player's skin, or a resource-pack texture, with a
 * fallback text while no picture is available.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property playerId the UUID of the player whose face is shown, or `null`
 * @property texture the identifier of the texture shown when no player is named, or `null`
 * @property fallback the text shown while no picture is available, as component JSON
 * @property size the size of the avatar
 * @property badge whether a small badge is drawn at the bottom right
 * @property badgeIcon the Lucide name of an icon in the badge, or `null` for a plain dot
 */
@Serializable
@SerialName("avatar")
data class AvatarNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val playerId: String? = null,
    @ProtoNumber(5) val texture: String? = null,
    @ProtoNumber(6) val fallback: String = "",
    @ProtoNumber(7) val size: AvatarSize = AvatarSize.DEFAULT,
    @ProtoNumber(8) val badge: Boolean = false,
    @ProtoNumber(9) val badgeIcon: String? = null,
) : ScreenNode

/**
 * A row of overlapping avatars.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the avatars and an optional count, in order
 */
@Serializable
@SerialName("avatar_group")
data class AvatarGroupNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this group with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): AvatarGroupNode = copy(children = children)
}

/**
 * The count of further people at the end of an avatar group, drawn like an avatar.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the count as component JSON, such as `+3`
 * @property icon the Lucide name of an icon shown instead of the text, or `null` for none
 */
@Serializable
@SerialName("avatar_group_count")
data class AvatarGroupCountNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
) : ScreenNode

/**
 * The look of an alert.
 */
@Serializable
enum class AlertVariant {
    /**
     * A neutral alert on the card colour.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * An alert whose icon and texts are drawn in the destructive colour.
     */
    @ProtoNumber(1)
    DESTRUCTIVE,
}

/**
 * A bordered callout with an optional icon beside its title and description.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the title, description and further content, stacked beside the icon
 * @property variant the look of the alert
 * @property icon the Lucide name of the icon, or `null` for none
 */
@Serializable
@SerialName("alert")
data class AlertNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val variant: AlertVariant = AlertVariant.DEFAULT,
    @ProtoNumber(6) val icon: String? = null,
) : ContainerNode {
    /**
     * Returns a copy of this alert with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): AlertNode = copy(children = children)
}

/**
 * A bordered surface that stacks a header, content and footer.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the header, content and footer, in order
 */
@Serializable
@SerialName("card")
data class CardNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this card with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): CardNode = copy(children = children)
}

/**
 * The header of a card: its title and description stacked, with an optional action at the top
 * right.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the title, description and action
 */
@Serializable
@SerialName("card_header")
data class CardHeaderNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this header with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): CardHeaderNode = copy(children = children)
}

/**
 * The action of a card header, placed at its top right.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content of the action, such as a button
 */
@Serializable
@SerialName("card_action")
data class CardActionNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this action with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): CardActionNode = copy(children = children)
}

/**
 * The content of a card, stacked with the card's side padding.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, in order
 */
@Serializable
@SerialName("card_content")
data class CardContentNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this content with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): CardContentNode = copy(children = children)
}

/**
 * The footer of a card: a row of content centered on one line, such as buttons.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, in order
 */
@Serializable
@SerialName("card_footer")
data class CardFooterNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this footer with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): CardFooterNode = copy(children = children)
}

/**
 * The look of the media of an empty state.
 */
@Serializable
enum class EmptyMediaVariant {
    /**
     * The content as it is, without a background.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * An icon on a muted rounded square.
     */
    @ProtoNumber(1)
    ICON,
}

/**
 * The look of an item.
 */
@Serializable
enum class ItemVariant {
    /**
     * Without a border or background.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * With a border.
     */
    @ProtoNumber(1)
    OUTLINE,

    /**
     * On a muted background.
     */
    @ProtoNumber(2)
    MUTED,
}

/**
 * The size of an item.
 */
@Serializable
enum class ItemSize {
    /**
     * The regular padding and gaps.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * Smaller padding and gaps.
     */
    @ProtoNumber(1)
    SM,
}

/**
 * The look of the media of an item.
 */
@Serializable
enum class ItemMediaVariant {
    /**
     * The content as it is, without a background.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * An icon on a small muted bordered square.
     */
    @ProtoNumber(1)
    ICON,

    /**
     * The content clipped to a square, such as an image.
     */
    @ProtoNumber(2)
    IMAGE,
}

/**
 * An empty state: a centered stack of a header and content that says there is nothing to show.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the header and content, in order
 * @property outline whether a dashed border is drawn around the state
 */
@Serializable
@SerialName("empty")
data class EmptyNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val outline: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): EmptyNode = copy(children = children)
}

/**
 * The header of an empty state: its media, title and description, centered.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the media, title and description, in order
 */
@Serializable
@SerialName("empty_header")
data class EmptyHeaderNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): EmptyHeaderNode = copy(children = children)
}

/**
 * The media of an empty state, such as an icon or avatars.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content of the default variant
 * @property variant the look of the media
 * @property icon the Lucide name of the icon of the icon variant, or `null` for none
 */
@Serializable
@SerialName("empty_media")
data class EmptyMediaNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val variant: EmptyMediaVariant = EmptyMediaVariant.DEFAULT,
    @ProtoNumber(6) val icon: String? = null,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): EmptyMediaNode = copy(children = children)
}

/**
 * The content of an empty state, such as buttons, centered below its header.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, in order
 */
@Serializable
@SerialName("empty_content")
data class EmptyContentNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): EmptyContentNode = copy(children = children)
}

/**
 * A row of media, content and actions, with an optional header above and footer below; a clickable
 * item fires a widget action.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the header, media, content, actions and footer
 * @property variant the look of the item
 * @property size the size of the item
 * @property clickable whether the item can be clicked or activated to fire a widget action
 */
@Serializable
@SerialName("item")
data class ItemNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val variant: ItemVariant = ItemVariant.DEFAULT,
    @ProtoNumber(6) val size: ItemSize = ItemSize.DEFAULT,
    @ProtoNumber(7) val clickable: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ItemNode = copy(children = children)
}

/**
 * The media at the start of an item, such as an icon, avatar or image.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content of the default and image variants
 * @property variant the look of the media
 * @property icon the Lucide name of the icon of the icon variant, or `null` for none
 */
@Serializable
@SerialName("item_media")
data class ItemMediaNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val variant: ItemMediaVariant = ItemMediaVariant.DEFAULT,
    @ProtoNumber(6) val icon: String? = null,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ItemMediaNode = copy(children = children)
}

/**
 * The stacked title and description of an item, which takes the space its media and actions leave.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the title, description and further texts, in order
 */
@Serializable
@SerialName("item_content")
data class ItemContentNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ItemContentNode = copy(children = children)
}

/**
 * The actions at the end of an item, such as buttons.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, in order
 */
@Serializable
@SerialName("item_actions")
data class ItemActionsNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ItemActionsNode = copy(children = children)
}

/**
 * A line above the row of an item; its first part is placed at the start and the others at the end.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, in order
 */
@Serializable
@SerialName("item_header")
data class ItemHeaderNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ItemHeaderNode = copy(children = children)
}

/**
 * A line below the row of an item; its first part is placed at the start and the others at the end.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, in order
 */
@Serializable
@SerialName("item_footer")
data class ItemFooterNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ItemFooterNode = copy(children = children)
}

/**
 * A stack of items and separators, each as wide as the group.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the items and separators, in order
 */
@Serializable
@SerialName("item_group")
data class ItemGroupNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ItemGroupNode = copy(children = children)
}
