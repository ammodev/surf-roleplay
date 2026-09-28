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

/**
 * How a tab list is drawn.
 */
@Serializable
enum class TabsVariant {
    /**
     * A muted pill in which the active trigger is raised.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * Plain triggers with a line under the active one.
     */
    @ProtoNumber(1)
    LINE,
}

/**
 * Tabs: a tab list and contents, of which only the content of the selected tab is shown.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children one tab list and the tab contents
 * @property value the value of the selected tab
 * @property orientation whether the triggers are in a row above the contents or in a column
 *           beside them
 * @property notifyChange whether the mod reports every change of the state at once
 */
@Serializable
@SerialName("tabs")
data class TabsNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val value: String = "",
    @ProtoNumber(6) val orientation: Orientation = Orientation.HORIZONTAL,
    @ProtoNumber(7) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TabsNode = copy(children = children)
}

/**
 * The list of the triggers of tabs.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the tab triggers
 * @property variant how the list is drawn
 */
@Serializable
@SerialName("tabs_list")
data class TabsListNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val variant: TabsVariant = TabsVariant.DEFAULT,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TabsListNode = copy(children = children)
}

/**
 * A trigger of tabs: selecting it shows the content with the same value.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property value the value of the tab
 * @property text the text
 * @property icon the Lucide name of an icon before the text, or null for none
 * @property enabled whether the tab can be selected
 */
@Serializable
@SerialName("tabs_trigger")
data class TabsTriggerNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val value: String = "",
    @ProtoNumber(5) val text: String = "",
    @ProtoNumber(6) val icon: String? = null,
    @ProtoNumber(7) val enabled: Boolean = true,
) : ScreenNode

/**
 * The content of a tab, shown while its tab is selected.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, stacked
 * @property value the value of the tab it belongs to
 */
@Serializable
@SerialName("tabs_content")
data class TabsContentNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val value: String = "",
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TabsContentNode = copy(children = children)
}

/**
 * A breadcrumb: the path to the current page.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children one breadcrumb list
 */
@Serializable
@SerialName("breadcrumb")
data class BreadcrumbNode(
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
    override fun withChildren(children: List<ScreenNode>): BreadcrumbNode = copy(children = children)
}

/**
 * The list of a breadcrumb: its items and separators in a row, in muted text.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the items and separators
 */
@Serializable
@SerialName("breadcrumb_list")
data class BreadcrumbListNode(
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
    override fun withChildren(children: List<ScreenNode>): BreadcrumbListNode = copy(children = children)
}

/**
 * An item of a breadcrumb: a link, the current page or an ellipsis.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the link, page or ellipsis
 */
@Serializable
@SerialName("breadcrumb_item")
data class BreadcrumbItemNode(
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
    override fun withChildren(children: List<ScreenNode>): BreadcrumbItemNode = copy(children = children)
}

/**
 * A link of a breadcrumb: muted text that turns to the foreground colour when hovered and fires an
 * action when clicked.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text
 * @property enabled whether the link can be clicked
 */
@Serializable
@SerialName("breadcrumb_link")
data class BreadcrumbLinkNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val enabled: Boolean = true,
) : ScreenNode

/**
 * The current page of a breadcrumb, in the foreground colour and not clickable.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text
 */
@Serializable
@SerialName("breadcrumb_page")
data class BreadcrumbPageNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
) : ScreenNode

/**
 * A separator between breadcrumb items: a chevron or another icon.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property icon the Lucide name of the icon
 */
@Serializable
@SerialName("breadcrumb_separator")
data class BreadcrumbSeparatorNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val icon: String = "chevron-right",
) : ScreenNode

/**
 * An ellipsis that stands for collapsed breadcrumb items, often the trigger of a dropdown menu.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 */
@Serializable
@SerialName("breadcrumb_ellipsis")
data class BreadcrumbEllipsisNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
) : ScreenNode

/**
 * A pagination: page links centered across the available width.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children one pagination content
 */
@Serializable
@SerialName("pagination")
data class PaginationNode(
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
    override fun withChildren(children: List<ScreenNode>): PaginationNode = copy(children = children)
}

/**
 * The row of the items of a pagination.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the pagination items
 */
@Serializable
@SerialName("pagination_content")
data class PaginationContentNode(
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
    override fun withChildren(children: List<ScreenNode>): PaginationContentNode = copy(children = children)
}

/**
 * An item of a pagination: a link, a previous or next link, or an ellipsis.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the link or ellipsis
 */
@Serializable
@SerialName("pagination_item")
data class PaginationItemNode(
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
    override fun withChildren(children: List<ScreenNode>): PaginationItemNode = copy(children = children)
}

/**
 * A page link of a pagination, outlined while it is the current page, that fires an action when
 * clicked.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text
 * @property active whether the link is the current page
 * @property enabled whether the link can be clicked
 */
@Serializable
@SerialName("pagination_link")
data class PaginationLinkNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val active: Boolean = false,
    @ProtoNumber(6) val enabled: Boolean = true,
) : ScreenNode

/**
 * The link to the previous page: a chevron and its text.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text
 * @property enabled whether the link can be clicked
 */
@Serializable
@SerialName("pagination_previous")
data class PaginationPreviousNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val enabled: Boolean = true,
) : ScreenNode

/**
 * The link to the next page: its text and a chevron.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text
 * @property enabled whether the link can be clicked
 */
@Serializable
@SerialName("pagination_next")
data class PaginationNextNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val enabled: Boolean = true,
) : ScreenNode

/**
 * An ellipsis that stands for pages without a link.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 */
@Serializable
@SerialName("pagination_ellipsis")
data class PaginationEllipsisNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
) : ScreenNode

/**
 * The directions a scroll area scrolls in.
 */
@Serializable
enum class ScrollOrientation {
    /**
     * Up and down; the content is as wide as the area.
     */
    @ProtoNumber(0)
    VERTICAL,

    /**
     * Left and right; the content is as tall as the area.
     */
    @ProtoNumber(1)
    HORIZONTAL,

    /**
     * In both directions.
     */
    @ProtoNumber(2)
    BOTH,
}

/**
 * A scroll area: its content, stacked, scrolled inside the area with the wheel or by dragging thin
 * scroll bars.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, stacked
 * @property orientation the directions the area scrolls in
 */
@Serializable
@SerialName("scroll_area")
data class ScrollAreaNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val orientation: ScrollOrientation = ScrollOrientation.VERTICAL,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ScrollAreaNode = copy(children = children)
}
