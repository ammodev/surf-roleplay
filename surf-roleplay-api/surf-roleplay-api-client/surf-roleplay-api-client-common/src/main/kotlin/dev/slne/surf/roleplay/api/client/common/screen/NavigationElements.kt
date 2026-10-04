package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component

/**
 * How many items of an accordion can be open at once.
 */
enum class AccordionType {
    /**
     * At most one item; opening an item closes the open one.
     */
    SINGLE,

    /**
     * Any number of items.
     */
    MULTIPLE,
}

/**
 * A collapsible: its triggers, and a content shown or hidden when a trigger fires.
 *
 * @property id the id of this element
 * @property children the triggers and the collapsible contents
 * @property open whether the content is shown
 * @property onChange whether the mod reports every change of the state at once
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CollapsibleElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The trigger part of a collapsible: an action of a widget inside it shows or hides the content
 * instead of reaching the server.
 *
 * @property id the id of this element
 * @property children the widgets that toggle the content, usually one button
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CollapsibleTriggerElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a collapsible, shown while the collapsible is open.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CollapsibleContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An accordion: stacked items, each with a trigger that shows or hides its content.
 *
 * @property id the id of this element
 * @property children the accordion items
 * @property type how many items can be open at once
 * @property collapsible whether the open item of a single accordion can be closed
 * @property value the values of the open items
 * @property onChange whether the mod reports every change of the state at once
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AccordionElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val type: AccordionType = AccordionType.SINGLE,
    val collapsible: Boolean = false,
    val value: List<String> = emptyList(),
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An item of an accordion: its trigger and its content, with a border below.
 *
 * @property id the id of this element
 * @property children the trigger and the content
 * @property value the value that identifies the item in the accordion
 * @property enabled whether the item can be opened and closed
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AccordionItemElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val value: String = "",
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The trigger of an accordion item: its text and a chevron that turns while the item is open.
 *
 * @property id the id of this element
 * @property text the text
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AccordionTriggerElement(
    override val id: String,
    val text: Component = Component.empty(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The content of an accordion item, shown while the item is open.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AccordionContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * How a tab list is drawn.
 */
enum class TabsVariant {
    /**
     * A muted pill in which the active trigger is raised.
     */
    DEFAULT,

    /**
     * Plain triggers with a line under the active one.
     */
    LINE,
}

/**
 * Tabs: a tab list and contents, of which only the content of the selected tab is shown.
 *
 * @property id the id of this element
 * @property children one tab list and the tab contents
 * @property value the value of the selected tab
 * @property orientation whether the triggers are in a row above the contents or in a column
 *           beside them
 * @property onChange whether the mod reports every change of the state at once
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TabsElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val value: String = "",
    val orientation: Orientation = Orientation.HORIZONTAL,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The list of the triggers of tabs.
 *
 * @property id the id of this element
 * @property children the tab triggers
 * @property variant how the list is drawn
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TabsListElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val variant: TabsVariant = TabsVariant.DEFAULT,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A trigger of tabs: selecting it shows the content with the same value.
 *
 * @property id the id of this element
 * @property value the value of the tab
 * @property text the text
 * @property icon the Lucide name of an icon before the text, or null for none
 * @property enabled whether the tab can be selected
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TabsTriggerElement(
    override val id: String,
    val value: String = "",
    val text: Component = Component.empty(),
    val icon: String? = null,
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The content of a tab, shown while its tab is selected.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property value the value of the tab it belongs to
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TabsContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val value: String = "",
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A breadcrumb: the path to the current page.
 *
 * @property id the id of this element
 * @property children one breadcrumb list
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class BreadcrumbElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The list of a breadcrumb: its items and separators in a row, in muted text.
 *
 * @property id the id of this element
 * @property children the items and separators
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class BreadcrumbListElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An item of a breadcrumb: a link, the current page or an ellipsis.
 *
 * @property id the id of this element
 * @property children the link, page or ellipsis
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class BreadcrumbItemElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A link of a breadcrumb: muted text that turns to the foreground colour when hovered and fires an
 * action when clicked.
 *
 * @property id the id of this element
 * @property text the text
 * @property enabled whether the link can be clicked
 * @property onClick the handler run when the player clicks the link, or null for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class BreadcrumbLinkElement(
    override val id: String,
    val text: Component = Component.empty(),
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The current page of a breadcrumb, in the foreground colour and not clickable.
 *
 * @property id the id of this element
 * @property text the text
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class BreadcrumbPageElement(
    override val id: String,
    val text: Component = Component.empty(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A separator between breadcrumb items: a chevron or another icon.
 *
 * @property id the id of this element
 * @property icon the Lucide name of the icon
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class BreadcrumbSeparatorElement(
    override val id: String,
    val icon: String = "chevron-right",
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * An ellipsis that stands for collapsed breadcrumb items, often the trigger of a dropdown menu.
 *
 * @property id the id of this element
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class BreadcrumbEllipsisElement(
    override val id: String,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A pagination: page links centered across the available width.
 *
 * @property id the id of this element
 * @property children one pagination content
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PaginationElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The row of the items of a pagination.
 *
 * @property id the id of this element
 * @property children the pagination items
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PaginationContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An item of a pagination: a link, a previous or next link, or an ellipsis.
 *
 * @property id the id of this element
 * @property children the link or ellipsis
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PaginationItemElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A page link of a pagination, outlined while it is the current page, that fires an action when
 * clicked.
 *
 * @property id the id of this element
 * @property text the text
 * @property active whether the link is the current page
 * @property enabled whether the link can be clicked
 * @property onClick the handler run when the player clicks the link, or null for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PaginationLinkElement(
    override val id: String,
    val text: Component = Component.empty(),
    val active: Boolean = false,
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The link to the previous page: a chevron and its text.
 *
 * @property id the id of this element
 * @property text the text
 * @property enabled whether the link can be clicked
 * @property onClick the handler run when the player clicks the link, or null for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PaginationPreviousElement(
    override val id: String,
    val text: Component = Component.empty(),
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The link to the next page: its text and a chevron.
 *
 * @property id the id of this element
 * @property text the text
 * @property enabled whether the link can be clicked
 * @property onClick the handler run when the player clicks the link, or null for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PaginationNextElement(
    override val id: String,
    val text: Component = Component.empty(),
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * An ellipsis that stands for pages without a link.
 *
 * @property id the id of this element
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PaginationEllipsisElement(
    override val id: String,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The directions a scroll area scrolls in.
 */
enum class ScrollOrientation {
    /**
     * Up and down; the content is as wide as the area.
     */
    VERTICAL,

    /**
     * Left and right; the content is as tall as the area.
     */
    HORIZONTAL,

    /**
     * In both directions.
     */
    BOTH,
}

/**
 * A scroll area: its content, stacked, scrolled inside the area with the wheel or by dragging thin
 * scroll bars.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property orientation the directions the area scrolls in
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ScrollAreaElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val orientation: ScrollOrientation = ScrollOrientation.VERTICAL,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A group of panels side by side or stacked, with handles between them that resize the panels on
 * both sides.
 *
 * @property id the id of this element
 * @property children the panels, with a handle between every two
 * @property orientation whether the panels are side by side or stacked
 * @property onChange the handler run after the player resized the panels, with the shares in
 *           percent, comma separated, or `null` for none
 * @property sizes the shares of the panels in percent as the server holds them, or empty for the
 *           default sizes of the panels
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ResizablePanelGroupElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val orientation: Orientation = Orientation.HORIZONTAL,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
    val sizes: List<Double> = emptyList(),
) : ContainerElement

/**
 * A panel of a resizable group, taking a share of the group in percent.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property defaultSize the share of the group the panel takes at first, in percent, or 0 to
 *           share what the other panels leave
 * @property minSize the smallest share the panel can take, in percent
 * @property maxSize the largest share the panel can take, in percent
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ResizablePanelElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val defaultSize: Double = 0.0,
    val minSize: Double = 0.0,
    val maxSize: Double = 100.0,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A handle between two panels of a resizable group: a thin line, optionally with a grip, that is
 * dragged or moved with the arrow keys.
 *
 * @property id the id of this element
 * @property withHandle whether a grip is drawn on the line
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ResizableHandleElement(
    override val id: String,
    val withHandle: Boolean = false,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A carousel: a content of slides with buttons to the previous and next slide.
 *
 * @property id the id of this element
 * @property children one carousel content and the previous and next buttons
 * @property orientation whether the slides move sideways or up and down
 * @property loop whether the last slide is followed by the first
 * @property index the index of the first shown slide
 * @property onChange whether the mod reports every change of the shown slide at once
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CarouselElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val orientation: Orientation = Orientation.HORIZONTAL,
    val loop: Boolean = false,
    val index: Int = 0,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a carousel: its slides in a row or a column, of which the part from the current
 * slide on is shown.
 *
 * @property id the id of this element
 * @property children the carousel items
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CarouselContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A slide of a carousel, taking a share of the content.
 *
 * @property id the id of this element
 * @property children the content of the slide, stacked
 * @property basis the share of the content the slide takes, in percent
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CarouselItemElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val basis: Double = 100.0,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The button that shows the previous slide of a carousel: a round outline button with an arrow.
 *
 * @property id the id of this element
 * @property enabled whether the button can be used
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CarouselPreviousElement(
    override val id: String,
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The button that shows the next slide of a carousel: a round outline button with an arrow.
 *
 * @property id the id of this element
 * @property enabled whether the button can be used
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CarouselNextElement(
    override val id: String,
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A navigation menu: a list of items whose triggers open content below them, and links.
 *
 * @property id the id of this element
 * @property children one navigation menu list
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class NavigationMenuElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The row of the items of a navigation menu.
 *
 * @property id the id of this element
 * @property children the navigation menu items
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class NavigationMenuListElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An item of a navigation menu: a trigger with a content that opens below it on hover or click, or
 * a link.
 *
 * @property id the id of this element
 * @property children a trigger and a content, or a link
 * @property open whether the content is open
 * @property onChange whether the mod reports every opening and closing at once
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class NavigationMenuItemElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The trigger of a navigation menu item: its text and a chevron that turns while the content is
 * open.
 *
 * @property id the id of this element
 * @property text the text
 * @property enabled whether the trigger can open the content
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class NavigationMenuTriggerElement(
    override val id: String,
    val text: Component = Component.empty(),
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The content of a navigation menu item, shown on a surface below its trigger while open.
 *
 * @property id the id of this element
 * @property children the links and other content
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class NavigationMenuContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A link of a navigation menu: its content, highlighted when hovered or active, that fires an
 * action when clicked. Directly in an item it is drawn like a trigger.
 *
 * @property id the id of this element
 * @property children the content of the link, such as a title and a description
 * @property active whether the link leads to the current page
 * @property enabled whether the link can be clicked
 * @property onClick the handler run when the player clicks the link, or null for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class NavigationMenuLinkElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val active: Boolean = false,
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The side of the screen a sidebar is on.
 */
enum class SidebarSide {
    /**
     * The left side.
     */
    LEFT,

    /**
     * The right side.
     */
    RIGHT,
}

/**
 * How a sidebar is drawn.
 */
enum class SidebarVariant {
    /**
     * A panel along the edge with a border to the content.
     */
    SIDEBAR,

    /**
     * A card with a margin, floating beside the content.
     */
    FLOATING,

    /**
     * A panel without a border, with the content drawn as a card beside it.
     */
    INSET,
}

/**
 * How a sidebar collapses.
 */
enum class SidebarCollapsible {
    /**
     * It disappears completely.
     */
    OFFCANVAS,

    /**
     * It shrinks to the icons of its menu buttons.
     */
    ICON,

    /**
     * It cannot collapse.
     */
    NONE,
}

/**
 * The size of a sidebar menu button.
 */
enum class SidebarMenuButtonSize {
    /**
     * The regular height.
     */
    DEFAULT,

    /**
     * A lower height.
     */
    SM,

    /**
     * A larger height.
     */
    LG,
}

/**
 * How a sidebar menu button is drawn.
 */
enum class SidebarMenuButtonVariant {
    /**
     * Plain, highlighted when hovered.
     */
    DEFAULT,

    /**
     * On the background colour with a border.
     */
    OUTLINE,
}

/**
 * The size of the text of a sidebar sub-menu button.
 */
enum class SidebarMenuSubButtonSize {
    /**
     * Smaller text.
     */
    SM,

    /**
     * Regular text.
     */
    MD,
}

/**
 * The frame of a sidebar layout: a sidebar and the inset beside it, with the expanded state of the
 * sidebar.
 *
 * @property id the id of this element
 * @property children one sidebar and one sidebar inset
 * @property open whether the sidebar is expanded
 * @property onChange whether the mod reports every expanding and collapsing at once
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarProviderElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = true,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A sidebar: a column of header, content and footer on one side of its provider.
 *
 * @property id the id of this element
 * @property children the header, content, footer and rail
 * @property side the side the sidebar is on
 * @property variant how the sidebar is drawn
 * @property collapsible how the sidebar collapses
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val side: SidebarSide = SidebarSide.LEFT,
    val variant: SidebarVariant = SidebarVariant.SIDEBAR,
    val collapsible: SidebarCollapsible = SidebarCollapsible.OFFCANVAS,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The main content beside a sidebar, taking the rest of the provider.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarInsetElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The header of a sidebar, above its content.
 *
 * @property id the id of this element
 * @property children the header content, stacked
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarHeaderElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The footer of a sidebar, below its content.
 *
 * @property id the id of this element
 * @property children the footer content, stacked
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarFooterElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The scrolling content of a sidebar between its header and footer.
 *
 * @property id the id of this element
 * @property children the sidebar groups
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A group of a sidebar: a label, an optional action and a content.
 *
 * @property id the id of this element
 * @property children the label, action and content
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarGroupElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The label of a sidebar group, in muted text; hidden while the sidebar is collapsed to icons.
 *
 * @property id the id of this element
 * @property text the text
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarGroupLabelElement(
    override val id: String,
    val text: Component = Component.empty(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A small icon button at the end of a sidebar group label.
 *
 * @property id the id of this element
 * @property icon the Lucide name of the icon
 * @property enabled whether the button can be clicked
 * @property onClick the handler run when the player clicks the button, or null for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarGroupActionElement(
    override val id: String,
    val icon: String = "plus",
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The content of a sidebar group.
 *
 * @property id the id of this element
 * @property children the content, usually one sidebar menu
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarGroupContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A menu of a sidebar: its items, stacked.
 *
 * @property id the id of this element
 * @property children the sidebar menu items
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarMenuElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An item of a sidebar menu: a button with an optional action and badge at its end, and an optional
 * sub-menu below.
 *
 * @property id the id of this element
 * @property children the button, action, badge and sub-menu
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarMenuItemElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The button of a sidebar menu item: its icon and text, highlighted while active; only the icon
 * while the sidebar is collapsed to icons, with the tooltip on hover.
 *
 * @property id the id of this element
 * @property text the text
 * @property icon the Lucide name of an icon before the text, or null for none
 * @property size the size of the button
 * @property variant how the button is drawn
 * @property active whether the button leads to the current page
 * @property tooltip the text shown beside the button while the sidebar is collapsed to icons, or
 *           empty for none
 * @property enabled whether the button can be clicked
 * @property onClick the handler run when the player clicks the button, or null for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarMenuButtonElement(
    override val id: String,
    val text: Component = Component.empty(),
    val icon: String? = null,
    val size: SidebarMenuButtonSize = SidebarMenuButtonSize.DEFAULT,
    val variant: SidebarMenuButtonVariant = SidebarMenuButtonVariant.DEFAULT,
    val active: Boolean = false,
    val tooltip: Component? = null,
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A small icon button at the end of a sidebar menu button.
 *
 * @property id the id of this element
 * @property icon the Lucide name of the icon
 * @property showOnHover whether the action is shown only while its item is hovered or focused
 * @property enabled whether the button can be clicked
 * @property onClick the handler run when the player clicks the button, or null for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarMenuActionElement(
    override val id: String,
    val icon: String = "ellipsis",
    val showOnHover: Boolean = false,
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A small count or label at the end of a sidebar menu button.
 *
 * @property id the id of this element
 * @property text the text
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarMenuBadgeElement(
    override val id: String,
    val text: Component = Component.empty(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A placeholder for a sidebar menu button that is still loading.
 *
 * @property id the id of this element
 * @property showIcon whether a placeholder for the icon is drawn
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarMenuSkeletonElement(
    override val id: String,
    val showIcon: Boolean = false,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A sub-menu below a sidebar menu button, indented with a line; hidden while the sidebar is
 * collapsed to icons.
 *
 * @property id the id of this element
 * @property children the sub-menu items
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarMenuSubElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An item of a sidebar sub-menu.
 *
 * @property id the id of this element
 * @property children the sub-menu button
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarMenuSubItemElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The button of a sidebar sub-menu item.
 *
 * @property id the id of this element
 * @property text the text
 * @property icon the Lucide name of an icon before the text, or null for none
 * @property size the size of the text
 * @property active whether the button leads to the current page
 * @property enabled whether the button can be clicked
 * @property onClick the handler run when the player clicks the button, or null for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarMenuSubButtonElement(
    override val id: String,
    val text: Component = Component.empty(),
    val icon: String? = null,
    val size: SidebarMenuSubButtonSize = SidebarMenuSubButtonSize.MD,
    val active: Boolean = false,
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A small ghost button that expands and collapses the sidebar of its provider.
 *
 * @property id the id of this element
 * @property enabled whether the trigger can be used
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarTriggerElement(
    override val id: String,
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A thin strip along the inner edge of a sidebar that expands and collapses it when clicked.
 *
 * @property id the id of this element
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SidebarRailElement(
    override val id: String,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The direction a subtree is laid out in.
 */
enum class LayoutDirection {
    /**
     * Left to right: rows start at the left.
     */
    LTR,

    /**
     * Right to left: rows start at the right, start and end alignment swap, and components with a
     * side use the mirrored side. Text is still drawn left to right.
     */
    RTL,
}

/**
 * Sets the layout direction of its children, stacked. A nested direction sets the direction of
 * its own subtree.
 *
 * @property id the id of this element
 * @property children the content, stacked
 * @property direction the direction of the content
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DirectionElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val direction: LayoutDirection = LayoutDirection.LTR,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement
