package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The kind of text of a form field.
 */
@Serializable
enum class FieldTextKind {
    /**
     * The heading of a field set.
     */
    @ProtoNumber(0)
    LEGEND,

    /**
     * The heading of a field set, drawn like a label.
     */
    @ProtoNumber(1)
    LEGEND_LABEL,

    /**
     * The label of a field's input; a click on it focuses the input it names.
     */
    @ProtoNumber(2)
    LABEL,

    /**
     * The title of a field whose input has its own label.
     */
    @ProtoNumber(3)
    TITLE,

    /**
     * A muted description of a field.
     */
    @ProtoNumber(4)
    DESCRIPTION,

    /**
     * The error of a field, in the destructive colour, and not drawn while empty.
     */
    @ProtoNumber(5)
    ERROR,
}

/**
 * A group of related fields with a legend.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the legend, fields and groups of the set, in order
 */
@Serializable
@SerialName("field_set")
data class FieldSetNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this set with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): FieldSetNode = copy(children = children)
}

/**
 * A stack of fields.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the fields and separators of the group, in order
 */
@Serializable
@SerialName("field_group")
data class FieldGroupNode(
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
    override fun withChildren(children: List<ScreenNode>): FieldGroupNode = copy(children = children)
}

/**
 * One form field: an input with its label, description and error. An invalid field draws its
 * label and title in the destructive colour.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the parts of the field, in order
 * @property orientation whether the parts are stacked or placed side by side
 */
@Serializable
@SerialName("field")
data class FieldNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val orientation: Orientation = Orientation.VERTICAL,
) : ContainerNode {
    /**
     * Returns a copy of this field with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): FieldNode = copy(children = children)
}

/**
 * A stack of a field's texts next to its input in a horizontal field.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the texts, in order
 */
@Serializable
@SerialName("field_content")
data class FieldContentNode(
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
    override fun withChildren(children: List<ScreenNode>): FieldContentNode = copy(children = children)
}

/**
 * A text of a form: a legend, label, title, description or error.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property kind the kind of text
 * @property text the text as component JSON
 * @property forId the id of the input a click on a label focuses, or `null` for none
 */
@Serializable
@SerialName("field_text")
data class FieldTextNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val kind: FieldTextKind = FieldTextKind.LABEL,
    @ProtoNumber(5) val text: String = "",
    @ProtoNumber(6) val forId: String? = null,
) : ScreenNode

/**
 * A line between fields, optionally with a text in its middle.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON, or `null` for a plain line
 */
@Serializable
@SerialName("field_separator")
data class FieldSeparatorNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String? = null,
) : ScreenNode

/**
 * A form. Enter in one of its single-line inputs clicks its submit button.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content of the form, in order
 * @property submitId the id of the button that Enter clicks, or `null` for none
 */
@Serializable
@SerialName("form")
data class FormNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val submitId: String? = null,
) : ContainerNode {
    /**
     * Returns a copy of this form with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): FormNode = copy(children = children)
}

/**
 * Marks a field or an input as invalid, or clears the mark, as the result of a check the server
 * made. An input loses the mark when the player changes it.
 *
 * @property targetId the id of the field or input
 * @property invalid whether it is invalid
 */
@Serializable
@SerialName("set_invalid")
data class SetInvalid(
    @ProtoNumber(1) val targetId: String,
    @ProtoNumber(2) val invalid: Boolean,
) : PatchOperation
