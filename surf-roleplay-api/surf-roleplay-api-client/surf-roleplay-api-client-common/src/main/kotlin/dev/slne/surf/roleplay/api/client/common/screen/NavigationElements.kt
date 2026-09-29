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
 * Builds the children of a container element.
 *
 * @param children the builder of the children
 * @return the built children
 */
private fun build(children: ElementsBuilder.() -> Unit): List<ScreenElement> = ElementsBuilder().apply(children).elements.toList()

/**
 * Adds a collapsible. Its children are [collapsibleTrigger]s and [collapsibleContent]s.
 *
 * @param id the id of the collapsible
 * @param open whether the content is shown at first
 * @param onChange the handler run whenever the player shows or hides the content, or `null` for
 *        none
 * @param children the builder of the triggers and contents
 */
fun ElementsBuilder.collapsible(id: String, open: Boolean = false, onChange: ChangeHandler? = null, children: ElementsBuilder.() -> Unit) {
    elements += CollapsibleElement(id, build(children), open, onChange)
}

/**
 * Adds the trigger part of a collapsible: an action of a widget inside it shows or hides the
 * content instead of reaching the server.
 *
 * @param id the id of the trigger
 * @param children the builder of the widgets that toggle the content, usually one button
 */
fun ElementsBuilder.collapsibleTrigger(id: String, children: ElementsBuilder.() -> Unit) {
    elements += CollapsibleTriggerElement(id, build(children))
}

/**
 * Adds the content of a collapsible, shown while the collapsible is open.
 *
 * @param id the id of the content
 * @param children the builder of the content
 */
fun ElementsBuilder.collapsibleContent(id: String, children: ElementsBuilder.() -> Unit) {
    elements += CollapsibleContentElement(id, build(children))
}

/**
 * Adds an accordion. Its children are [accordionItem]s.
 *
 * @param id the id of the accordion
 * @param type how many items can be open at once
 * @param collapsible whether the open item of a single accordion can be closed
 * @param value the values of the items open at first
 * @param onChange the handler run whenever the player opens or closes an item, with the values of
 *        the open items, comma separated, or `null` for none
 * @param children the builder of the items
 */
fun ElementsBuilder.accordion(
    id: String,
    type: AccordionType = AccordionType.SINGLE,
    collapsible: Boolean = false,
    value: List<String> = emptyList(),
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    elements += AccordionElement(id, build(children), type, collapsible, value, onChange)
}

/**
 * Adds an item of an accordion. Its children are an [accordionTrigger] and an
 * [accordionContent].
 *
 * @param id the id of the item
 * @param value the value that identifies the item in the accordion; it must not contain a comma
 * @param enabled whether the item can be opened and closed
 * @param children the builder of the trigger and the content
 */
fun ElementsBuilder.accordionItem(id: String, value: String, enabled: Boolean = true, children: ElementsBuilder.() -> Unit) {
    require(',' !in value) { "Accordion item values must not contain a comma: $value" }
    elements += AccordionItemElement(id, build(children), value, enabled)
}

/**
 * Adds the trigger of an accordion item: its text and a chevron that turns while the item is
 * open.
 *
 * @param id the id of the trigger
 * @param text the text
 */
fun ElementsBuilder.accordionTrigger(id: String, text: Component) {
    elements += AccordionTriggerElement(id, text)
}

/**
 * Adds the content of an accordion item, shown while the item is open.
 *
 * @param id the id of the content
 * @param children the builder of the content
 */
fun ElementsBuilder.accordionContent(id: String, children: ElementsBuilder.() -> Unit) {
    elements += AccordionContentElement(id, build(children))
}

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
 * Adds tabs. Their children are one [tabsList] and a [tabsContent] per tab.
 *
 * @param id the id of the tabs
 * @param value the value of the tab selected at first; empty selects the first enabled tab
 * @param orientation whether the triggers are in a row above the contents or in a column beside
 *        them
 * @param onChange the handler run whenever the player selects another tab, with its value, or
 *        `null` for none
 * @param children the builder of the list and the contents
 */
fun ElementsBuilder.tabs(
    id: String,
    value: String = "",
    orientation: Orientation = Orientation.HORIZONTAL,
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    elements += TabsElement(id, build(children), value, orientation, onChange)
}

/**
 * Adds the list of the triggers of tabs. Its children are [tabsTrigger]s.
 *
 * @param id the id of the list
 * @param variant how the list is drawn
 * @param children the builder of the triggers
 */
fun ElementsBuilder.tabsList(id: String, variant: TabsVariant = TabsVariant.DEFAULT, children: ElementsBuilder.() -> Unit) {
    elements += TabsListElement(id, build(children), variant)
}

/**
 * Adds a trigger of tabs.
 *
 * @param id the id of the trigger
 * @param value the value of the tab
 * @param text the text
 * @param icon the Lucide name of an icon before the text, or `null` for none
 * @param enabled whether the tab can be selected
 */
fun ElementsBuilder.tabsTrigger(id: String, value: String, text: Component, icon: String? = null, enabled: Boolean = true) {
    elements += TabsTriggerElement(id, value, text, icon, enabled)
}

/**
 * Adds the content of a tab, shown while its tab is selected.
 *
 * @param id the id of the content
 * @param value the value of the tab it belongs to
 * @param children the builder of the content
 */
fun ElementsBuilder.tabsContent(id: String, value: String, children: ElementsBuilder.() -> Unit) {
    elements += TabsContentElement(id, build(children), value)
}

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
 * Adds a breadcrumb: the path to the current page. Its child is one [breadcrumbList].
 *
 * @param id the id of the breadcrumb
 * @param children the builder of the list
 */
fun ElementsBuilder.breadcrumb(id: String, children: ElementsBuilder.() -> Unit) {
    elements += BreadcrumbElement(id, build(children))
}

/**
 * Adds the list of a breadcrumb. Its children are [breadcrumbItem]s and [breadcrumbSeparator]s.
 *
 * @param id the id of the list
 * @param children the builder of the items and separators
 */
fun ElementsBuilder.breadcrumbList(id: String, children: ElementsBuilder.() -> Unit) {
    elements += BreadcrumbListElement(id, build(children))
}

/**
 * Adds an item of a breadcrumb, holding a [breadcrumbLink], a [breadcrumbPage] or a
 * [breadcrumbEllipsis].
 *
 * @param id the id of the item
 * @param children the builder of the link, page or ellipsis
 */
fun ElementsBuilder.breadcrumbItem(id: String, children: ElementsBuilder.() -> Unit) {
    elements += BreadcrumbItemElement(id, build(children))
}

/**
 * Adds a link of a breadcrumb.
 *
 * @param id the id of the link
 * @param text the text
 * @param enabled whether the link can be clicked
 * @param onClick the handler run when the player clicks the link, or `null` for none
 */
fun ElementsBuilder.breadcrumbLink(id: String, text: Component, enabled: Boolean = true, onClick: ButtonHandler? = null) {
    elements += BreadcrumbLinkElement(id, text, enabled, onClick)
}

/**
 * Adds the current page of a breadcrumb.
 *
 * @param id the id of the page
 * @param text the text
 */
fun ElementsBuilder.breadcrumbPage(id: String, text: Component) {
    elements += BreadcrumbPageElement(id, text)
}

/**
 * Adds a separator between breadcrumb items.
 *
 * @param id the id of the separator
 * @param icon the Lucide name of the icon
 */
fun ElementsBuilder.breadcrumbSeparator(id: String, icon: String = "chevron-right") {
    elements += BreadcrumbSeparatorElement(id, icon)
}

/**
 * Adds an ellipsis that stands for collapsed breadcrumb items. As the trigger of a
 * [dropdownMenu] it opens the menu.
 *
 * @param id the id of the ellipsis
 */
fun ElementsBuilder.breadcrumbEllipsis(id: String) {
    elements += BreadcrumbEllipsisElement(id)
}

/**
 * Adds a pagination, centered across the available width. Its child is one
 * [paginationContent].
 *
 * @param id the id of the pagination
 * @param children the builder of the content
 */
fun ElementsBuilder.pagination(id: String, children: ElementsBuilder.() -> Unit) {
    elements += PaginationElement(id, build(children), ElementSize.grow())
}

/**
 * Adds the row of the items of a pagination. Its children are [paginationItem]s.
 *
 * @param id the id of the content
 * @param children the builder of the items
 */
fun ElementsBuilder.paginationContent(id: String, children: ElementsBuilder.() -> Unit) {
    elements += PaginationContentElement(id, build(children))
}

/**
 * Adds an item of a pagination, holding a link or an ellipsis.
 *
 * @param id the id of the item
 * @param children the builder of the link or ellipsis
 */
fun ElementsBuilder.paginationItem(id: String, children: ElementsBuilder.() -> Unit) {
    elements += PaginationItemElement(id, build(children))
}

/**
 * Adds a page link of a pagination.
 *
 * @param id the id of the link
 * @param text the text, usually the page number
 * @param active whether the link is the current page
 * @param enabled whether the link can be clicked
 * @param onClick the handler run when the player clicks the link, or `null` for none
 */
fun ElementsBuilder.paginationLink(id: String, text: Component, active: Boolean = false, enabled: Boolean = true, onClick: ButtonHandler? = null) {
    elements += PaginationLinkElement(id, text, active, enabled, onClick)
}

/**
 * Adds the link to the previous page of a pagination.
 *
 * @param id the id of the link
 * @param text the text
 * @param enabled whether the link can be clicked
 * @param onClick the handler run when the player clicks the link, or `null` for none
 */
fun ElementsBuilder.paginationPrevious(id: String, text: Component = Component.text("Zurück"), enabled: Boolean = true, onClick: ButtonHandler? = null) {
    elements += PaginationPreviousElement(id, text, enabled, onClick)
}

/**
 * Adds the link to the next page of a pagination.
 *
 * @param id the id of the link
 * @param text the text
 * @param enabled whether the link can be clicked
 * @param onClick the handler run when the player clicks the link, or `null` for none
 */
fun ElementsBuilder.paginationNext(id: String, text: Component = Component.text("Weiter"), enabled: Boolean = true, onClick: ButtonHandler? = null) {
    elements += PaginationNextElement(id, text, enabled, onClick)
}

/**
 * Adds an ellipsis that stands for pages without a link.
 *
 * @param id the id of the ellipsis
 */
fun ElementsBuilder.paginationEllipsis(id: String) {
    elements += PaginationEllipsisElement(id)
}

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
 * Adds a scroll area: its content, stacked, scrolled inside the area with the wheel or by
 * dragging thin scroll bars.
 *
 * @param id the id of the area
 * @param width how wide the area is laid out, usually fixed
 * @param height how tall the area is laid out, usually fixed
 * @param orientation the directions the area scrolls in
 * @param children the builder of the content
 */
fun ElementsBuilder.scrollArea(
    id: String,
    width: ElementSize,
    height: ElementSize,
    orientation: ScrollOrientation = ScrollOrientation.VERTICAL,
    children: ElementsBuilder.() -> Unit,
) {
    elements += ScrollAreaElement(id, build(children), orientation, width, height)
}


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
 * Adds a resizable panel group. Its children are [resizablePanel]s with a [resizableHandle]
 * between every two.
 *
 * @param id the id of the group
 * @param orientation whether the panels are side by side or stacked
 * @param width how wide the group is laid out
 * @param height how tall the group is laid out
 * @param onChange the handler run after the player resized the panels, with the shares in
 *        percent, comma separated, or `null` for none
 * @param children the builder of the panels and handles
 */
fun ElementsBuilder.resizablePanelGroup(
    id: String,
    orientation: Orientation = Orientation.HORIZONTAL,
    width: ElementSize = ElementSize.grow(),
    height: ElementSize = ElementSize.FIT,
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    elements += ResizablePanelGroupElement(id, build(children), orientation, onChange, width, height)
}

/**
 * Adds a panel of a resizable group.
 *
 * @param id the id of the panel
 * @param defaultSize the share of the group the panel takes at first, in percent, or 0 to share
 *        what the other panels leave
 * @param minSize the smallest share the panel can take, in percent
 * @param maxSize the largest share the panel can take, in percent
 * @param children the builder of the content
 */
fun ElementsBuilder.resizablePanel(id: String, defaultSize: Double = 0.0, minSize: Double = 0.0, maxSize: Double = 100.0, children: ElementsBuilder.() -> Unit) {
    elements += ResizablePanelElement(id, build(children), defaultSize, minSize, maxSize)
}

/**
 * Adds a handle between two panels of a resizable group.
 *
 * @param id the id of the handle
 * @param withHandle whether a grip is drawn on the line
 */
fun ElementsBuilder.resizableHandle(id: String, withHandle: Boolean = false) {
    elements += ResizableHandleElement(id, withHandle)
}

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
 * Adds a carousel. Its children are one [carouselContent], a [carouselPrevious] and a
 * [carouselNext].
 *
 * @param id the id of the carousel
 * @param width how wide the carousel is laid out, with its buttons
 * @param orientation whether the slides move sideways or up and down
 * @param loop whether the last slide is followed by the first
 * @param index the index of the slide shown first
 * @param height how tall the carousel is laid out; a vertical carousel needs a fixed height
 * @param onChange the handler run whenever the player shows another slide, with its index, or
 *        `null` for none
 * @param children the builder of the content and the buttons
 */
fun ElementsBuilder.carousel(
    id: String,
    width: ElementSize,
    orientation: Orientation = Orientation.HORIZONTAL,
    loop: Boolean = false,
    index: Int = 0,
    height: ElementSize = ElementSize.FIT,
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    elements += CarouselElement(id, build(children), orientation, loop, index, onChange, width, height)
}

/**
 * Adds the content of a carousel. Its children are [carouselItem]s.
 *
 * @param id the id of the content
 * @param children the builder of the slides
 */
fun ElementsBuilder.carouselContent(id: String, children: ElementsBuilder.() -> Unit) {
    elements += CarouselContentElement(id, build(children))
}

/**
 * Adds a slide of a carousel.
 *
 * @param id the id of the slide
 * @param basis the share of the content the slide takes, in percent
 * @param children the builder of the content of the slide
 */
fun ElementsBuilder.carouselItem(id: String, basis: Double = 100.0, children: ElementsBuilder.() -> Unit) {
    elements += CarouselItemElement(id, build(children), basis)
}

/**
 * Adds the button that shows the previous slide of a carousel.
 *
 * @param id the id of the button
 * @param enabled whether the button can be used
 */
fun ElementsBuilder.carouselPrevious(id: String, enabled: Boolean = true) {
    elements += CarouselPreviousElement(id, enabled)
}

/**
 * Adds the button that shows the next slide of a carousel.
 *
 * @param id the id of the button
 * @param enabled whether the button can be used
 */
fun ElementsBuilder.carouselNext(id: String, enabled: Boolean = true) {
    elements += CarouselNextElement(id, enabled)
}

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
 * Adds a navigation menu. Its child is one [navigationMenuList].
 *
 * @param id the id of the menu
 * @param children the builder of the list
 */
fun ElementsBuilder.navigationMenu(id: String, children: ElementsBuilder.() -> Unit) {
    elements += NavigationMenuElement(id, build(children))
}

/**
 * Adds the row of the items of a navigation menu. Its children are [navigationMenuItem]s.
 *
 * @param id the id of the list
 * @param children the builder of the items
 */
fun ElementsBuilder.navigationMenuList(id: String, children: ElementsBuilder.() -> Unit) {
    elements += NavigationMenuListElement(id, build(children))
}

/**
 * Adds an item of a navigation menu: a [navigationMenuTrigger] with a [navigationMenuContent],
 * or a [navigationMenuLink].
 *
 * @param id the id of the item
 * @param onChange the handler run whenever the player opens or closes the content, or `null`
 *        for none
 * @param children the builder of the trigger and content, or of the link
 */
fun ElementsBuilder.navigationMenuItem(id: String, onChange: ChangeHandler? = null, children: ElementsBuilder.() -> Unit) {
    elements += NavigationMenuItemElement(id, build(children), false, onChange)
}

/**
 * Adds the trigger of a navigation menu item.
 *
 * @param id the id of the trigger
 * @param text the text
 * @param enabled whether the trigger can open the content
 */
fun ElementsBuilder.navigationMenuTrigger(id: String, text: Component, enabled: Boolean = true) {
    elements += NavigationMenuTriggerElement(id, text, enabled)
}

/**
 * Adds the content of a navigation menu item, shown below its trigger while open.
 *
 * @param id the id of the content
 * @param children the builder of the links and other content
 */
fun ElementsBuilder.navigationMenuContent(id: String, children: ElementsBuilder.() -> Unit) {
    elements += NavigationMenuContentElement(id, build(children))
}

/**
 * Adds a link of a navigation menu. Its children are its content, such as a title and a
 * description.
 *
 * @param id the id of the link
 * @param active whether the link leads to the current page
 * @param enabled whether the link can be clicked
 * @param onClick the handler run when the player clicks the link, or `null` for none
 * @param children the builder of the content
 */
fun ElementsBuilder.navigationMenuLink(id: String, active: Boolean = false, enabled: Boolean = true, onClick: ButtonHandler? = null, children: ElementsBuilder.() -> Unit) {
    elements += NavigationMenuLinkElement(id, build(children), active, enabled, onClick)
}


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
 * Adds the frame of a sidebar layout. Its children are one [sidebar] and one [sidebarInset].
 *
 * @param id the id of the provider
 * @param width how wide the provider is laid out
 * @param height how tall the provider is laid out
 * @param open whether the sidebar is expanded at first
 * @param onChange the handler run whenever the player expands or collapses the sidebar, with
 *        `true` or `false`, or `null` for none
 * @param children the builder of the sidebar and inset
 */
fun ElementsBuilder.sidebarProvider(
    id: String,
    width: ElementSize = ElementSize.grow(),
    height: ElementSize = ElementSize.FIT,
    open: Boolean = true,
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    elements += SidebarProviderElement(id, build(children), open, onChange, width, height)
}

/**
 * Adds a sidebar. Its children are a [sidebarHeader], a [sidebarContent], a [sidebarFooter] and
 * an optional [sidebarRail].
 *
 * @param id the id of the sidebar
 * @param side the side the sidebar is on
 * @param variant how the sidebar is drawn
 * @param collapsible how the sidebar collapses
 * @param children the builder of the parts
 */
fun ElementsBuilder.sidebar(
    id: String,
    side: SidebarSide = SidebarSide.LEFT,
    variant: SidebarVariant = SidebarVariant.SIDEBAR,
    collapsible: SidebarCollapsible = SidebarCollapsible.OFFCANVAS,
    children: ElementsBuilder.() -> Unit,
) {
    elements += SidebarElement(id, build(children), side, variant, collapsible)
}

/**
 * Adds the main content beside a sidebar.
 *
 * @param id the id of the inset
 * @param children the builder of the content
 */
fun ElementsBuilder.sidebarInset(id: String, children: ElementsBuilder.() -> Unit) {
    elements += SidebarInsetElement(id, build(children))
}

/**
 * Adds the header of a sidebar.
 *
 * @param id the id of the header
 * @param children the builder of the header content
 */
fun ElementsBuilder.sidebarHeader(id: String, children: ElementsBuilder.() -> Unit) {
    elements += SidebarHeaderElement(id, build(children))
}

/**
 * Adds the footer of a sidebar.
 *
 * @param id the id of the footer
 * @param children the builder of the footer content
 */
fun ElementsBuilder.sidebarFooter(id: String, children: ElementsBuilder.() -> Unit) {
    elements += SidebarFooterElement(id, build(children))
}

/**
 * Adds the scrolling content of a sidebar. Its children are [sidebarGroup]s.
 *
 * @param id the id of the content
 * @param children the builder of the groups
 */
fun ElementsBuilder.sidebarContent(id: String, children: ElementsBuilder.() -> Unit) {
    elements += SidebarContentElement(id, build(children))
}

/**
 * Adds a group of a sidebar: a [sidebarGroupLabel], an optional [sidebarGroupAction] and a
 * [sidebarGroupContent].
 *
 * @param id the id of the group
 * @param children the builder of the label, action and content
 */
fun ElementsBuilder.sidebarGroup(id: String, children: ElementsBuilder.() -> Unit) {
    elements += SidebarGroupElement(id, build(children))
}

/**
 * Adds the label of a sidebar group.
 *
 * @param id the id of the label
 * @param text the text
 */
fun ElementsBuilder.sidebarGroupLabel(id: String, text: Component) {
    elements += SidebarGroupLabelElement(id, text)
}

/**
 * Adds a small icon button at the end of a sidebar group label.
 *
 * @param id the id of the button
 * @param icon the Lucide name of the icon
 * @param enabled whether the button can be clicked
 * @param onClick the handler run when the player clicks the button, or `null` for none
 */
fun ElementsBuilder.sidebarGroupAction(id: String, icon: String = "plus", enabled: Boolean = true, onClick: ButtonHandler? = null) {
    elements += SidebarGroupActionElement(id, icon, enabled, onClick)
}

/**
 * Adds the content of a sidebar group, usually one [sidebarMenu].
 *
 * @param id the id of the content
 * @param children the builder of the content
 */
fun ElementsBuilder.sidebarGroupContent(id: String, children: ElementsBuilder.() -> Unit) {
    elements += SidebarGroupContentElement(id, build(children))
}

/**
 * Adds a menu of a sidebar. Its children are [sidebarMenuItem]s.
 *
 * @param id the id of the menu
 * @param children the builder of the items
 */
fun ElementsBuilder.sidebarMenu(id: String, children: ElementsBuilder.() -> Unit) {
    elements += SidebarMenuElement(id, build(children))
}

/**
 * Adds an item of a sidebar menu: a [sidebarMenuButton] or [sidebarMenuSkeleton], with an
 * optional [sidebarMenuAction], [sidebarMenuBadge] and [sidebarMenuSub].
 *
 * @param id the id of the item
 * @param children the builder of the parts
 */
fun ElementsBuilder.sidebarMenuItem(id: String, children: ElementsBuilder.() -> Unit) {
    elements += SidebarMenuItemElement(id, build(children))
}

/**
 * Adds the button of a sidebar menu item.
 *
 * @param id the id of the button
 * @param text the text
 * @param icon the Lucide name of an icon before the text, or `null` for none
 * @param size the size of the button
 * @param variant how the button is drawn
 * @param active whether the button leads to the current page
 * @param tooltip the text shown beside the button while the sidebar is collapsed to icons, or
 *        `null` for none
 * @param enabled whether the button can be clicked
 * @param onClick the handler run when the player clicks the button, or `null` for none
 */
fun ElementsBuilder.sidebarMenuButton(
    id: String,
    text: Component,
    icon: String? = null,
    size: SidebarMenuButtonSize = SidebarMenuButtonSize.DEFAULT,
    variant: SidebarMenuButtonVariant = SidebarMenuButtonVariant.DEFAULT,
    active: Boolean = false,
    tooltip: Component? = null,
    enabled: Boolean = true,
    onClick: ButtonHandler? = null,
) {
    elements += SidebarMenuButtonElement(id, text, icon, size, variant, active, tooltip, enabled, onClick)
}

/**
 * Adds a small icon button at the end of a sidebar menu button.
 *
 * @param id the id of the button
 * @param icon the Lucide name of the icon
 * @param showOnHover whether the action is shown only while its item is hovered or focused
 * @param enabled whether the button can be clicked
 * @param onClick the handler run when the player clicks the button, or `null` for none
 */
fun ElementsBuilder.sidebarMenuAction(id: String, icon: String = "ellipsis", showOnHover: Boolean = false, enabled: Boolean = true, onClick: ButtonHandler? = null) {
    elements += SidebarMenuActionElement(id, icon, showOnHover, enabled, onClick)
}

/**
 * Adds a small count or label at the end of a sidebar menu button.
 *
 * @param id the id of the badge
 * @param text the text
 */
fun ElementsBuilder.sidebarMenuBadge(id: String, text: Component) {
    elements += SidebarMenuBadgeElement(id, text)
}

/**
 * Adds a placeholder for a sidebar menu button that is still loading.
 *
 * @param id the id of the placeholder
 * @param showIcon whether a placeholder for the icon is drawn
 */
fun ElementsBuilder.sidebarMenuSkeleton(id: String, showIcon: Boolean = false) {
    elements += SidebarMenuSkeletonElement(id, showIcon)
}

/**
 * Adds a sub-menu below a sidebar menu button. Its children are [sidebarMenuSubItem]s.
 *
 * @param id the id of the sub-menu
 * @param children the builder of the items
 */
fun ElementsBuilder.sidebarMenuSub(id: String, children: ElementsBuilder.() -> Unit) {
    elements += SidebarMenuSubElement(id, build(children))
}

/**
 * Adds an item of a sidebar sub-menu. Its child is one [sidebarMenuSubButton].
 *
 * @param id the id of the item
 * @param children the builder of the button
 */
fun ElementsBuilder.sidebarMenuSubItem(id: String, children: ElementsBuilder.() -> Unit) {
    elements += SidebarMenuSubItemElement(id, build(children))
}

/**
 * Adds the button of a sidebar sub-menu item.
 *
 * @param id the id of the button
 * @param text the text
 * @param icon the Lucide name of an icon before the text, or `null` for none
 * @param size the size of the text
 * @param active whether the button leads to the current page
 * @param enabled whether the button can be clicked
 * @param onClick the handler run when the player clicks the button, or `null` for none
 */
fun ElementsBuilder.sidebarMenuSubButton(
    id: String,
    text: Component,
    icon: String? = null,
    size: SidebarMenuSubButtonSize = SidebarMenuSubButtonSize.MD,
    active: Boolean = false,
    enabled: Boolean = true,
    onClick: ButtonHandler? = null,
) {
    elements += SidebarMenuSubButtonElement(id, text, icon, size, active, enabled, onClick)
}

/**
 * Adds a small ghost button that expands and collapses the sidebar of the enclosing provider.
 *
 * @param id the id of the trigger
 * @param enabled whether the trigger can be used
 */
fun ElementsBuilder.sidebarTrigger(id: String, enabled: Boolean = true) {
    elements += SidebarTriggerElement(id, enabled)
}

/**
 * Adds a thin strip along the inner edge of a sidebar that expands and collapses it when clicked.
 *
 * @param id the id of the rail
 */
fun ElementsBuilder.sidebarRail(id: String) {
    elements += SidebarRailElement(id)
}

/**
 * Adds a text input that fills the width of a sidebar.
 *
 * @param id the id of the input
 * @param placeholder the hint shown while the input is empty
 */
fun ElementsBuilder.sidebarInput(id: String, placeholder: Component = Component.empty()) {
    textInput(id, placeholder = placeholder, width = ElementSize.grow())
}

/**
 * Adds a horizontal separator between the parts of a sidebar.
 *
 * @param id the id of the separator
 */
fun ElementsBuilder.sidebarSeparator(id: String) {
    separator(id)
}

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

/**
 * Adds a direction: its content, stacked, laid out left to right or mirrored right to left.
 *
 * @param id the id of the direction
 * @param direction the direction of the content
 * @param width how wide the direction is laid out
 * @param height how tall the direction is laid out
 * @param children the builder of the content
 */
fun ElementsBuilder.direction(
    id: String,
    direction: LayoutDirection,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    children: ElementsBuilder.() -> Unit,
) {
    elements += DirectionElement(id, build(children), direction, width, height)
}
