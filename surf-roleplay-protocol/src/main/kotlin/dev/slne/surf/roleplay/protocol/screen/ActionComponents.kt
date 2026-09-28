package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The look of a button.
 */
@Serializable
enum class ButtonVariant {
    /**
     * A filled button in the primary colour.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * A filled button in the destructive colour.
     */
    @ProtoNumber(1)
    DESTRUCTIVE,

    /**
     * A bordered button on the background.
     */
    @ProtoNumber(2)
    OUTLINE,

    /**
     * A filled button in the secondary colour.
     */
    @ProtoNumber(3)
    SECONDARY,

    /**
     * A button without background until hovered.
     */
    @ProtoNumber(4)
    GHOST,

    /**
     * A button that looks like a link.
     */
    @ProtoNumber(5)
    LINK,
}

/**
 * The size of a button. The icon sizes are square and show only the icon.
 */
@Serializable
enum class ButtonSize {
    /**
     * The regular size.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * The smallest size.
     */
    @ProtoNumber(1)
    XS,

    /**
     * A small size.
     */
    @ProtoNumber(2)
    SM,

    /**
     * A large size.
     */
    @ProtoNumber(3)
    LG,

    /**
     * A square button of the regular height.
     */
    @ProtoNumber(4)
    ICON,

    /**
     * A square button of the smallest height.
     */
    @ProtoNumber(5)
    ICON_XS,

    /**
     * A square button of the small height.
     */
    @ProtoNumber(6)
    ICON_SM,

    /**
     * A square button of the large height.
     */
    @ProtoNumber(7)
    ICON_LG,
}

/**
 * The direction in which a component arranges its parts.
 */
@Serializable
enum class Orientation {
    /**
     * From left to right.
     */
    @ProtoNumber(0)
    HORIZONTAL,

    /**
     * From top to bottom.
     */
    @ProtoNumber(1)
    VERTICAL,
}

/**
 * The look of a toggle.
 */
@Serializable
enum class ToggleVariant {
    /**
     * A toggle without border.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * A bordered toggle.
     */
    @ProtoNumber(1)
    OUTLINE,
}

/**
 * The size of a toggle.
 */
@Serializable
enum class ToggleSize {
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
 * A group of buttons joined into one control.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the buttons, texts and separators of the group, in order
 * @property orientation whether the group runs horizontally or vertically
 */
@Serializable
@SerialName("button_group")
data class ButtonGroupNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val orientation: Orientation = Orientation.HORIZONTAL,
) : ContainerNode {
    /**
     * Returns a copy of this group with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ButtonGroupNode = copy(children = children)
}

/**
 * A text part of a button group, drawn like a muted button.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 * @property icon the name of a Lucide icon drawn before the text, or `null` for none
 */
@Serializable
@SerialName("button_group_text")
data class ButtonGroupTextNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
) : ScreenNode

/**
 * A line between the parts of a button group.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 */
@Serializable
@SerialName("button_group_separator")
data class ButtonGroupSeparatorNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
) : ScreenNode

/**
 * A two-state button. Pressing it switches its state and sends a widget action with the screen's
 * input values, including its own new state as `true` or `false`.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the caption as component JSON
 * @property icon the name of a Lucide icon drawn before the caption, or `null` for none
 * @property pressed whether the toggle is on
 * @property variant the look of the toggle
 * @property size the size of the toggle
 * @property enabled whether the toggle can be pressed
 */
@Serializable
@SerialName("toggle")
data class ToggleNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
    @ProtoNumber(6) val pressed: Boolean = false,
    @ProtoNumber(7) val variant: ToggleVariant = ToggleVariant.DEFAULT,
    @ProtoNumber(8) val size: ToggleSize = ToggleSize.DEFAULT,
    @ProtoNumber(9) val enabled: Boolean = true,
) : ScreenNode

/**
 * One item of a [ToggleGroupNode].
 *
 * @property value the value reported when the item is on
 * @property text the caption as component JSON
 * @property icon the name of a Lucide icon drawn before the caption, or `null` for none
 * @property enabled whether the item can be switched
 */
@Serializable
data class ToggleGroupItem(
    @ProtoNumber(1) val value: String,
    @ProtoNumber(2) val text: String = "",
    @ProtoNumber(3) val icon: String? = null,
    @ProtoNumber(4) val enabled: Boolean = true,
)

/**
 * A group of toggles joined into one input. Its value is the values of the items that are on,
 * joined by commas in item order.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property items the items, in order
 * @property multiple whether several items can be on at once; otherwise at most one is
 * @property selected the values of the items that are on
 * @property variant the look of the items
 * @property size the size of the items
 * @property spacing the space between items in GUI pixels; zero joins them
 * @property orientation whether the items run horizontally or vertically
 * @property enabled whether the player can switch items
 * @property required whether having no item on is invalid
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("toggle_group")
data class ToggleGroupNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val items: List<ToggleGroupItem> = emptyList(),
    @ProtoNumber(5) val multiple: Boolean = false,
    @ProtoNumber(6) val selected: List<String> = emptyList(),
    @ProtoNumber(7) val variant: ToggleVariant = ToggleVariant.DEFAULT,
    @ProtoNumber(8) val size: ToggleSize = ToggleSize.DEFAULT,
    @ProtoNumber(9) val spacing: Int = 0,
    @ProtoNumber(10) val orientation: Orientation = Orientation.HORIZONTAL,
    @ProtoNumber(11) val enabled: Boolean = true,
    @ProtoNumber(12) val required: Boolean = false,
    @ProtoNumber(13) val notifyChange: Boolean = false,
) : ScreenNode
