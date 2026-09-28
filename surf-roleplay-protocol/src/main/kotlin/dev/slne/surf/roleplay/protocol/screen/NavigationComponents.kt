package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * How many items of an accordion can be open at once.
 */
@Serializable
enum class AccordionType {
    /**
     * At most one item; opening an item closes the open one.
     */
    @ProtoNumber(0)
    SINGLE,

    /**
     * Any number of items.
     */
    @ProtoNumber(1)
    MULTIPLE,
}

/**
 * A collapsible: its triggers, and a content shown or hidden when a trigger fires.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the triggers and the collapsible contents
 * @property open whether the content is shown
 * @property notifyChange whether the mod reports every change of the state at once
 */
@Serializable
@SerialName("collapsible")
data class CollapsibleNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val open: Boolean = false,
    @ProtoNumber(6) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): CollapsibleNode = copy(children = children)
}

/**
 * The trigger part of a collapsible: an action of a widget inside it shows or hides the content
 * instead of reaching the server.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the widgets that toggle the content, usually one button
 */
@Serializable
@SerialName("collapsible_trigger")
data class CollapsibleTriggerNode(
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
    override fun withChildren(children: List<ScreenNode>): CollapsibleTriggerNode = copy(children = children)
}

/**
 * The content of a collapsible, shown while the collapsible is open.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, stacked
 */
@Serializable
@SerialName("collapsible_content")
data class CollapsibleContentNode(
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
    override fun withChildren(children: List<ScreenNode>): CollapsibleContentNode = copy(children = children)
}

/**
 * An accordion: stacked items, each with a trigger that shows or hides its content.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the accordion items
 * @property type how many items can be open at once
 * @property collapsible whether the open item of a single accordion can be closed
 * @property value the values of the open items
 * @property notifyChange whether the mod reports every change of the state at once
 */
@Serializable
@SerialName("accordion")
data class AccordionNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val type: AccordionType = AccordionType.SINGLE,
    @ProtoNumber(6) val collapsible: Boolean = false,
    @ProtoNumber(7) val value: List<String> = emptyList(),
    @ProtoNumber(8) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): AccordionNode = copy(children = children)
}

/**
 * An item of an accordion: its trigger and its content, with a border below.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the trigger and the content
 * @property value the value that identifies the item in the accordion
 * @property enabled whether the item can be opened and closed
 */
@Serializable
@SerialName("accordion_item")
data class AccordionItemNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val value: String = "",
    @ProtoNumber(6) val enabled: Boolean = true,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): AccordionItemNode = copy(children = children)
}

/**
 * The trigger of an accordion item: its text and a chevron that turns while the item is open.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text
 */
@Serializable
@SerialName("accordion_trigger")
data class AccordionTriggerNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
) : ScreenNode

/**
 * The content of an accordion item, shown while the item is open.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, stacked
 */
@Serializable
@SerialName("accordion_content")
data class AccordionContentNode(
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
    override fun withChildren(children: List<ScreenNode>): AccordionContentNode = copy(children = children)
}
