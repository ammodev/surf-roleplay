package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.AccordionContentElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionItemElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionType
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbEllipsisElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbItemElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbLinkElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbListElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbPageElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.CarouselContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselItemElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselNextElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselPreviousElement
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.CollapsibleContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CollapsibleElement
import dev.slne.surf.roleplay.api.client.common.screen.CollapsibleTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.DirectionElement
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.LayoutDirection
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuContentElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuLinkElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuListElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.PaginationContentElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationEllipsisElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationItemElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationLinkElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationNextElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationPreviousElement
import dev.slne.surf.roleplay.api.client.common.screen.ResizableHandleElement
import dev.slne.surf.roleplay.api.client.common.screen.ResizablePanelElement
import dev.slne.surf.roleplay.api.client.common.screen.ResizablePanelGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ScrollAreaElement
import dev.slne.surf.roleplay.api.client.common.screen.ScrollOrientation
import dev.slne.surf.roleplay.api.client.common.screen.SeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarCollapsible
import dev.slne.surf.roleplay.api.client.common.screen.SidebarContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupActionElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupLabelElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarInsetElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuActionElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuBadgeElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSkeletonElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubItemElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarProviderElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarRailElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarSide
import dev.slne.surf.roleplay.api.client.common.screen.SidebarTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarVariant
import dev.slne.surf.roleplay.api.client.common.screen.TabsContentElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsListElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsVariant
import net.kyori.adventure.text.Component

/**
 * Adds a collapsible: [CollapsibleTrigger]s, and [CollapsibleContent]s shown or hidden when a
 * trigger fires.
 *
 * @param open whether the content is shown at first
 * @param onChange the handler run whenever the player shows or hides the content, or `null` for
 *        none
 * @param id the id of the collapsible, or `null` for a generated one
 * @param children the builder of the triggers and contents
 * @return the reference to whether the content is shown
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Collapsible(
    open: Boolean = false,
    onChange: ChangeHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputRef<Boolean> {
    val elementId = nextId(id)
    add(CollapsibleElement(elementId, this.children(children), open, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds the trigger part of a collapsible: an action of a widget inside it shows or hides the
 * content instead of reaching the server.
 *
 * @param id the id of the trigger, or `null` for a generated one
 * @param children the builder of the widgets that toggle the content, usually one button
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CollapsibleTrigger(id: String? = null, children: ComponentScope.() -> Unit): CollapsibleTriggerElement {
    val elementId = nextId(id)
    return add(CollapsibleTriggerElement(elementId, this.children(children)))
}

/**
 * Adds the content of a collapsible, shown while the collapsible is open.
 *
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the content
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CollapsibleContent(id: String? = null, children: ComponentScope.() -> Unit): CollapsibleContentElement {
    val elementId = nextId(id)
    return add(CollapsibleContentElement(elementId, this.children(children)))
}

/**
 * Adds an accordion: stacked [AccordionItem]s, each with a trigger that shows or hides its
 * content.
 *
 * @param type how many items can be open at once
 * @param collapsible whether the open item of a single accordion can be closed
 * @param value the values of the items open at first
 * @param onChange the handler run whenever the player opens or closes an item, or `null` for none
 * @param id the id of the accordion, or `null` for a generated one
 * @param children the builder of the items
 * @return the reference to the values of the open items
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Accordion(
    type: AccordionType = AccordionType.SINGLE,
    collapsible: Boolean = false,
    value: List<String> = emptyList(),
    onChange: ChangeHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputRef<List<String>> {
    val elementId = nextId(id)
    add(AccordionElement(elementId, this.children(children), type, collapsible, value, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.list)
}

/**
 * Adds an item of an accordion: an [AccordionTrigger] and an [AccordionContent], with a border
 * below.
 *
 * @param value the value that identifies the item in the accordion; it must not contain a comma
 * @param enabled whether the item can be opened and closed
 * @param id the id of the item, or `null` for a generated one
 * @param children the builder of the trigger and the content
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_` or [value] contains a comma
 */
fun ComponentScope.AccordionItem(
    value: String,
    enabled: Boolean = true,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): AccordionItemElement {
    require(',' !in value) { "Accordion item values must not contain a comma: $value" }
    val elementId = nextId(id)
    return add(AccordionItemElement(elementId, this.children(children), value, enabled))
}

/**
 * Adds the trigger of an accordion item: its text and a chevron that turns while the item is
 * open.
 *
 * @param text the text
 * @param id the id of the trigger, or `null` for a generated one
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AccordionTrigger(text: Component, id: String? = null): AccordionTriggerElement = add(AccordionTriggerElement(nextId(id), text))

/**
 * Adds the trigger of an accordion item with a plain text and a chevron that turns while the item
 * is open.
 *
 * @param text the text
 * @param id the id of the trigger, or `null` for a generated one
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AccordionTrigger(text: String, id: String? = null): AccordionTriggerElement = AccordionTrigger(Component.text(text), id)

/**
 * Adds the content of an accordion item, shown while the item is open.
 *
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the content
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AccordionContent(id: String? = null, children: ComponentScope.() -> Unit): AccordionContentElement {
    val elementId = nextId(id)
    return add(AccordionContentElement(elementId, this.children(children)))
}

/**
 * Adds tabs: one [TabsList] and a [TabsContent] per tab, of which only the content of the selected
 * tab is shown.
 *
 * @param value the value of the tab selected at first; empty selects the first enabled tab
 * @param orientation whether the triggers are in a row above the contents or in a column beside
 *        them
 * @param onChange the handler run whenever the player selects another tab, or `null` for none
 * @param id the id of the tabs, or `null` for a generated one
 * @param children the builder of the list and the contents
 * @return the reference to the value of the selected tab, which is `null` while it is unknown
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Tabs(
    value: String = "",
    orientation: Orientation = Orientation.HORIZONTAL,
    onChange: ChangeHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputRef<String?> {
    val elementId = nextId(id)
    add(TabsElement(elementId, this.children(children), value, orientation, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.selected)
}

/**
 * Adds the list of the [TabsTrigger]s of tabs.
 *
 * @param variant how the list is drawn
 * @param id the id of the list, or `null` for a generated one
 * @param children the builder of the triggers
 * @return the list
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TabsList(variant: TabsVariant = TabsVariant.DEFAULT, id: String? = null, children: ComponentScope.() -> Unit): TabsListElement {
    val elementId = nextId(id)
    return add(TabsListElement(elementId, this.children(children), variant))
}

/**
 * Adds a trigger of tabs: selecting it shows the content with the same value.
 *
 * @param value the value of the tab
 * @param text the text
 * @param icon the Lucide name of an icon before the text, or `null` for none
 * @param enabled whether the tab can be selected
 * @param id the id of the trigger, or `null` for a generated one
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TabsTrigger(
    value: String,
    text: Component,
    icon: String? = null,
    enabled: Boolean = true,
    id: String? = null,
): TabsTriggerElement = add(TabsTriggerElement(nextId(id), value, text, icon, enabled))

/**
 * Adds a trigger of tabs with a plain text: selecting it shows the content with the same value.
 *
 * @param value the value of the tab
 * @param text the text
 * @param icon the Lucide name of an icon before the text, or `null` for none
 * @param enabled whether the tab can be selected
 * @param id the id of the trigger, or `null` for a generated one
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TabsTrigger(
    value: String,
    text: String,
    icon: String? = null,
    enabled: Boolean = true,
    id: String? = null,
): TabsTriggerElement = TabsTrigger(value, Component.text(text), icon, enabled, id)

/**
 * Adds the content of a tab, shown while its tab is selected.
 *
 * @param value the value of the tab it belongs to
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the content
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.TabsContent(value: String, id: String? = null, children: ComponentScope.() -> Unit): TabsContentElement {
    val elementId = nextId(id)
    return add(TabsContentElement(elementId, this.children(children), value))
}

/**
 * Adds a breadcrumb: the path to the current page, as one [BreadcrumbList].
 *
 * @param id the id of the breadcrumb, or `null` for a generated one
 * @param children the builder of the list
 * @return the breadcrumb
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Breadcrumb(id: String? = null, children: ComponentScope.() -> Unit): BreadcrumbElement {
    val elementId = nextId(id)
    return add(BreadcrumbElement(elementId, this.children(children)))
}

/**
 * Adds the list of a breadcrumb: its [BreadcrumbItem]s and [BreadcrumbSeparator]s in a row, in
 * muted text.
 *
 * @param id the id of the list, or `null` for a generated one
 * @param children the builder of the items and separators
 * @return the list
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.BreadcrumbList(id: String? = null, children: ComponentScope.() -> Unit): BreadcrumbListElement {
    val elementId = nextId(id)
    return add(BreadcrumbListElement(elementId, this.children(children)))
}

/**
 * Adds an item of a breadcrumb, holding a [BreadcrumbLink], a [BreadcrumbPage] or a
 * [BreadcrumbEllipsis].
 *
 * @param id the id of the item, or `null` for a generated one
 * @param children the builder of the link, page or ellipsis
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.BreadcrumbItem(id: String? = null, children: ComponentScope.() -> Unit): BreadcrumbItemElement {
    val elementId = nextId(id)
    return add(BreadcrumbItemElement(elementId, this.children(children)))
}

/**
 * Adds a link of a breadcrumb: muted text that turns to the foreground colour when hovered and
 * runs its handler when clicked.
 *
 * @param text the text
 * @param enabled whether the link can be clicked
 * @param id the id of the link, or `null` for a generated one
 * @param onClick the handler run when the player clicks the link, or `null` for none
 * @return the link
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.BreadcrumbLink(text: Component, enabled: Boolean = true, id: String? = null, onClick: ButtonHandler? = null): BreadcrumbLinkElement {
    val elementId = nextId(id)
    return add(BreadcrumbLinkElement(elementId, text, enabled, bindButton(elementId, onClick)))
}

/**
 * Adds a link of a breadcrumb with a plain text that turns to the foreground colour when hovered
 * and runs its handler when clicked.
 *
 * @param text the text
 * @param enabled whether the link can be clicked
 * @param id the id of the link, or `null` for a generated one
 * @param onClick the handler run when the player clicks the link, or `null` for none
 * @return the link
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.BreadcrumbLink(text: String, enabled: Boolean = true, id: String? = null, onClick: ButtonHandler? = null): BreadcrumbLinkElement =
    BreadcrumbLink(Component.text(text), enabled, id, onClick)

/**
 * Adds the current page of a breadcrumb, in the foreground colour and not clickable.
 *
 * @param text the text
 * @param id the id of the page, or `null` for a generated one
 * @return the page
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.BreadcrumbPage(text: Component, id: String? = null): BreadcrumbPageElement = add(BreadcrumbPageElement(nextId(id), text))

/**
 * Adds the current page of a breadcrumb with a plain text, in the foreground colour and not
 * clickable.
 *
 * @param text the text
 * @param id the id of the page, or `null` for a generated one
 * @return the page
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.BreadcrumbPage(text: String, id: String? = null): BreadcrumbPageElement = BreadcrumbPage(Component.text(text), id)

/**
 * Adds a separator between breadcrumb items: a chevron or another icon.
 *
 * @param icon the Lucide name of the icon
 * @param id the id of the separator, or `null` for a generated one
 * @return the separator
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.BreadcrumbSeparator(icon: String = "chevron-right", id: String? = null): BreadcrumbSeparatorElement =
    add(BreadcrumbSeparatorElement(nextId(id), icon))

/**
 * Adds an ellipsis that stands for collapsed breadcrumb items. As the trigger of a
 * [DropdownMenu] it opens the menu.
 *
 * @param id the id of the ellipsis, or `null` for a generated one
 * @return the ellipsis
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.BreadcrumbEllipsis(id: String? = null): BreadcrumbEllipsisElement = add(BreadcrumbEllipsisElement(nextId(id)))

/**
 * Adds a pagination, centered across the available width, holding one [PaginationContent].
 *
 * @param id the id of the pagination, or `null` for a generated one
 * @param children the builder of the content
 * @return the pagination
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Pagination(id: String? = null, children: ComponentScope.() -> Unit): PaginationElement {
    val elementId = nextId(id)
    return add(PaginationElement(elementId, this.children(children), ElementSize.grow()))
}

/**
 * Adds the row of the [PaginationItem]s of a pagination.
 *
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the items
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PaginationContent(id: String? = null, children: ComponentScope.() -> Unit): PaginationContentElement {
    val elementId = nextId(id)
    return add(PaginationContentElement(elementId, this.children(children)))
}

/**
 * Adds an item of a pagination, holding a link, a previous or next link, or an ellipsis.
 *
 * @param id the id of the item, or `null` for a generated one
 * @param children the builder of the link or ellipsis
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PaginationItem(id: String? = null, children: ComponentScope.() -> Unit): PaginationItemElement {
    val elementId = nextId(id)
    return add(PaginationItemElement(elementId, this.children(children)))
}

/**
 * Adds a page link of a pagination, outlined while it is the current page, that runs its handler
 * when clicked.
 *
 * @param text the text, usually the page number
 * @param active whether the link is the current page
 * @param enabled whether the link can be clicked
 * @param id the id of the link, or `null` for a generated one
 * @param onClick the handler run when the player clicks the link, or `null` for none
 * @return the link
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PaginationLink(
    text: Component,
    active: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): PaginationLinkElement {
    val elementId = nextId(id)
    return add(PaginationLinkElement(elementId, text, active, enabled, bindButton(elementId, onClick)))
}

/**
 * Adds a page link of a pagination with a plain text, outlined while it is the current page, that
 * runs its handler when clicked.
 *
 * @param text the text, usually the page number
 * @param active whether the link is the current page
 * @param enabled whether the link can be clicked
 * @param id the id of the link, or `null` for a generated one
 * @param onClick the handler run when the player clicks the link, or `null` for none
 * @return the link
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PaginationLink(
    text: String,
    active: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): PaginationLinkElement = PaginationLink(Component.text(text), active, enabled, id, onClick)

/**
 * Adds the link to the previous page of a pagination: a chevron and its text.
 *
 * @param text the text
 * @param enabled whether the link can be clicked
 * @param id the id of the link, or `null` for a generated one
 * @param onClick the handler run when the player clicks the link, or `null` for none
 * @return the link
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PaginationPrevious(
    text: Component = Component.text("Zurück"),
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): PaginationPreviousElement {
    val elementId = nextId(id)
    return add(PaginationPreviousElement(elementId, text, enabled, bindButton(elementId, onClick)))
}

/**
 * Adds the link to the previous page of a pagination: a chevron and its plain text.
 *
 * @param text the text
 * @param enabled whether the link can be clicked
 * @param id the id of the link, or `null` for a generated one
 * @param onClick the handler run when the player clicks the link, or `null` for none
 * @return the link
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PaginationPrevious(text: String, enabled: Boolean = true, id: String? = null, onClick: ButtonHandler? = null): PaginationPreviousElement =
    PaginationPrevious(Component.text(text), enabled, id, onClick)

/**
 * Adds the link to the next page of a pagination: its text and a chevron.
 *
 * @param text the text
 * @param enabled whether the link can be clicked
 * @param id the id of the link, or `null` for a generated one
 * @param onClick the handler run when the player clicks the link, or `null` for none
 * @return the link
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PaginationNext(
    text: Component = Component.text("Weiter"),
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): PaginationNextElement {
    val elementId = nextId(id)
    return add(PaginationNextElement(elementId, text, enabled, bindButton(elementId, onClick)))
}

/**
 * Adds the link to the next page of a pagination: its plain text and a chevron.
 *
 * @param text the text
 * @param enabled whether the link can be clicked
 * @param id the id of the link, or `null` for a generated one
 * @param onClick the handler run when the player clicks the link, or `null` for none
 * @return the link
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PaginationNext(text: String, enabled: Boolean = true, id: String? = null, onClick: ButtonHandler? = null): PaginationNextElement =
    PaginationNext(Component.text(text), enabled, id, onClick)

/**
 * Adds an ellipsis that stands for pages without a link.
 *
 * @param id the id of the ellipsis, or `null` for a generated one
 * @return the ellipsis
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PaginationEllipsis(id: String? = null): PaginationEllipsisElement = add(PaginationEllipsisElement(nextId(id)))

/**
 * Adds a scroll area: its content, stacked, scrolled inside the area with the wheel or by dragging
 * thin scroll bars.
 *
 * @param width how wide the area is laid out, usually fixed
 * @param height how tall the area is laid out, usually fixed
 * @param orientation the directions the area scrolls in
 * @param id the id of the area, or `null` for a generated one
 * @param children the builder of the content
 * @return the area
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ScrollArea(
    width: ElementSize,
    height: ElementSize,
    orientation: ScrollOrientation = ScrollOrientation.VERTICAL,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): ScrollAreaElement {
    val elementId = nextId(id)
    return add(ScrollAreaElement(elementId, this.children(children), orientation, width, height))
}

/**
 * Adds a group of [ResizablePanel]s side by side or stacked, with a [ResizableHandle] between every
 * two that resizes the panels on both sides.
 *
 * @param orientation whether the panels are side by side or stacked
 * @param width how wide the group is laid out
 * @param height how tall the group is laid out
 * @param onChange the handler run after the player resized the panels, or `null` for none
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the panels and handles
 * @return the reference to the shares of the panels in percent
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ResizablePanelGroup(
    orientation: Orientation = Orientation.HORIZONTAL,
    width: ElementSize = ElementSize.grow(),
    height: ElementSize = ElementSize.FIT,
    onChange: ChangeHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputRef<List<Double>> {
    val elementId = nextId(id)
    add(ResizablePanelGroupElement(elementId, this.children(children), orientation, bindChange(elementId, onChange), width, height))
    return InputRef(elementId, InputParsers.numbers)
}

/**
 * Adds a panel of a resizable group, taking a share of the group in percent.
 *
 * @param defaultSize the share of the group the panel takes at first, in percent, or 0 to share
 *        what the other panels leave
 * @param minSize the smallest share the panel can take, in percent
 * @param maxSize the largest share the panel can take, in percent
 * @param id the id of the panel, or `null` for a generated one
 * @param children the builder of the content
 * @return the panel
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ResizablePanel(
    defaultSize: Double = 0.0,
    minSize: Double = 0.0,
    maxSize: Double = 100.0,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): ResizablePanelElement {
    val elementId = nextId(id)
    return add(ResizablePanelElement(elementId, this.children(children), defaultSize, minSize, maxSize))
}

/**
 * Adds a handle between two panels of a resizable group: a thin line, optionally with a grip,
 * that is dragged or moved with the arrow keys.
 *
 * @param withHandle whether a grip is drawn on the line
 * @param id the id of the handle, or `null` for a generated one
 * @return the handle
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ResizableHandle(withHandle: Boolean = false, id: String? = null): ResizableHandleElement =
    add(ResizableHandleElement(nextId(id), withHandle))

/**
 * Adds a carousel: one [CarouselContent] of slides, a [CarouselPrevious] and a [CarouselNext].
 *
 * @param width how wide the carousel is laid out, with its buttons
 * @param orientation whether the slides move sideways or up and down
 * @param loop whether the last slide is followed by the first
 * @param index the index of the slide shown first
 * @param height how tall the carousel is laid out; a vertical carousel needs a fixed height
 * @param onChange the handler run whenever the player shows another slide, or `null` for none
 * @param id the id of the carousel, or `null` for a generated one
 * @param children the builder of the content and the buttons
 * @return the reference to the index of the shown slide
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Carousel(
    width: ElementSize,
    orientation: Orientation = Orientation.HORIZONTAL,
    loop: Boolean = false,
    index: Int = 0,
    height: ElementSize = ElementSize.FIT,
    onChange: ChangeHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputRef<Int?> {
    val elementId = nextId(id)
    add(CarouselElement(elementId, this.children(children), orientation, loop, index, bindChange(elementId, onChange), width, height))
    return InputRef(elementId, InputParsers.index)
}

/**
 * Adds the content of a carousel: its [CarouselItem]s in a row or a column, of which the part from
 * the current slide on is shown.
 *
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the slides
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CarouselContent(id: String? = null, children: ComponentScope.() -> Unit): CarouselContentElement {
    val elementId = nextId(id)
    return add(CarouselContentElement(elementId, this.children(children)))
}

/**
 * Adds a slide of a carousel, taking a share of the content.
 *
 * @param basis the share of the content the slide takes, in percent
 * @param id the id of the slide, or `null` for a generated one
 * @param children the builder of the content of the slide
 * @return the slide
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CarouselItem(basis: Double = 100.0, id: String? = null, children: ComponentScope.() -> Unit): CarouselItemElement {
    val elementId = nextId(id)
    return add(CarouselItemElement(elementId, this.children(children), basis))
}

/**
 * Adds the button that shows the previous slide of a carousel: a round outline button with an
 * arrow.
 *
 * @param enabled whether the button can be used
 * @param id the id of the button, or `null` for a generated one
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CarouselPrevious(enabled: Boolean = true, id: String? = null): CarouselPreviousElement = add(CarouselPreviousElement(nextId(id), enabled))

/**
 * Adds the button that shows the next slide of a carousel: a round outline button with an arrow.
 *
 * @param enabled whether the button can be used
 * @param id the id of the button, or `null` for a generated one
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CarouselNext(enabled: Boolean = true, id: String? = null): CarouselNextElement = add(CarouselNextElement(nextId(id), enabled))

/**
 * Adds a navigation menu, holding one [NavigationMenuList].
 *
 * @param id the id of the menu, or `null` for a generated one
 * @param children the builder of the list
 * @return the menu
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.NavigationMenu(id: String? = null, children: ComponentScope.() -> Unit): NavigationMenuElement {
    val elementId = nextId(id)
    return add(NavigationMenuElement(elementId, this.children(children)))
}

/**
 * Adds the row of the [NavigationMenuItem]s of a navigation menu.
 *
 * @param id the id of the list, or `null` for a generated one
 * @param children the builder of the items
 * @return the list
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.NavigationMenuList(id: String? = null, children: ComponentScope.() -> Unit): NavigationMenuListElement {
    val elementId = nextId(id)
    return add(NavigationMenuListElement(elementId, this.children(children)))
}

/**
 * Adds an item of a navigation menu: a [NavigationMenuTrigger] with a [NavigationMenuContent] that
 * opens below it on hover or click, or a [NavigationMenuLink].
 *
 * @param onChange the handler run whenever the player opens or closes the content, or `null` for
 *        none
 * @param id the id of the item, or `null` for a generated one
 * @param children the builder of the trigger and content, or of the link
 * @return the reference to whether the content is open
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.NavigationMenuItem(onChange: ChangeHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<Boolean> {
    val elementId = nextId(id)
    add(NavigationMenuItemElement(elementId, this.children(children), false, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds the trigger of a navigation menu item: its text and a chevron that turns while the content
 * is open.
 *
 * @param text the text
 * @param enabled whether the trigger can open the content
 * @param id the id of the trigger, or `null` for a generated one
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.NavigationMenuTrigger(text: Component, enabled: Boolean = true, id: String? = null): NavigationMenuTriggerElement =
    add(NavigationMenuTriggerElement(nextId(id), text, enabled))

/**
 * Adds the trigger of a navigation menu item: its plain text and a chevron that turns while the
 * content is open.
 *
 * @param text the text
 * @param enabled whether the trigger can open the content
 * @param id the id of the trigger, or `null` for a generated one
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.NavigationMenuTrigger(text: String, enabled: Boolean = true, id: String? = null): NavigationMenuTriggerElement =
    NavigationMenuTrigger(Component.text(text), enabled, id)

/**
 * Adds the content of a navigation menu item, shown on a surface below its trigger while open.
 *
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the links and other content
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.NavigationMenuContent(id: String? = null, children: ComponentScope.() -> Unit): NavigationMenuContentElement {
    val elementId = nextId(id)
    return add(NavigationMenuContentElement(elementId, this.children(children)))
}

/**
 * Adds a link of a navigation menu: its content, highlighted when hovered or active, that runs its
 * handler when clicked. Directly in an item it is drawn like a trigger.
 *
 * @param active whether the link leads to the current page
 * @param enabled whether the link can be clicked
 * @param onClick the handler run when the player clicks the link, or `null` for none
 * @param id the id of the link, or `null` for a generated one
 * @param children the builder of the content, such as a title and a description
 * @return the link
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.NavigationMenuLink(
    active: Boolean = false,
    enabled: Boolean = true,
    onClick: ButtonHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): NavigationMenuLinkElement {
    val elementId = nextId(id)
    return add(NavigationMenuLinkElement(elementId, this.children(children), active, enabled, bindButton(elementId, onClick)))
}

/**
 * Adds the frame of a sidebar layout: one [Sidebar] and one [SidebarInset], with the expanded
 * state of the sidebar.
 *
 * @param width how wide the provider is laid out
 * @param height how tall the provider is laid out
 * @param open whether the sidebar is expanded at first
 * @param onChange the handler run whenever the player expands or collapses the sidebar, or `null`
 *        for none
 * @param id the id of the provider, or `null` for a generated one
 * @param children the builder of the sidebar and inset
 * @return the reference to whether the sidebar is expanded
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarProvider(
    width: ElementSize = ElementSize.grow(),
    height: ElementSize = ElementSize.FIT,
    open: Boolean = true,
    onChange: ChangeHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputRef<Boolean> {
    val elementId = nextId(id)
    add(SidebarProviderElement(elementId, this.children(children), open, bindChange(elementId, onChange), width, height))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds a sidebar: a [SidebarHeader], a [SidebarContent], a [SidebarFooter] and an optional
 * [SidebarRail] in a column on one side of its provider.
 *
 * @param side the side the sidebar is on
 * @param variant how the sidebar is drawn
 * @param collapsible how the sidebar collapses
 * @param id the id of the sidebar, or `null` for a generated one
 * @param children the builder of the parts
 * @return the sidebar
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Sidebar(
    side: SidebarSide = SidebarSide.LEFT,
    variant: SidebarVariant = SidebarVariant.SIDEBAR,
    collapsible: SidebarCollapsible = SidebarCollapsible.OFFCANVAS,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): SidebarElement {
    val elementId = nextId(id)
    return add(SidebarElement(elementId, this.children(children), side, variant, collapsible))
}

/**
 * Adds the main content beside a sidebar, taking the rest of the provider.
 *
 * @param id the id of the inset, or `null` for a generated one
 * @param children the builder of the content
 * @return the inset
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarInset(id: String? = null, children: ComponentScope.() -> Unit): SidebarInsetElement {
    val elementId = nextId(id)
    return add(SidebarInsetElement(elementId, this.children(children)))
}

/**
 * Adds the header of a sidebar, above its content.
 *
 * @param id the id of the header, or `null` for a generated one
 * @param children the builder of the header content
 * @return the header
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarHeader(id: String? = null, children: ComponentScope.() -> Unit): SidebarHeaderElement {
    val elementId = nextId(id)
    return add(SidebarHeaderElement(elementId, this.children(children)))
}

/**
 * Adds the footer of a sidebar, below its content.
 *
 * @param id the id of the footer, or `null` for a generated one
 * @param children the builder of the footer content
 * @return the footer
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarFooter(id: String? = null, children: ComponentScope.() -> Unit): SidebarFooterElement {
    val elementId = nextId(id)
    return add(SidebarFooterElement(elementId, this.children(children)))
}

/**
 * Adds the scrolling content of a sidebar between its header and footer, holding
 * [SidebarGroup]s.
 *
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the groups
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarContent(id: String? = null, children: ComponentScope.() -> Unit): SidebarContentElement {
    val elementId = nextId(id)
    return add(SidebarContentElement(elementId, this.children(children)))
}

/**
 * Adds a group of a sidebar: a [SidebarGroupLabel], an optional [SidebarGroupAction] and a
 * [SidebarGroupContent].
 *
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the label, action and content
 * @return the group
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarGroup(id: String? = null, children: ComponentScope.() -> Unit): SidebarGroupElement {
    val elementId = nextId(id)
    return add(SidebarGroupElement(elementId, this.children(children)))
}

/**
 * Adds the label of a sidebar group, in muted text; hidden while the sidebar is collapsed to icons.
 *
 * @param text the text
 * @param id the id of the label, or `null` for a generated one
 * @return the label
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarGroupLabel(text: Component, id: String? = null): SidebarGroupLabelElement = add(SidebarGroupLabelElement(nextId(id), text))

/**
 * Adds the label of a sidebar group, in plain muted text; hidden while the sidebar is collapsed to
 * icons.
 *
 * @param text the text
 * @param id the id of the label, or `null` for a generated one
 * @return the label
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarGroupLabel(text: String, id: String? = null): SidebarGroupLabelElement = SidebarGroupLabel(Component.text(text), id)

/**
 * Adds a small icon button at the end of a sidebar group label.
 *
 * @param icon the Lucide name of the icon
 * @param enabled whether the button can be clicked
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run when the player clicks the button, or `null` for none
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarGroupAction(
    icon: String = "plus",
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): SidebarGroupActionElement {
    val elementId = nextId(id)
    return add(SidebarGroupActionElement(elementId, icon, enabled, bindButton(elementId, onClick)))
}

/**
 * Adds the content of a sidebar group, usually one [SidebarMenu].
 *
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the content
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarGroupContent(id: String? = null, children: ComponentScope.() -> Unit): SidebarGroupContentElement {
    val elementId = nextId(id)
    return add(SidebarGroupContentElement(elementId, this.children(children)))
}

/**
 * Adds a menu of a sidebar: its [SidebarMenuItem]s, stacked.
 *
 * @param id the id of the menu, or `null` for a generated one
 * @param children the builder of the items
 * @return the menu
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenu(id: String? = null, children: ComponentScope.() -> Unit): SidebarMenuElement {
    val elementId = nextId(id)
    return add(SidebarMenuElement(elementId, this.children(children)))
}

/**
 * Adds an item of a sidebar menu: a [SidebarMenuButton] or [SidebarMenuSkeleton], with an optional
 * [SidebarMenuAction], [SidebarMenuBadge] and [SidebarMenuSub].
 *
 * @param id the id of the item, or `null` for a generated one
 * @param children the builder of the parts
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenuItem(id: String? = null, children: ComponentScope.() -> Unit): SidebarMenuItemElement {
    val elementId = nextId(id)
    return add(SidebarMenuItemElement(elementId, this.children(children)))
}

/**
 * Adds the button of a sidebar menu item: its icon and text, highlighted while active; only the
 * icon while the sidebar is collapsed to icons, with the tooltip on hover.
 *
 * @param text the text
 * @param icon the Lucide name of an icon before the text, or `null` for none
 * @param size the size of the button
 * @param variant how the button is drawn
 * @param active whether the button leads to the current page
 * @param tooltip the text shown beside the button while the sidebar is collapsed to icons, or
 *        `null` for none
 * @param enabled whether the button can be clicked
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run when the player clicks the button, or `null` for none
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenuButton(
    text: Component,
    icon: String? = null,
    size: SidebarMenuButtonSize = SidebarMenuButtonSize.DEFAULT,
    variant: SidebarMenuButtonVariant = SidebarMenuButtonVariant.DEFAULT,
    active: Boolean = false,
    tooltip: Component? = null,
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): SidebarMenuButtonElement {
    val elementId = nextId(id)
    return add(SidebarMenuButtonElement(elementId, text, icon, size, variant, active, tooltip, enabled, bindButton(elementId, onClick)))
}

/**
 * Adds the button of a sidebar menu item with a plain text: its icon and text, highlighted while
 * active; only the icon while the sidebar is collapsed to icons, with the tooltip on hover.
 *
 * @param text the text
 * @param icon the Lucide name of an icon before the text, or `null` for none
 * @param size the size of the button
 * @param variant how the button is drawn
 * @param active whether the button leads to the current page
 * @param tooltip the text shown beside the button while the sidebar is collapsed to icons, or
 *        `null` for none
 * @param enabled whether the button can be clicked
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run when the player clicks the button, or `null` for none
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenuButton(
    text: String,
    icon: String? = null,
    size: SidebarMenuButtonSize = SidebarMenuButtonSize.DEFAULT,
    variant: SidebarMenuButtonVariant = SidebarMenuButtonVariant.DEFAULT,
    active: Boolean = false,
    tooltip: Component? = null,
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): SidebarMenuButtonElement = SidebarMenuButton(Component.text(text), icon, size, variant, active, tooltip, enabled, id, onClick)

/**
 * Adds a small icon button at the end of a sidebar menu button.
 *
 * @param icon the Lucide name of the icon
 * @param showOnHover whether the action is shown only while its item is hovered or focused
 * @param enabled whether the button can be clicked
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run when the player clicks the button, or `null` for none
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenuAction(
    icon: String = "ellipsis",
    showOnHover: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): SidebarMenuActionElement {
    val elementId = nextId(id)
    return add(SidebarMenuActionElement(elementId, icon, showOnHover, enabled, bindButton(elementId, onClick)))
}

/**
 * Adds a small count or label at the end of a sidebar menu button.
 *
 * @param text the text
 * @param id the id of the badge, or `null` for a generated one
 * @return the badge
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenuBadge(text: Component, id: String? = null): SidebarMenuBadgeElement = add(SidebarMenuBadgeElement(nextId(id), text))

/**
 * Adds a small plain count or label at the end of a sidebar menu button.
 *
 * @param text the text
 * @param id the id of the badge, or `null` for a generated one
 * @return the badge
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenuBadge(text: String, id: String? = null): SidebarMenuBadgeElement = SidebarMenuBadge(Component.text(text), id)

/**
 * Adds a placeholder for a sidebar menu button that is still loading.
 *
 * @param showIcon whether a placeholder for the icon is drawn
 * @param id the id of the placeholder, or `null` for a generated one
 * @return the placeholder
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenuSkeleton(showIcon: Boolean = false, id: String? = null): SidebarMenuSkeletonElement =
    add(SidebarMenuSkeletonElement(nextId(id), showIcon))

/**
 * Adds a sub-menu below a sidebar menu button, indented with a line and holding
 * [SidebarMenuSubItem]s; hidden while the sidebar is collapsed to icons.
 *
 * @param id the id of the sub-menu, or `null` for a generated one
 * @param children the builder of the items
 * @return the sub-menu
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenuSub(id: String? = null, children: ComponentScope.() -> Unit): SidebarMenuSubElement {
    val elementId = nextId(id)
    return add(SidebarMenuSubElement(elementId, this.children(children)))
}

/**
 * Adds an item of a sidebar sub-menu, holding one [SidebarMenuSubButton].
 *
 * @param id the id of the item, or `null` for a generated one
 * @param children the builder of the button
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenuSubItem(id: String? = null, children: ComponentScope.() -> Unit): SidebarMenuSubItemElement {
    val elementId = nextId(id)
    return add(SidebarMenuSubItemElement(elementId, this.children(children)))
}

/**
 * Adds the button of a sidebar sub-menu item.
 *
 * @param text the text
 * @param icon the Lucide name of an icon before the text, or `null` for none
 * @param size the size of the text
 * @param active whether the button leads to the current page
 * @param enabled whether the button can be clicked
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run when the player clicks the button, or `null` for none
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenuSubButton(
    text: Component,
    icon: String? = null,
    size: SidebarMenuSubButtonSize = SidebarMenuSubButtonSize.MD,
    active: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): SidebarMenuSubButtonElement {
    val elementId = nextId(id)
    return add(SidebarMenuSubButtonElement(elementId, text, icon, size, active, enabled, bindButton(elementId, onClick)))
}

/**
 * Adds the button of a sidebar sub-menu item with a plain text.
 *
 * @param text the text
 * @param icon the Lucide name of an icon before the text, or `null` for none
 * @param size the size of the text
 * @param active whether the button leads to the current page
 * @param enabled whether the button can be clicked
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run when the player clicks the button, or `null` for none
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarMenuSubButton(
    text: String,
    icon: String? = null,
    size: SidebarMenuSubButtonSize = SidebarMenuSubButtonSize.MD,
    active: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): SidebarMenuSubButtonElement = SidebarMenuSubButton(Component.text(text), icon, size, active, enabled, id, onClick)

/**
 * Adds a small ghost button that expands and collapses the sidebar of the enclosing provider.
 *
 * @param enabled whether the trigger can be used
 * @param id the id of the trigger, or `null` for a generated one
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarTrigger(enabled: Boolean = true, id: String? = null): SidebarTriggerElement = add(SidebarTriggerElement(nextId(id), enabled))

/**
 * Adds a thin strip along the inner edge of a sidebar that expands and collapses it when clicked.
 *
 * @param id the id of the rail, or `null` for a generated one
 * @return the rail
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarRail(id: String? = null): SidebarRailElement = add(SidebarRailElement(nextId(id)))

/**
 * Adds a text input that fills the width of a sidebar.
 *
 * @param placeholder the hint shown while the input is empty
 * @param id the id of the input, or `null` for a generated one
 * @return the reference to the input's text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarInput(placeholder: Component = Component.empty(), id: String? = null): InputRef<String> =
    Input(placeholder = placeholder, width = ElementSize.grow(), id = id)

/**
 * Adds a horizontal separator between the parts of a sidebar.
 *
 * @param id the id of the separator, or `null` for a generated one
 * @return the separator
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SidebarSeparator(id: String? = null): SeparatorElement = Separator(id = id)

/**
 * Adds a direction: its content, stacked, laid out left to right or mirrored right to left. A
 * nested direction sets the direction of its own subtree.
 *
 * @param direction the direction of the content
 * @param width how wide the direction is laid out
 * @param height how tall the direction is laid out
 * @param id the id of the direction, or `null` for a generated one
 * @param children the builder of the content
 * @return the direction
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Direction(
    direction: LayoutDirection,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): DirectionElement {
    val elementId = nextId(id)
    return add(DirectionElement(elementId, this.children(children), direction, width, height))
}
