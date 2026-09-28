package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * One change to the tree of an open generic screen, addressed by node id.
 */
@Serializable
sealed interface PatchOperation

/**
 * Replaces a node, including its children, with another node.
 *
 * @property targetId the id of the node to replace
 * @property node the node that takes its place
 */
@Serializable
@SerialName("replace")
data class ReplaceNode(
    @ProtoNumber(1) val targetId: String,
    @ProtoNumber(2) val node: ScreenNode,
) : PatchOperation

/**
 * Inserts a node into a container.
 *
 * @property parentId the id of the container
 * @property index the position among the container's children; a value at or beyond the
 *           number of children appends the node
 * @property node the node to insert
 */
@Serializable
@SerialName("insert")
data class InsertNode(
    @ProtoNumber(1) val parentId: String,
    @ProtoNumber(2) val index: Int,
    @ProtoNumber(3) val node: ScreenNode,
) : PatchOperation

/**
 * Removes a node and its children.
 *
 * @property targetId the id of the node to remove
 */
@Serializable
@SerialName("remove")
data class RemoveNode(
    @ProtoNumber(1) val targetId: String,
) : PatchOperation

/**
 * Sets the text of a label, the caption of a button, the label of a checkbox or progress bar,
 * or the placeholder of a text input.
 *
 * @property targetId the id of the widget
 * @property text the new text as component JSON
 */
@Serializable
@SerialName("set_text")
data class SetText(
    @ProtoNumber(1) val targetId: String,
    @ProtoNumber(2) val text: String,
) : PatchOperation

/**
 * Sets the value of an input widget, in the same string form as [InputValue.value].
 *
 * @property targetId the id of the input widget
 * @property value the new value
 */
@Serializable
@SerialName("set_value")
data class SetValue(
    @ProtoNumber(1) val targetId: String,
    @ProtoNumber(2) val value: String,
) : PatchOperation

/**
 * Sets the filled fraction of a progress bar.
 *
 * @property targetId the id of the progress bar
 * @property progress the filled fraction, from `0` to `1`
 */
@Serializable
@SerialName("set_progress")
data class SetProgress(
    @ProtoNumber(1) val targetId: String,
    @ProtoNumber(2) val progress: Float,
) : PatchOperation

/**
 * Sets whether a button or input widget can be used.
 *
 * @property targetId the id of the widget
 * @property enabled whether the widget can be used
 */
@Serializable
@SerialName("set_enabled")
data class SetEnabled(
    @ProtoNumber(1) val targetId: String,
    @ProtoNumber(2) val enabled: Boolean,
) : PatchOperation
