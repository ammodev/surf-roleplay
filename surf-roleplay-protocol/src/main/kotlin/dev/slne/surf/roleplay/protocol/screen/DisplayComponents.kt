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
