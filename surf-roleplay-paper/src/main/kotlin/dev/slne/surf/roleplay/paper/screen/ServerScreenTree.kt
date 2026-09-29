package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.AspectRatioElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuListElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuContentElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuLinkElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarProviderElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarInsetElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubItemElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselItemElement
import dev.slne.surf.roleplay.api.client.common.screen.ResizablePanelGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ResizablePanelElement
import dev.slne.surf.roleplay.api.client.common.screen.ScrollAreaElement
import dev.slne.surf.roleplay.api.client.common.screen.DirectionElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbListElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbItemElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationContentElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationItemElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsListElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CollapsibleElement
import dev.slne.surf.roleplay.api.client.common.screen.CollapsibleTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.CollapsibleContentElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionItemElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionContentElement
import dev.slne.surf.roleplay.api.client.common.screen.OverlayContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogContentElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogCloseElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.DrawerElement
import dev.slne.surf.roleplay.api.client.common.screen.DrawerContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandListElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuContentElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuRadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubElement
import dev.slne.surf.roleplay.api.client.common.screen.ContextMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverContentElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.HoverCardElement
import dev.slne.surf.roleplay.api.client.common.screen.HoverCardContentElement
import dev.slne.surf.roleplay.api.client.common.screen.TooltipElement
import dev.slne.surf.roleplay.api.client.common.screen.EmptyElement
import dev.slne.surf.roleplay.api.client.common.screen.EmptyHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.EmptyMediaElement
import dev.slne.surf.roleplay.api.client.common.screen.EmptyContentElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemMediaElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemContentElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemActionsElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertElement
import dev.slne.surf.roleplay.api.client.common.screen.CardElement
import dev.slne.surf.roleplay.api.client.common.screen.CardHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.CardActionElement
import dev.slne.surf.roleplay.api.client.common.screen.CardContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CardFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.KbdGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldContentElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldSetElement
import dev.slne.surf.roleplay.api.client.common.screen.FormElement
import dev.slne.surf.roleplay.api.client.common.screen.ComboboxElement
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAddonElement
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.ProgressElement
import dev.slne.surf.roleplay.api.client.common.screen.RowElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.ScrollListElement

/**
 * The server's copy of an open generic screen's element tree.
 *
 * It applies the same changes that are sent to the mod, so that it always matches what the
 * player sees, and it is what player actions are validated against.
 *
 * @param root the initial root element
 */
class ServerScreenTree(root: ScreenElement) {

    /**
     * The root element.
     */
    var root: ScreenElement = root
        private set

    /**
     * Finds an element by id.
     *
     * @param id the id
     * @return the element, or `null` if the tree has none with that id
     */
    fun find(id: String): ScreenElement? = find(root, id)

    /**
     * Returns every element of the tree, parents before children.
     *
     * @return the elements
     */
    fun elements(): List<ScreenElement> = mutableListOf<ScreenElement>().also { collect(root, it) }

    /**
     * Applies a change. A change whose target does not exist or does not fit the target, that
     * would remove the root, or that would give two elements the same id is refused.
     *
     * @param change the change
     * @return whether the change was applied
     */
    fun apply(change: ScreenChange): Boolean {
        val updated = when (change) {
            is ScreenChange.Replace -> {
                val target = find(change.targetId) ?: return false
                if (!idsStayUnique(change.element, removed = target)) return false
                if (root.id == change.targetId) change.element else replace(root, change.targetId) { change.element }
            }

            is ScreenChange.Insert -> {
                if (find(change.parentId) !is ContainerElement || !idsStayUnique(change.element, removed = null)) return false
                replace(root, change.parentId) { parent ->
                    val children = (parent as ContainerElement).children.toMutableList()
                    children.add(change.index.coerceIn(0, children.size), change.element)
                    withChildren(parent, children)
                }
            }

            is ScreenChange.Remove -> {
                if (root.id == change.targetId || find(change.targetId) == null) return false
                remove(root, change.targetId)
            }

            is ScreenChange.SetText -> update(change.targetId) { element ->
                ElementRules.rule(element)?.withText?.invoke(element, change.text)
            } ?: return false

            is ScreenChange.SetValue -> update(change.targetId) { element -> withValue(element, change.value) } ?: return false
            is ScreenChange.SetOpen -> update(change.targetId) { element -> withValue(element, change.open.toString()) } ?: return false
            is ScreenChange.SetProgress -> update(change.targetId) { element ->
                (element as? ProgressElement)?.copy(progress = change.progress)
            } ?: return false

            is ScreenChange.SetEnabled -> update(change.targetId) { element ->
                ElementRules.rule(element)?.withEnabled?.invoke(element, change.enabled)
            } ?: return false

            is ScreenChange.SetInvalid -> if (find(change.targetId) == null) return false else root
            is ScreenChange.SetOptions -> update(change.targetId) { element ->
                (element as? ComboboxElement)?.let { combobox -> withOptions(combobox, change.groups) }
            } ?: return false
        }
        root = updated
        return true
    }

    /**
     * Stores validated input values in the tree, so that it holds what the player submitted.
     *
     * @param values the values keyed by input id, in their string form
     */
    fun storeValues(values: Map<String, String>) {
        for ((id, value) in values) apply(ScreenChange.SetValue(id, value))
    }

    /**
     * Returns an input element with a new value, or `null` if the element is not an input or the
     * value does not fit it.
     *
     * @param element the element
     * @param value the value in its string form
     * @return the updated element, or `null`
     */
    private fun withValue(element: ScreenElement, value: String): ScreenElement? =
        ElementRules.input(element)?.withValue?.invoke(element, value)

    /**
     * Returns a combobox with new options. Options offered before and missing from the new groups
     * are kept in an extra group without heading at the end, so that an option the player chose
     * from an earlier result stays valid.
     *
     * @param combobox the combobox
     * @param groups the new option groups
     * @return the updated combobox, or `null` if the new option values repeat
     */
    private fun withOptions(combobox: ComboboxElement, groups: List<SelectChoiceGroup>): ComboboxElement? {
        val present = groups.flatMap { group -> group.options.map { it.value } }.toSet()
        val kept = combobox.groups.flatMap { it.options }.filter { it.value !in present }
        val merged = if (kept.isEmpty()) groups else groups + SelectChoiceGroup(null, kept)
        return runCatching { combobox.copy(groups = merged) }.getOrNull()
    }

    /**
     * Rebuilds the tree with one element updated.
     *
     * @param id the id of the element
     * @param transform returns the updated element, or `null` if the change does not fit it
     * @return the new root, or `null` if the element does not exist or the change does not fit
     */
    private fun update(id: String, transform: (ScreenElement) -> ScreenElement?): ScreenElement? {
        val target = find(id) ?: return null
        val updated = transform(target) ?: return null
        return if (root.id == id) updated else replace(root, id) { updated }
    }

    /**
     * Checks that adding an element keeps every id unique.
     *
     * @param added the element to add, with its children
     * @param removed the element it replaces, with its children, or `null`
     * @return whether the ids stay unique
     */
    private fun idsStayUnique(added: ScreenElement, removed: ScreenElement?): Boolean {
        val removedIds = removed?.let { r -> mutableListOf<ScreenElement>().also { collect(r, it) }.map { it.id }.toSet() } ?: emptySet()
        val existing = elements().map { it.id }.filterNot { it in removedIds }.toSet()
        val addedIds = mutableListOf<ScreenElement>().also { collect(added, it) }.map { it.id }
        return addedIds.size == addedIds.toSet().size && addedIds.none { it in existing }
    }

    /**
     * Holds the tree walking helpers.
     */
    private companion object {
        /**
         * Finds an element by id below and including an element.
         *
         * @param element the element to search from
         * @param id the id
         * @return the element, or `null`
         */
        fun find(element: ScreenElement, id: String): ScreenElement? {
            if (element.id == id) return element
            if (element is ContainerElement) {
                for (child in element.children) find(child, id)?.let { return it }
            }
            return null
        }

        /**
         * Adds an element and its descendants to a list, parents first.
         *
         * @param element the element
         * @param into the list
         */
        fun collect(element: ScreenElement, into: MutableList<ScreenElement>) {
            into += element
            if (element is ContainerElement) element.children.forEach { collect(it, into) }
        }

        /**
         * Rebuilds a subtree with one descendant replaced.
         *
         * @param element the subtree
         * @param id the id of the descendant
         * @param transform creates the replacement
         * @return the rebuilt subtree
         */
        fun replace(element: ScreenElement, id: String, transform: (ScreenElement) -> ScreenElement): ScreenElement {
            if (element.id == id) return transform(element)
            if (element !is ContainerElement) return element
            return withChildren(element, element.children.map { replace(it, id, transform) })
        }

        /**
         * Rebuilds a subtree with one descendant removed.
         *
         * @param element the subtree
         * @param id the id of the descendant
         * @return the rebuilt subtree
         */
        fun remove(element: ScreenElement, id: String): ScreenElement {
            if (element !is ContainerElement) return element
            return withChildren(element, element.children.filter { it.id != id }.map { remove(it, id) })
        }

        /**
         * Returns a copy of a container with other children.
         *
         * @param container the container
         * @param children the new children
         * @return the copy
         */
        fun withChildren(container: ContainerElement, children: List<ScreenElement>): ScreenElement = when (container) {
            is RowElement -> container.copy(children = children)
            is ColumnElement -> container.copy(children = children)
            is ScrollListElement -> container.copy(children = children)
            is ButtonGroupElement -> container.copy(children = children)
            is InputGroupElement -> container.copy(children = children)
            is InputGroupAddonElement -> container.copy(children = children)
            is FormElement -> container.copy(children = children)
            is FieldSetElement -> container.copy(children = children)
            is FieldGroupElement -> container.copy(children = children)
            is FieldElement -> container.copy(children = children)
            is FieldContentElement -> container.copy(children = children)
            is KbdGroupElement -> container.copy(children = children)
            is AspectRatioElement -> container.copy(children = children)
            is AvatarGroupElement -> container.copy(children = children)
            is AlertElement -> container.copy(children = children)
            is CardElement -> container.copy(children = children)
            is CardHeaderElement -> container.copy(children = children)
            is CardActionElement -> container.copy(children = children)
            is CardContentElement -> container.copy(children = children)
            is CardFooterElement -> container.copy(children = children)
            is EmptyElement -> container.copy(children = children)
            is EmptyHeaderElement -> container.copy(children = children)
            is EmptyMediaElement -> container.copy(children = children)
            is EmptyContentElement -> container.copy(children = children)
            is ItemElement -> container.copy(children = children)
            is ItemMediaElement -> container.copy(children = children)
            is ItemContentElement -> container.copy(children = children)
            is ItemActionsElement -> container.copy(children = children)
            is ItemHeaderElement -> container.copy(children = children)
            is ItemFooterElement -> container.copy(children = children)
            is ItemGroupElement -> container.copy(children = children)
            is PopoverElement -> container.copy(children = children)
            is PopoverContentElement -> container.copy(children = children)
            is PopoverHeaderElement -> container.copy(children = children)
            is HoverCardElement -> container.copy(children = children)
            is HoverCardContentElement -> container.copy(children = children)
            is TooltipElement -> container.copy(children = children)
            is OverlayContainerElement -> container.copy(children = children)
            is DialogElement -> container.copy(children = children)
            is CollapsibleElement -> container.copy(children = children)
            is CollapsibleTriggerElement -> container.copy(children = children)
            is CollapsibleContentElement -> container.copy(children = children)
            is AccordionElement -> container.copy(children = children)
            is AccordionItemElement -> container.copy(children = children)
            is AccordionContentElement -> container.copy(children = children)
            is TabsElement -> container.copy(children = children)
            is TabsListElement -> container.copy(children = children)
            is TabsContentElement -> container.copy(children = children)
            is BreadcrumbElement -> container.copy(children = children)
            is BreadcrumbListElement -> container.copy(children = children)
            is BreadcrumbItemElement -> container.copy(children = children)
            is PaginationElement -> container.copy(children = children)
            is PaginationContentElement -> container.copy(children = children)
            is PaginationItemElement -> container.copy(children = children)
            is ScrollAreaElement -> container.copy(children = children)
            is DirectionElement -> container.copy(children = children)
            is ResizablePanelGroupElement -> container.copy(children = children)
            is ResizablePanelElement -> container.copy(children = children)
            is CarouselElement -> container.copy(children = children)
            is CarouselContentElement -> container.copy(children = children)
            is CarouselItemElement -> container.copy(children = children)
            is NavigationMenuElement -> container.copy(children = children)
            is NavigationMenuListElement -> container.copy(children = children)
            is NavigationMenuItemElement -> container.copy(children = children)
            is NavigationMenuContentElement -> container.copy(children = children)
            is NavigationMenuLinkElement -> container.copy(children = children)
            is SidebarProviderElement -> container.copy(children = children)
            is SidebarElement -> container.copy(children = children)
            is SidebarInsetElement -> container.copy(children = children)
            is SidebarHeaderElement -> container.copy(children = children)
            is SidebarFooterElement -> container.copy(children = children)
            is SidebarContentElement -> container.copy(children = children)
            is SidebarGroupElement -> container.copy(children = children)
            is SidebarGroupContentElement -> container.copy(children = children)
            is SidebarMenuElement -> container.copy(children = children)
            is SidebarMenuItemElement -> container.copy(children = children)
            is SidebarMenuSubElement -> container.copy(children = children)
            is SidebarMenuSubItemElement -> container.copy(children = children)
            is DialogContentElement -> container.copy(children = children)
            is DialogHeaderElement -> container.copy(children = children)
            is DialogFooterElement -> container.copy(children = children)
            is DialogCloseElement -> container.copy(children = children)
            is AlertDialogElement -> container.copy(children = children)
            is AlertDialogContentElement -> container.copy(children = children)
            is SheetElement -> container.copy(children = children)
            is SheetContentElement -> container.copy(children = children)
            is SheetHeaderElement -> container.copy(children = children)
            is SheetFooterElement -> container.copy(children = children)
            is DrawerElement -> container.copy(children = children)
            is DrawerContentElement -> container.copy(children = children)
            is CommandElement -> container.copy(children = children)
            is CommandListElement -> container.copy(children = children)
            is CommandGroupElement -> container.copy(children = children)
            is DropdownMenuElement -> container.copy(children = children)
            is MenuContentElement -> container.copy(children = children)
            is MenuRadioGroupElement -> container.copy(children = children)
            is MenuGroupElement -> container.copy(children = children)
            is MenuSubElement -> container.copy(children = children)
            is ContextMenuElement -> container.copy(children = children)
            is MenubarElement -> container.copy(children = children)
            is MenubarMenuElement -> container.copy(children = children)
        }
    }
}
