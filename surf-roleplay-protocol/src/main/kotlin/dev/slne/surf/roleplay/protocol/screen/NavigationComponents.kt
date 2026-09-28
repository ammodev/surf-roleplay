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

/**
 * A group of panels side by side or stacked, with handles between them that resize the panels on
 * both sides.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the panels, with a handle between every two
 * @property orientation whether the panels are side by side or stacked
 * @property notifyChange whether the mod reports the panel sizes after the player resized them
 */
@Serializable
@SerialName("resizable_panel_group")
data class ResizablePanelGroupNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val orientation: Orientation = Orientation.HORIZONTAL,
    @ProtoNumber(6) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ResizablePanelGroupNode = copy(children = children)
}

/**
 * A panel of a resizable group, taking a share of the group in percent.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, stacked
 * @property defaultSize the share of the group the panel takes at first, in percent, or 0 to
 *           share what the other panels leave
 * @property minSize the smallest share the panel can take, in percent
 * @property maxSize the largest share the panel can take, in percent
 */
@Serializable
@SerialName("resizable_panel")
data class ResizablePanelNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val defaultSize: Double = 0.0,
    @ProtoNumber(6) val minSize: Double = 0.0,
    @ProtoNumber(7) val maxSize: Double = 100.0,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ResizablePanelNode = copy(children = children)
}

/**
 * A handle between two panels of a resizable group: a thin line, optionally with a grip, that is
 * dragged or moved with the arrow keys.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property withHandle whether a grip is drawn on the line
 */
@Serializable
@SerialName("resizable_handle")
data class ResizableHandleNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val withHandle: Boolean = false,
) : ScreenNode

/**
 * A carousel: a content of slides with buttons to the previous and next slide.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children one carousel content and the previous and next buttons
 * @property orientation whether the slides move sideways or up and down
 * @property loop whether the last slide is followed by the first
 * @property index the index of the first shown slide
 * @property notifyChange whether the mod reports every change of the shown slide at once
 */
@Serializable
@SerialName("carousel")
data class CarouselNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val orientation: Orientation = Orientation.HORIZONTAL,
    @ProtoNumber(6) val loop: Boolean = false,
    @ProtoNumber(7) val index: Int = 0,
    @ProtoNumber(8) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): CarouselNode = copy(children = children)
}

/**
 * The content of a carousel: its slides in a row or a column, of which the part from the current
 * slide on is shown.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the carousel items
 */
@Serializable
@SerialName("carousel_content")
data class CarouselContentNode(
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
    override fun withChildren(children: List<ScreenNode>): CarouselContentNode = copy(children = children)
}

/**
 * A slide of a carousel, taking a share of the content.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content of the slide, stacked
 * @property basis the share of the content the slide takes, in percent
 */
@Serializable
@SerialName("carousel_item")
data class CarouselItemNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val basis: Double = 100.0,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): CarouselItemNode = copy(children = children)
}

/**
 * The button that shows the previous slide of a carousel: a round outline button with an arrow.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property enabled whether the button can be used
 */
@Serializable
@SerialName("carousel_previous")
data class CarouselPreviousNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val enabled: Boolean = true,
) : ScreenNode

/**
 * The button that shows the next slide of a carousel: a round outline button with an arrow.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property enabled whether the button can be used
 */
@Serializable
@SerialName("carousel_next")
data class CarouselNextNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val enabled: Boolean = true,
) : ScreenNode

/**
 * A navigation menu: a list of items whose triggers open content below them, and links.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children one navigation menu list
 */
@Serializable
@SerialName("navigation_menu")
data class NavigationMenuNode(
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
    override fun withChildren(children: List<ScreenNode>): NavigationMenuNode = copy(children = children)
}

/**
 * The row of the items of a navigation menu.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the navigation menu items
 */
@Serializable
@SerialName("navigation_menu_list")
data class NavigationMenuListNode(
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
    override fun withChildren(children: List<ScreenNode>): NavigationMenuListNode = copy(children = children)
}

/**
 * An item of a navigation menu: a trigger with a content that opens below it on hover or click, or
 * a link.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children a trigger and a content, or a link
 * @property open whether the content is open
 * @property notifyChange whether the mod reports every opening and closing at once
 */
@Serializable
@SerialName("navigation_menu_item")
data class NavigationMenuItemNode(
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
    override fun withChildren(children: List<ScreenNode>): NavigationMenuItemNode = copy(children = children)
}

/**
 * The trigger of a navigation menu item: its text and a chevron that turns while the content is
 * open.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text
 * @property enabled whether the trigger can open the content
 */
@Serializable
@SerialName("navigation_menu_trigger")
data class NavigationMenuTriggerNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val enabled: Boolean = true,
) : ScreenNode

/**
 * The content of a navigation menu item, shown on a surface below its trigger while open.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the links and other content
 */
@Serializable
@SerialName("navigation_menu_content")
data class NavigationMenuContentNode(
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
    override fun withChildren(children: List<ScreenNode>): NavigationMenuContentNode = copy(children = children)
}

/**
 * A link of a navigation menu: its content, highlighted when hovered or active, that fires an
 * action when clicked. Directly in an item it is drawn like a trigger.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content of the link, such as a title and a description
 * @property active whether the link leads to the current page
 * @property enabled whether the link can be clicked
 */
@Serializable
@SerialName("navigation_menu_link")
data class NavigationMenuLinkNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val active: Boolean = false,
    @ProtoNumber(6) val enabled: Boolean = true,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): NavigationMenuLinkNode = copy(children = children)
}

/**
 * The side of the screen a sidebar is on.
 */
@Serializable
enum class SidebarSide {
    /**
     * The left side.
     */
    @ProtoNumber(0)
    LEFT,

    /**
     * The right side.
     */
    @ProtoNumber(1)
    RIGHT,
}

/**
 * How a sidebar is drawn.
 */
@Serializable
enum class SidebarVariant {
    /**
     * A panel along the edge with a border to the content.
     */
    @ProtoNumber(0)
    SIDEBAR,

    /**
     * A card with a margin, floating beside the content.
     */
    @ProtoNumber(1)
    FLOATING,

    /**
     * A panel without a border, with the content drawn as a card beside it.
     */
    @ProtoNumber(2)
    INSET,
}

/**
 * How a sidebar collapses.
 */
@Serializable
enum class SidebarCollapsible {
    /**
     * It disappears completely.
     */
    @ProtoNumber(0)
    OFFCANVAS,

    /**
     * It shrinks to the icons of its menu buttons.
     */
    @ProtoNumber(1)
    ICON,

    /**
     * It cannot collapse.
     */
    @ProtoNumber(2)
    NONE,
}

/**
 * The size of a sidebar menu button.
 */
@Serializable
enum class SidebarMenuButtonSize {
    /**
     * The regular height.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * A lower height.
     */
    @ProtoNumber(1)
    SM,

    /**
     * A larger height.
     */
    @ProtoNumber(2)
    LG,
}

/**
 * How a sidebar menu button is drawn.
 */
@Serializable
enum class SidebarMenuButtonVariant {
    /**
     * Plain, highlighted when hovered.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * On the background colour with a border.
     */
    @ProtoNumber(1)
    OUTLINE,
}

/**
 * The size of the text of a sidebar sub-menu button.
 */
@Serializable
enum class SidebarMenuSubButtonSize {
    /**
     * Smaller text.
     */
    @ProtoNumber(0)
    SM,

    /**
     * Regular text.
     */
    @ProtoNumber(1)
    MD,
}

/**
 * The frame of a sidebar layout: a sidebar and the inset beside it, with the expanded state of the
 * sidebar.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children one sidebar and one sidebar inset
 * @property open whether the sidebar is expanded
 * @property notifyChange whether the mod reports every expanding and collapsing at once
 */
@Serializable
@SerialName("sidebar_provider")
data class SidebarProviderNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val open: Boolean = true,
    @ProtoNumber(6) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): SidebarProviderNode = copy(children = children)
}

/**
 * A sidebar: a column of header, content and footer on one side of its provider.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the header, content, footer and rail
 * @property side the side the sidebar is on
 * @property variant how the sidebar is drawn
 * @property collapsible how the sidebar collapses
 */
@Serializable
@SerialName("sidebar")
data class SidebarNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val side: SidebarSide = SidebarSide.LEFT,
    @ProtoNumber(6) val variant: SidebarVariant = SidebarVariant.SIDEBAR,
    @ProtoNumber(7) val collapsible: SidebarCollapsible = SidebarCollapsible.OFFCANVAS,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): SidebarNode = copy(children = children)
}

/**
 * The main content beside a sidebar, taking the rest of the provider.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, stacked
 */
@Serializable
@SerialName("sidebar_inset")
data class SidebarInsetNode(
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
    override fun withChildren(children: List<ScreenNode>): SidebarInsetNode = copy(children = children)
}

/**
 * The header of a sidebar, above its content.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the header content, stacked
 */
@Serializable
@SerialName("sidebar_header")
data class SidebarHeaderNode(
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
    override fun withChildren(children: List<ScreenNode>): SidebarHeaderNode = copy(children = children)
}

/**
 * The footer of a sidebar, below its content.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the footer content, stacked
 */
@Serializable
@SerialName("sidebar_footer")
data class SidebarFooterNode(
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
    override fun withChildren(children: List<ScreenNode>): SidebarFooterNode = copy(children = children)
}

/**
 * The scrolling content of a sidebar between its header and footer.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the sidebar groups
 */
@Serializable
@SerialName("sidebar_content")
data class SidebarContentNode(
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
    override fun withChildren(children: List<ScreenNode>): SidebarContentNode = copy(children = children)
}

/**
 * A group of a sidebar: a label, an optional action and a content.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the label, action and content
 */
@Serializable
@SerialName("sidebar_group")
data class SidebarGroupNode(
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
    override fun withChildren(children: List<ScreenNode>): SidebarGroupNode = copy(children = children)
}

/**
 * The label of a sidebar group, in muted text; hidden while the sidebar is collapsed to icons.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text
 */
@Serializable
@SerialName("sidebar_group_label")
data class SidebarGroupLabelNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
) : ScreenNode

/**
 * A small icon button at the end of a sidebar group label.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property icon the Lucide name of the icon
 * @property enabled whether the button can be clicked
 */
@Serializable
@SerialName("sidebar_group_action")
data class SidebarGroupActionNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val icon: String = "plus",
    @ProtoNumber(5) val enabled: Boolean = true,
) : ScreenNode

/**
 * The content of a sidebar group.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, usually one sidebar menu
 */
@Serializable
@SerialName("sidebar_group_content")
data class SidebarGroupContentNode(
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
    override fun withChildren(children: List<ScreenNode>): SidebarGroupContentNode = copy(children = children)
}

/**
 * A menu of a sidebar: its items, stacked.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the sidebar menu items
 */
@Serializable
@SerialName("sidebar_menu")
data class SidebarMenuNode(
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
    override fun withChildren(children: List<ScreenNode>): SidebarMenuNode = copy(children = children)
}

/**
 * An item of a sidebar menu: a button with an optional action and badge at its end, and an optional
 * sub-menu below.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the button, action, badge and sub-menu
 */
@Serializable
@SerialName("sidebar_menu_item")
data class SidebarMenuItemNode(
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
    override fun withChildren(children: List<ScreenNode>): SidebarMenuItemNode = copy(children = children)
}

/**
 * The button of a sidebar menu item: its icon and text, highlighted while active; only the icon
 * while the sidebar is collapsed to icons, with the tooltip on hover.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text
 * @property icon the Lucide name of an icon before the text, or null for none
 * @property size the size of the button
 * @property variant how the button is drawn
 * @property active whether the button leads to the current page
 * @property tooltip the text shown beside the button while the sidebar is collapsed to icons, or
 *           empty for none
 * @property enabled whether the button can be clicked
 */
@Serializable
@SerialName("sidebar_menu_button")
data class SidebarMenuButtonNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
    @ProtoNumber(6) val size: SidebarMenuButtonSize = SidebarMenuButtonSize.DEFAULT,
    @ProtoNumber(7) val variant: SidebarMenuButtonVariant = SidebarMenuButtonVariant.DEFAULT,
    @ProtoNumber(8) val active: Boolean = false,
    @ProtoNumber(9) val tooltip: String = "",
    @ProtoNumber(10) val enabled: Boolean = true,
) : ScreenNode

/**
 * A small icon button at the end of a sidebar menu button.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property icon the Lucide name of the icon
 * @property showOnHover whether the action is shown only while its item is hovered or focused
 * @property enabled whether the button can be clicked
 */
@Serializable
@SerialName("sidebar_menu_action")
data class SidebarMenuActionNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val icon: String = "ellipsis",
    @ProtoNumber(5) val showOnHover: Boolean = false,
    @ProtoNumber(6) val enabled: Boolean = true,
) : ScreenNode

/**
 * A small count or label at the end of a sidebar menu button.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text
 */
@Serializable
@SerialName("sidebar_menu_badge")
data class SidebarMenuBadgeNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
) : ScreenNode

/**
 * A placeholder for a sidebar menu button that is still loading.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property showIcon whether a placeholder for the icon is drawn
 */
@Serializable
@SerialName("sidebar_menu_skeleton")
data class SidebarMenuSkeletonNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val showIcon: Boolean = false,
) : ScreenNode

/**
 * A sub-menu below a sidebar menu button, indented with a line; hidden while the sidebar is
 * collapsed to icons.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the sub-menu items
 */
@Serializable
@SerialName("sidebar_menu_sub")
data class SidebarMenuSubNode(
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
    override fun withChildren(children: List<ScreenNode>): SidebarMenuSubNode = copy(children = children)
}

/**
 * An item of a sidebar sub-menu.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the sub-menu button
 */
@Serializable
@SerialName("sidebar_menu_sub_item")
data class SidebarMenuSubItemNode(
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
    override fun withChildren(children: List<ScreenNode>): SidebarMenuSubItemNode = copy(children = children)
}

/**
 * The button of a sidebar sub-menu item.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text
 * @property icon the Lucide name of an icon before the text, or null for none
 * @property size the size of the text
 * @property active whether the button leads to the current page
 * @property enabled whether the button can be clicked
 */
@Serializable
@SerialName("sidebar_menu_sub_button")
data class SidebarMenuSubButtonNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
    @ProtoNumber(6) val size: SidebarMenuSubButtonSize = SidebarMenuSubButtonSize.MD,
    @ProtoNumber(7) val active: Boolean = false,
    @ProtoNumber(8) val enabled: Boolean = true,
) : ScreenNode

/**
 * A small ghost button that expands and collapses the sidebar of its provider.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property enabled whether the trigger can be used
 */
@Serializable
@SerialName("sidebar_trigger")
data class SidebarTriggerNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val enabled: Boolean = true,
) : ScreenNode

/**
 * A thin strip along the inner edge of a sidebar that expands and collapses it when clicked.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 */
@Serializable
@SerialName("sidebar_rail")
data class SidebarRailNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
) : ScreenNode
