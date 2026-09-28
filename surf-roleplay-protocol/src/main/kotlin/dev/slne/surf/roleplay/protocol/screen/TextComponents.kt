package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The kind of text a single-line text field holds.
 */
@Serializable
enum class TextInputType {
    /**
     * Any text.
     */
    @ProtoNumber(0)
    TEXT,

    /**
     * A password, drawn masked.
     */
    @ProtoNumber(1)
    PASSWORD,

    /**
     * An email address.
     */
    @ProtoNumber(2)
    EMAIL,
}

/**
 * The characters a one-time code input accepts.
 */
@Serializable
enum class OtpPattern {
    /**
     * The digits `0` to `9`.
     */
    @ProtoNumber(0)
    DIGITS,

    /**
     * Latin letters and digits.
     */
    @ProtoNumber(1)
    ALPHANUMERIC,
}

/**
 * Where an addon sits in an input group.
 */
@Serializable
enum class InputGroupAlign {
    /**
     * Before the control, on the same line.
     */
    @ProtoNumber(0)
    INLINE_START,

    /**
     * After the control, on the same line.
     */
    @ProtoNumber(1)
    INLINE_END,

    /**
     * Above the control, across the group's width.
     */
    @ProtoNumber(2)
    BLOCK_START,

    /**
     * Below the control, across the group's width.
     */
    @ProtoNumber(3)
    BLOCK_END,
}

/**
 * A multi-line text field whose text wraps at its width and scrolls vertically.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property value the current text; line breaks are `\n`
 * @property placeholder the hint shown while the field is empty, as component JSON
 * @property rows the number of visible lines when the node fits its content
 * @property maxLength the maximum number of characters, or `null` for no limit
 * @property required whether an empty value is invalid
 * @property enabled whether the player can edit the field
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("textarea")
data class TextareaNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val value: String = "",
    @ProtoNumber(5) val placeholder: String = "",
    @ProtoNumber(6) val rows: Int = 3,
    @ProtoNumber(7) val maxLength: Int? = null,
    @ProtoNumber(8) val required: Boolean = false,
    @ProtoNumber(9) val enabled: Boolean = true,
    @ProtoNumber(10) val notifyChange: Boolean = false,
) : ScreenNode

/**
 * A text field or textarea joined with addons into one bordered control.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children one [TextInputNode] or [TextareaNode] and any number of
 *           [InputGroupAddonNode]s
 */
@Serializable
@SerialName("input_group")
data class InputGroupNode(
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
    override fun withChildren(children: List<ScreenNode>): InputGroupNode = copy(children = children)
}

/**
 * A row of texts, icons or buttons at one side of an input group's control.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the parts of the addon, in order
 * @property align where the addon sits
 */
@Serializable
@SerialName("input_group_addon")
data class InputGroupAddonNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val align: InputGroupAlign = InputGroupAlign.INLINE_START,
) : ContainerNode {
    /**
     * Returns a copy of this addon with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): InputGroupAddonNode = copy(children = children)
}

/**
 * A muted text in an input group addon.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 * @property icon the name of a Lucide icon drawn before the text, or `null` for none
 */
@Serializable
@SerialName("input_group_text")
data class InputGroupTextNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
) : ScreenNode

/**
 * An input for a one-time code, entered one character per slot.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property value the characters entered so far
 * @property length the number of slots
 * @property groups the number of slots in each group, which are drawn apart with a separator;
 *           empty for a single group
 * @property pattern the characters the slots accept
 * @property required whether an empty value is invalid
 * @property enabled whether the player can enter a code
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("input_otp")
data class InputOtpNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val value: String = "",
    @ProtoNumber(5) val length: Int = 6,
    @ProtoNumber(6) val groups: List<Int> = emptyList(),
    @ProtoNumber(7) val pattern: OtpPattern = OtpPattern.DIGITS,
    @ProtoNumber(8) val required: Boolean = false,
    @ProtoNumber(9) val enabled: Boolean = true,
    @ProtoNumber(10) val notifyChange: Boolean = false,
) : ScreenNode
