package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The side of its trigger an overlay opens on.
 */
@Serializable
enum class OverlaySide {
    /**
     * Below the trigger.
     */
    @ProtoNumber(0)
    BOTTOM,

    /**
     * Above the trigger.
     */
    @ProtoNumber(1)
    TOP,

    /**
     * To the right of the trigger.
     */
    @ProtoNumber(2)
    RIGHT,

    /**
     * To the left of the trigger.
     */
    @ProtoNumber(3)
    LEFT,
}

/**
 * Opens or closes an overlay, such as a popover, menu or dialog.
 *
 * @property targetId the id of the overlay
 * @property open whether the overlay is open
 */
@Serializable
@SerialName("set_open")
data class SetOpen(
    @ProtoNumber(1) val targetId: String,
    @ProtoNumber(2) val open: Boolean,
) : PatchOperation

/**
 * A popover: its triggers, and a content that opens next to them when a trigger is clicked.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the triggers and one popover content
 * @property open whether the popover is open
 * @property side the side of the triggers the content opens on
 * @property align how the content is aligned along that side
 * @property notifyChange whether the mod reports every opening and closing at once
 */
@Serializable
@SerialName("popover")
data class PopoverNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val open: Boolean = false,
    @ProtoNumber(6) val side: OverlaySide = OverlaySide.BOTTOM,
    @ProtoNumber(7) val align: Align = Align.CENTER,
    @ProtoNumber(8) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): PopoverNode = copy(children = children)
}

/**
 * The content of a popover, on a bordered surface.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, in order
 */
@Serializable
@SerialName("popover_content")
data class PopoverContentNode(
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
    override fun withChildren(children: List<ScreenNode>): PopoverContentNode = copy(children = children)
}

/**
 * The header of a popover content: its title and description, stacked.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the title and description
 */
@Serializable
@SerialName("popover_header")
data class PopoverHeaderNode(
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
    override fun withChildren(children: List<ScreenNode>): PopoverHeaderNode = copy(children = children)
}

/**
 * A hover card: its triggers, and a content that opens next to them while the mouse rests on them.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the triggers and one hover card content
 * @property open whether the hover card is open
 * @property side the side of the triggers the content opens on
 * @property align how the content is aligned along that side
 * @property openDelay how long the mouse must rest on a trigger before the content opens, in
 *           milliseconds
 * @property closeDelay how long after the mouse left the triggers and the content it closes, in
 *           milliseconds
 * @property notifyChange whether the mod reports every opening and closing at once
 */
@Serializable
@SerialName("hover_card")
data class HoverCardNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val open: Boolean = false,
    @ProtoNumber(6) val side: OverlaySide = OverlaySide.BOTTOM,
    @ProtoNumber(7) val align: Align = Align.CENTER,
    @ProtoNumber(8) val openDelay: Int = 700,
    @ProtoNumber(9) val closeDelay: Int = 300,
    @ProtoNumber(10) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): HoverCardNode = copy(children = children)
}

/**
 * The content of a hover card, on a bordered surface.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, in order
 */
@Serializable
@SerialName("hover_card_content")
data class HoverCardContentNode(
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
    override fun withChildren(children: List<ScreenNode>): HoverCardContentNode = copy(children = children)
}

/**
 * A tooltip: its triggers, and a short text shown next to them while a trigger is hovered or
 * focused.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the triggers
 * @property text the text as component JSON
 * @property side the side of the triggers the text is shown on
 */
@Serializable
@SerialName("tooltip")
data class TooltipNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val text: String = "",
    @ProtoNumber(6) val side: OverlaySide = OverlaySide.TOP,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TooltipNode = copy(children = children)
}
