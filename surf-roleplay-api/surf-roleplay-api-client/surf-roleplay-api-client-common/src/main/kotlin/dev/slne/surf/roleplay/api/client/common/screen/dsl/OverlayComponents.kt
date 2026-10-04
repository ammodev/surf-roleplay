package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.ALERT_DIALOG_SMALL_WIDTH
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogContentElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogMediaElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogSize
import dev.slne.surf.roleplay.api.client.common.screen.Alignment
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.CommandElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandEmptyElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandInputElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandItemElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandListElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.ContextMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.DIALOG_WIDTH
import dev.slne.surf.roleplay.api.client.common.screen.DialogCloseElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogContentElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.DrawerContentElement
import dev.slne.surf.roleplay.api.client.common.screen.DrawerElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.HOVER_CARD_WIDTH
import dev.slne.surf.roleplay.api.client.common.screen.HoverCardContentElement
import dev.slne.surf.roleplay.api.client.common.screen.HoverCardElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuCheckboxItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuContentElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuLabelElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuRadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuRadioItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.OverlayContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.OverlaySide
import dev.slne.surf.roleplay.api.client.common.screen.POPOVER_WIDTH
import dev.slne.surf.roleplay.api.client.common.screen.PopoverContentElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.SearchHandler
import dev.slne.surf.roleplay.api.client.common.screen.SheetContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetFooterElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetHeaderElement
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
import dev.slne.surf.roleplay.api.client.common.screen.TextKind
import dev.slne.surf.roleplay.api.client.common.screen.TooltipElement
import net.kyori.adventure.text.Component

/**
 * Adds a popover: its triggers, which open the popover when clicked instead of firing their own
 * actions, and one [PopoverContent].
 *
 * @param side the side of the triggers the content opens on
 * @param align how the content is aligned along that side
 * @param open whether the popover starts open
 * @param onChange the handler run whenever the player opens or closes the popover, or `null` for
 *        none
 * @param id the id of the popover, or `null` for a generated one
 * @param children the builder of the triggers and the content
 * @return the reference to whether the popover is open
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Popover(
    side: OverlaySide = OverlaySide.BOTTOM,
    align: Alignment = Alignment.CENTER,
    open: Boolean = false,
    onChange: ChangeHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputRef<Boolean> {
    val elementId = nextId(id)
    add(PopoverElement(elementId, this.children(children), open, side, align, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds the content of a popover, on a bordered surface.
 *
 * @param width how wide the content is laid out
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the content
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PopoverContent(
    width: ElementSize = ElementSize.fixed(POPOVER_WIDTH),
    id: String? = null,
    children: ComponentScope.() -> Unit,
): PopoverContentElement {
    val elementId = nextId(id)
    return add(PopoverContentElement(elementId, this.children(children), width))
}

/**
 * Adds the header of a popover content: its title and description, stacked.
 *
 * @param id the id of the header, or `null` for a generated one
 * @param children the builder of the title and description
 * @return the header
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PopoverHeader(id: String? = null, children: ComponentScope.() -> Unit): PopoverHeaderElement {
    val elementId = nextId(id)
    return add(PopoverHeaderElement(elementId, this.children(children)))
}

/**
 * Adds the title of a popover.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PopoverTitle(text: Component, id: String? = null): TextElement = styledText(text, TextKind.POPOVER_TITLE, id = id)

/**
 * Adds the plain title of a popover.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PopoverTitle(text: String, id: String? = null): TextElement = PopoverTitle(Component.text(text), id)

/**
 * Adds the muted description of a popover.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PopoverDescription(text: Component, id: String? = null): TextElement = styledText(text, TextKind.POPOVER_DESCRIPTION, id = id)

/**
 * Adds the plain, muted description of a popover.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.PopoverDescription(text: String, id: String? = null): TextElement = PopoverDescription(Component.text(text), id)

/**
 * Adds a hover card: its triggers and one [HoverCardContent], which opens while the mouse rests on
 * a trigger.
 *
 * @param side the side of the triggers the content opens on
 * @param align how the content is aligned along that side
 * @param openDelay how long the mouse must rest on a trigger before the content opens, in
 *        milliseconds
 * @param closeDelay how long after the mouse left the triggers and the content it closes, in
 *        milliseconds
 * @param onChange the handler run whenever the hover card opens or closes, or `null` for none
 * @param id the id of the hover card, or `null` for a generated one
 * @param children the builder of the triggers and the content
 * @return the reference to whether the hover card is open
 * @throws IllegalArgumentException if [id] starts with `_` or a delay is negative
 */
fun ComponentScope.HoverCard(
    side: OverlaySide = OverlaySide.BOTTOM,
    align: Alignment = Alignment.CENTER,
    openDelay: Int = 700,
    closeDelay: Int = 300,
    onChange: ChangeHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputRef<Boolean> {
    require(openDelay >= 0 && closeDelay >= 0) { "Hover card delays must not be negative" }
    val elementId = nextId(id)
    add(HoverCardElement(elementId, this.children(children), false, side, align, openDelay, closeDelay, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds the content of a hover card, on a bordered surface.
 *
 * @param width how wide the content is laid out
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the content
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.HoverCardContent(
    width: ElementSize = ElementSize.fixed(HOVER_CARD_WIDTH),
    id: String? = null,
    children: ComponentScope.() -> Unit,
): HoverCardContentElement {
    val elementId = nextId(id)
    return add(HoverCardContentElement(elementId, this.children(children), width))
}

/**
 * Adds a tooltip: its triggers, and a short text shown next to them while a trigger is hovered or
 * focused.
 *
 * @param text the text
 * @param side the side of the triggers the text is shown on
 * @param id the id of the tooltip, or `null` for a generated one
 * @param children the builder of the triggers
 * @return the tooltip
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Tooltip(
    text: Component,
    side: OverlaySide = OverlaySide.TOP,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): TooltipElement {
    val elementId = nextId(id)
    return add(TooltipElement(elementId, this.children(children), text, side))
}

/**
 * Adds a tooltip with a plain text: its triggers, and a short text shown next to them while a
 * trigger is hovered or focused.
 *
 * @param text the text
 * @param side the side of the triggers the text is shown on
 * @param id the id of the tooltip, or `null` for a generated one
 * @param children the builder of the triggers
 * @return the tooltip
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Tooltip(
    text: String,
    side: OverlaySide = OverlaySide.TOP,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): TooltipElement = Tooltip(Component.text(text), side, id, children)

/**
 * Adds a dropdown menu: its triggers and one [MenuContent], which opens next to them when a
 * trigger is clicked.
 *
 * @param side the side of the triggers the menu opens on
 * @param align how the menu is aligned along that side
 * @param onChange the handler run whenever the player opens or closes the menu, or `null` for
 *        none
 * @param id the id of the menu, or `null` for a generated one
 * @param children the builder of the triggers and the content
 * @return the reference to whether the menu is open
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DropdownMenu(
    side: OverlaySide = OverlaySide.BOTTOM,
    align: Alignment = Alignment.CENTER,
    onChange: ChangeHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputRef<Boolean> {
    val elementId = nextId(id)
    add(DropdownMenuElement(elementId, this.children(children), false, side, align, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds the content of a menu: a stack of items, labels, separators, groups and sub-menus on a
 * bordered surface.
 *
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the entries
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuContent(id: String? = null, children: ComponentScope.() -> Unit): MenuContentElement {
    val elementId = nextId(id)
    return add(MenuContentElement(elementId, this.children(children)))
}

/**
 * Adds an item of a menu that runs its handler and closes the menu when chosen.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param shortcut a keyboard shortcut shown at the end of the item, or `null` for none
 * @param destructive whether the item is drawn in the destructive colour
 * @param inset whether the text is indented as if it had an icon
 * @param enabled whether the item can be chosen
 * @param id the id of the item, or `null` for a generated one
 * @param onClick the handler run when the player chooses the item, or `null` for none
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuItem(
    text: Component,
    icon: String? = null,
    shortcut: Component? = null,
    destructive: Boolean = false,
    inset: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): MenuItemElement {
    val elementId = nextId(id)
    return add(MenuItemElement(elementId, text, icon, shortcut, destructive, inset, enabled, bindButton(elementId, onClick)))
}

/**
 * Adds an item of a menu with a plain text that runs its handler and closes the menu when chosen.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param shortcut a keyboard shortcut shown at the end of the item, or `null` for none
 * @param destructive whether the item is drawn in the destructive colour
 * @param inset whether the text is indented as if it had an icon
 * @param enabled whether the item can be chosen
 * @param id the id of the item, or `null` for a generated one
 * @param onClick the handler run when the player chooses the item, or `null` for none
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuItem(
    text: String,
    icon: String? = null,
    shortcut: Component? = null,
    destructive: Boolean = false,
    inset: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): MenuItemElement = MenuItem(Component.text(text), icon, shortcut, destructive, inset, enabled, id, onClick)

/**
 * Adds an item of a menu with a check mark; choosing it flips the mark and runs its handler.
 *
 * @param text the text
 * @param checked whether the item starts checked
 * @param enabled whether the item can be chosen
 * @param id the id of the item, or `null` for a generated one
 * @param onToggle the handler run when the player chooses the item, or `null` for none
 * @return the reference to whether the item is checked
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuCheckboxItem(
    text: Component,
    checked: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
    onToggle: ButtonHandler? = null,
): InputRef<Boolean> {
    val elementId = nextId(id)
    add(MenuCheckboxItemElement(elementId, text, checked, enabled, bindButton(elementId, onToggle)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds an item of a menu with a plain text and a check mark; choosing it flips the mark and runs
 * its handler.
 *
 * @param text the text
 * @param checked whether the item starts checked
 * @param enabled whether the item can be chosen
 * @param id the id of the item, or `null` for a generated one
 * @param onToggle the handler run when the player chooses the item, or `null` for none
 * @return the reference to whether the item is checked
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuCheckboxItem(
    text: String,
    checked: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
    onToggle: ButtonHandler? = null,
): InputRef<Boolean> = MenuCheckboxItem(Component.text(text), checked, enabled, id, onToggle)

/**
 * Adds a group of [MenuRadioItem]s to a menu, of which one is chosen; choosing an item runs the
 * group's handler.
 *
 * @param value the value of the item chosen at first, or empty for none
 * @param onSelect the handler run when the player chooses an item, or `null` for none
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the radio items
 * @return the reference to the value of the chosen item, which is `null` while none is chosen
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuRadioGroup(
    value: String = "",
    onSelect: ButtonHandler? = null,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputRef<String?> {
    val elementId = nextId(id)
    add(MenuRadioGroupElement(elementId, this.children(children), value, bindButton(elementId, onSelect)))
    return InputRef(elementId, InputParsers.selected)
}

/**
 * Adds an item to a menu radio group, marked with a dot while it is chosen.
 *
 * @param text the text
 * @param value the value the item stands for
 * @param enabled whether the item can be chosen
 * @param id the id of the item, or `null` for a generated one
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuRadioItem(text: Component, value: String, enabled: Boolean = true, id: String? = null): MenuRadioItemElement =
    add(MenuRadioItemElement(nextId(id), text, value, enabled))

/**
 * Adds an item with a plain text to a menu radio group, marked with a dot while it is chosen.
 *
 * @param text the text
 * @param value the value the item stands for
 * @param enabled whether the item can be chosen
 * @param id the id of the item, or `null` for a generated one
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuRadioItem(text: String, value: String, enabled: Boolean = true, id: String? = null): MenuRadioItemElement =
    MenuRadioItem(Component.text(text), value, enabled, id)

/**
 * Adds a heading to a menu.
 *
 * @param text the text
 * @param inset whether the text is indented as if it had an icon
 * @param id the id of the label, or `null` for a generated one
 * @return the label
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuLabel(text: Component, inset: Boolean = false, id: String? = null): MenuLabelElement =
    add(MenuLabelElement(nextId(id), text, inset))

/**
 * Adds a plain heading to a menu.
 *
 * @param text the text
 * @param inset whether the text is indented as if it had an icon
 * @param id the id of the label, or `null` for a generated one
 * @return the label
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuLabel(text: String, inset: Boolean = false, id: String? = null): MenuLabelElement = MenuLabel(Component.text(text), inset, id)

/**
 * Adds a line between parts of a menu.
 *
 * @param id the id of the separator, or `null` for a generated one
 * @return the separator
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuSeparator(id: String? = null): MenuSeparatorElement = add(MenuSeparatorElement(nextId(id)))

/**
 * Adds a group of related menu entries.
 *
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the entries
 * @return the group
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuGroup(id: String? = null, children: ComponentScope.() -> Unit): MenuGroupElement {
    val elementId = nextId(id)
    return add(MenuGroupElement(elementId, this.children(children)))
}

/**
 * Adds a sub-menu: one [MenuSubTrigger] and one [MenuContent], which opens beside the trigger.
 *
 * @param onChange the handler run whenever the player opens or closes the sub-menu, or `null` for
 *        none
 * @param id the id of the sub-menu, or `null` for a generated one
 * @param children the builder of the trigger and the content
 * @return the reference to whether the sub-menu is open
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuSub(onChange: ChangeHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<Boolean> {
    val elementId = nextId(id)
    add(MenuSubElement(elementId, this.children(children), false, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds the item of a menu that opens a sub-menu, marked with an arrow.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param inset whether the text is indented as if it had an icon
 * @param enabled whether the trigger can be used
 * @param id the id of the trigger, or `null` for a generated one
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuSubTrigger(
    text: Component,
    icon: String? = null,
    inset: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
): MenuSubTriggerElement = add(MenuSubTriggerElement(nextId(id), text, icon, inset, enabled))

/**
 * Adds the item of a menu with a plain text that opens a sub-menu, marked with an arrow.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param inset whether the text is indented as if it had an icon
 * @param enabled whether the trigger can be used
 * @param id the id of the trigger, or `null` for a generated one
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenuSubTrigger(
    text: String,
    icon: String? = null,
    inset: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
): MenuSubTriggerElement = MenuSubTrigger(Component.text(text), icon, inset, enabled, id)

/**
 * Adds a context menu: the area a right click opens the menu in, and one [MenuContent], which
 * opens at the mouse.
 *
 * @param onChange the handler run whenever the player opens or closes the menu, or `null` for
 *        none
 * @param id the id of the context menu, or `null` for a generated one
 * @param children the builder of the area and the content
 * @return the reference to whether the menu is open
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.ContextMenu(onChange: ChangeHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<Boolean> {
    val elementId = nextId(id)
    add(ContextMenuElement(elementId, this.children(children), false, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds a horizontal bar of [MenubarMenu]s, as at the top of an application.
 *
 * @param id the id of the menubar, or `null` for a generated one
 * @param children the builder of the menus
 * @return the menubar
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Menubar(id: String? = null, children: ComponentScope.() -> Unit): MenubarElement {
    val elementId = nextId(id)
    return add(MenubarElement(elementId, this.children(children)))
}

/**
 * Adds a menu to a menubar: one [MenubarTrigger] and one [MenuContent], which opens below it.
 *
 * @param onChange the handler run whenever the player opens or closes the menu, or `null` for
 *        none
 * @param id the id of the menu, or `null` for a generated one
 * @param children the builder of the trigger and the content
 * @return the reference to whether the menu is open
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenubarMenu(onChange: ChangeHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<Boolean> {
    val elementId = nextId(id)
    add(MenubarMenuElement(elementId, this.children(children), false, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds the trigger of a menubar menu, a text that is highlighted while its menu is open.
 *
 * @param text the text
 * @param id the id of the trigger, or `null` for a generated one
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenubarTrigger(text: Component, id: String? = null): MenubarTriggerElement = add(MenubarTriggerElement(nextId(id), text))

/**
 * Adds the trigger of a menubar menu, a plain text that is highlighted while its menu is open.
 *
 * @param text the text
 * @param id the id of the trigger, or `null` for a generated one
 * @return the trigger
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.MenubarTrigger(text: String, id: String? = null): MenubarTriggerElement = MenubarTrigger(Component.text(text), id)

/**
 * Adds a command menu, as wide as its container: a [CommandInput] above a [CommandList]. Its
 * input filters its items on the client, unless it has a search handler, which then receives
 * every query and answers by patching the list.
 *
 * @param onSearch the handler run for every typed query, or `null` to filter on the client
 * @param id the id of the command, or `null` for a generated one
 * @param children the builder of the input and the list
 * @return the command
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Command(onSearch: SearchHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): CommandElement {
    val elementId = nextId(id)
    return add(CommandElement(elementId, this.children(children), bindSearch(elementId, onSearch), width = ElementSize.grow()))
}

/**
 * Adds the search input of a command menu, with a search icon, as wide as the command.
 *
 * @param placeholder the hint shown while the input is empty
 * @param id the id of the input, or `null` for a generated one
 * @return the input
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CommandInput(placeholder: Component = Component.empty(), id: String? = null): CommandInputElement =
    add(CommandInputElement(nextId(id), placeholder, width = ElementSize.grow()))

/**
 * Adds the scrolling list of a command menu.
 *
 * @param id the id of the list, or `null` for a generated one
 * @param children the builder of the empty text, groups, items and separators
 * @return the list
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CommandList(id: String? = null, children: ComponentScope.() -> Unit): CommandListElement {
    val elementId = nextId(id)
    return add(CommandListElement(elementId, this.children(children)))
}

/**
 * Adds the text of a command menu shown while no item matches the query.
 *
 * @param text the text
 * @param id the id of the text, or `null` for a generated one
 * @return the text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CommandEmpty(text: Component, id: String? = null): CommandEmptyElement = add(CommandEmptyElement(nextId(id), text))

/**
 * Adds the plain text of a command menu shown while no item matches the query.
 *
 * @param text the text
 * @param id the id of the text, or `null` for a generated one
 * @return the text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CommandEmpty(text: String, id: String? = null): CommandEmptyElement = CommandEmpty(Component.text(text), id)

/**
 * Adds a group of command items under a heading, hidden while none of its items match the query.
 *
 * @param heading the heading, or `null` for none
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the items
 * @return the group
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CommandGroup(heading: Component? = null, id: String? = null, children: ComponentScope.() -> Unit): CommandGroupElement {
    val elementId = nextId(id)
    return add(CommandGroupElement(elementId, this.children(children), heading))
}

/**
 * Adds a group of command items under a plain heading, hidden while none of its items match the
 * query.
 *
 * @param heading the heading
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the items
 * @return the group
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CommandGroup(heading: String, id: String? = null, children: ComponentScope.() -> Unit): CommandGroupElement =
    CommandGroup(Component.text(heading), id, children)

/**
 * Adds an item of a command menu that runs its handler when chosen and is shown while it matches
 * the query.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param shortcut a keyboard shortcut shown at the end of the item, or `null` for none
 * @param keywords further words the query matches the item by
 * @param enabled whether the item can be chosen
 * @param id the id of the item, or `null` for a generated one
 * @param onClick the handler run when the player chooses the item, or `null` for none
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CommandItem(
    text: Component,
    icon: String? = null,
    shortcut: Component? = null,
    keywords: List<String> = emptyList(),
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): CommandItemElement {
    val elementId = nextId(id)
    return add(CommandItemElement(elementId, text, icon, shortcut, keywords, enabled, bindButton(elementId, onClick)))
}

/**
 * Adds an item of a command menu with a plain text that runs its handler when chosen and is shown
 * while it matches the query.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param shortcut a keyboard shortcut shown at the end of the item, or `null` for none
 * @param keywords further words the query matches the item by
 * @param enabled whether the item can be chosen
 * @param id the id of the item, or `null` for a generated one
 * @param onClick the handler run when the player chooses the item, or `null` for none
 * @return the item
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CommandItem(
    text: String,
    icon: String? = null,
    shortcut: Component? = null,
    keywords: List<String> = emptyList(),
    enabled: Boolean = true,
    id: String? = null,
    onClick: ButtonHandler? = null,
): CommandItemElement = CommandItem(Component.text(text), icon, shortcut, keywords, enabled, id, onClick)

/**
 * Adds a line between parts of a command menu, hidden while a query is typed.
 *
 * @param id the id of the separator, or `null` for a generated one
 * @return the separator
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.CommandSeparator(id: String? = null): CommandSeparatorElement = add(CommandSeparatorElement(nextId(id)))

/**
 * Adds a dialog: its triggers and one [DialogContent], which is centered over everything when a
 * trigger is clicked.
 *
 * @param onChange the handler run whenever the player opens or closes the dialog, or `null` for
 *        none
 * @param id the id of the dialog, or `null` for a generated one
 * @param children the builder of the triggers and the content
 * @return the reference to whether the dialog is open
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Dialog(onChange: ChangeHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<Boolean> {
    val elementId = nextId(id)
    add(DialogElement(elementId, this.children(children), false, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds the content of a dialog, on a bordered surface with an optional close button.
 *
 * @param width how wide the dialog is laid out
 * @param showCloseButton whether a close button is drawn at the top right
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the header, content and footer
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DialogContent(
    width: ElementSize = ElementSize.fixed(DIALOG_WIDTH),
    showCloseButton: Boolean = true,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): DialogContentElement {
    val elementId = nextId(id)
    return add(DialogContentElement(elementId, this.children(children), showCloseButton, width))
}

/**
 * Adds the header of a dialog or alert dialog: its media, title and description, stacked.
 *
 * @param id the id of the header, or `null` for a generated one
 * @param children the builder of the media, title and description
 * @return the header
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DialogHeader(id: String? = null, children: ComponentScope.() -> Unit): DialogHeaderElement {
    val elementId = nextId(id)
    return add(DialogHeaderElement(elementId, this.children(children)))
}

/**
 * Adds the footer of a dialog or alert dialog: its buttons in a row at the end.
 *
 * @param id the id of the footer, or `null` for a generated one
 * @param children the builder of the buttons
 * @return the footer
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DialogFooter(id: String? = null, children: ComponentScope.() -> Unit): DialogFooterElement {
    val elementId = nextId(id)
    return add(DialogFooterElement(elementId, this.children(children)))
}

/**
 * Adds a part of an overlay whose actions close the overlay after they fire, such as a cancel
 * button.
 *
 * @param id the id of the part, or `null` for a generated one
 * @param children the builder of the buttons
 * @return the part
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DialogClose(id: String? = null, children: ComponentScope.() -> Unit): DialogCloseElement {
    val elementId = nextId(id)
    return add(DialogCloseElement(elementId, this.children(children)))
}

/**
 * Adds the large, bold title of a dialog or alert dialog.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DialogTitle(text: Component, id: String? = null): TextElement = styledText(text, TextKind.DIALOG_TITLE, id = id)

/**
 * Adds the plain, large and bold title of a dialog or alert dialog.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DialogTitle(text: String, id: String? = null): TextElement = DialogTitle(Component.text(text), id)

/**
 * Adds the muted description of a dialog or alert dialog.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DialogDescription(text: Component, id: String? = null): TextElement = styledText(text, TextKind.DIALOG_DESCRIPTION, id = id)

/**
 * Adds the plain, muted description of a dialog or alert dialog.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DialogDescription(text: String, id: String? = null): TextElement = DialogDescription(Component.text(text), id)

/**
 * Adds an alert dialog: its triggers and one [AlertDialogContent], a modal content that a click
 * outside does not close.
 *
 * @param onChange the handler run whenever the player opens or closes the dialog, or `null` for
 *        none
 * @param id the id of the alert dialog, or `null` for a generated one
 * @param children the builder of the triggers and the content
 * @return the reference to whether the dialog is open
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AlertDialog(onChange: ChangeHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<Boolean> {
    val elementId = nextId(id)
    add(AlertDialogElement(elementId, this.children(children), false, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds the content of an alert dialog on a bordered surface, as wide as its size says.
 *
 * @param size the size of the dialog
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the media, header and footer
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AlertDialogContent(
    size: AlertDialogSize = AlertDialogSize.DEFAULT,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): AlertDialogContentElement {
    val elementId = nextId(id)
    val width = ElementSize.fixed(if (size == AlertDialogSize.SM) ALERT_DIALOG_SMALL_WIDTH else DIALOG_WIDTH)
    return add(AlertDialogContentElement(elementId, this.children(children), size, width))
}

/**
 * Adds an icon on a muted square above the title of an alert dialog.
 *
 * @param icon the Lucide name of the icon
 * @param id the id of the media, or `null` for a generated one
 * @return the media
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.AlertDialogMedia(icon: String, id: String? = null): AlertDialogMediaElement = add(AlertDialogMediaElement(nextId(id), icon))

/**
 * Returns the id of the [DialogClose] that wraps an alert dialog button.
 *
 * @param buttonId the explicit id of the button, or `null` for a generated one
 * @return the button id followed by `_close`, or `null` to generate the wrapper's id
 * @throws IllegalArgumentException if [buttonId] is blank
 */
private fun closeWrapperId(buttonId: String?): String? {
    require(buttonId == null || buttonId.isNotBlank()) { "An alert dialog button needs a non-blank id or none, got '$buttonId'" }
    return buttonId?.let { "${it}_close" }
}

/**
 * Adds the action of an alert dialog: a button that runs its handler and closes the dialog,
 * wrapped in a [DialogClose]. The wrapper's id is the button's explicit id followed by `_close`,
 * or a generated one; with an explicit id, `<id>_close` is reserved for the wrapper.
 *
 * @param text the caption
 * @param variant the look of the button
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run when the player confirms, or `null` for none
 * @return the part that wraps the button
 * @throws IllegalArgumentException if [id] starts with `_` or is blank
 */
fun ComponentScope.AlertDialogAction(
    text: Component,
    variant: ButtonVariant = ButtonVariant.DEFAULT,
    id: String? = null,
    onClick: ButtonHandler? = null,
): DialogCloseElement = DialogClose(closeWrapperId(id)) { Button(text, submitsInput = false, variant = variant, id = id, onClick = onClick) }

/**
 * Adds the action of an alert dialog with a plain caption: a button that runs its handler and
 * closes the dialog, wrapped in a [DialogClose]. The wrapper's id is the button's explicit id
 * followed by `_close`, or a generated one; with an explicit id, `<id>_close` is reserved for the
 * wrapper.
 *
 * @param text the caption
 * @param variant the look of the button
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run when the player confirms, or `null` for none
 * @return the part that wraps the button
 * @throws IllegalArgumentException if [id] starts with `_` or is blank
 */
fun ComponentScope.AlertDialogAction(
    text: String,
    variant: ButtonVariant = ButtonVariant.DEFAULT,
    id: String? = null,
    onClick: ButtonHandler? = null,
): DialogCloseElement = AlertDialogAction(Component.text(text), variant, id, onClick)

/**
 * Adds the cancel button of an alert dialog: an outline button that closes the dialog, wrapped in
 * a [DialogClose]. The wrapper's id is the button's explicit id followed by `_close`, or a
 * generated one; with an explicit id, `<id>_close` is reserved for the wrapper.
 *
 * @param text the caption
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run when the player cancels, or `null` for none
 * @return the part that wraps the button
 * @throws IllegalArgumentException if [id] starts with `_` or is blank
 */
fun ComponentScope.AlertDialogCancel(text: Component, id: String? = null, onClick: ButtonHandler? = null): DialogCloseElement =
    DialogClose(closeWrapperId(id)) { Button(text, submitsInput = false, variant = ButtonVariant.OUTLINE, id = id, onClick = onClick) }

/**
 * Adds the cancel button of an alert dialog with a plain caption: an outline button that closes
 * the dialog, wrapped in a [DialogClose]. The wrapper's id is the button's explicit id followed by
 * `_close`, or a generated one; with an explicit id, `<id>_close` is reserved for the wrapper.
 *
 * @param text the caption
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run when the player cancels, or `null` for none
 * @return the part that wraps the button
 * @throws IllegalArgumentException if [id] starts with `_` or is blank
 */
fun ComponentScope.AlertDialogCancel(text: String, id: String? = null, onClick: ButtonHandler? = null): DialogCloseElement =
    AlertDialogCancel(Component.text(text), id, onClick)

/**
 * Adds a sheet: its triggers and one [SheetContent], a modal content attached to an edge of the
 * window when a trigger is clicked.
 *
 * @param onChange the handler run whenever the player opens or closes the sheet, or `null` for
 *        none
 * @param id the id of the sheet, or `null` for a generated one
 * @param children the builder of the triggers and the content
 * @return the reference to whether the sheet is open
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Sheet(onChange: ChangeHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<Boolean> {
    val elementId = nextId(id)
    add(SheetElement(elementId, this.children(children), false, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds the content of a sheet, attached to an edge of the window.
 *
 * @param side the edge of the window the sheet is attached to
 * @param showCloseButton whether a close button is drawn at the top right
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the header, content and footer
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SheetContent(
    side: OverlaySide = OverlaySide.RIGHT,
    showCloseButton: Boolean = true,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): SheetContentElement {
    val elementId = nextId(id)
    return add(SheetContentElement(elementId, this.children(children), side, showCloseButton))
}

/**
 * Adds the header of a sheet or drawer: its title and description, stacked.
 *
 * @param id the id of the header, or `null` for a generated one
 * @param children the builder of the title and description
 * @return the header
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SheetHeader(id: String? = null, children: ComponentScope.() -> Unit): SheetHeaderElement {
    val elementId = nextId(id)
    return add(SheetHeaderElement(elementId, this.children(children)))
}

/**
 * Adds the footer of a sheet or drawer: its buttons, stacked at the end of the content.
 *
 * @param id the id of the footer, or `null` for a generated one
 * @param children the builder of the buttons
 * @return the footer
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SheetFooter(id: String? = null, children: ComponentScope.() -> Unit): SheetFooterElement {
    val elementId = nextId(id)
    return add(SheetFooterElement(elementId, this.children(children)))
}

/**
 * Adds the bold title of a sheet or drawer.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SheetTitle(text: Component, id: String? = null): TextElement = styledText(text, TextKind.SHEET_TITLE, id = id)

/**
 * Adds the plain, bold title of a sheet or drawer.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SheetTitle(text: String, id: String? = null): TextElement = SheetTitle(Component.text(text), id)

/**
 * Adds the muted description of a sheet or drawer.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SheetDescription(text: Component, id: String? = null): TextElement = styledText(text, TextKind.SHEET_DESCRIPTION, id = id)

/**
 * Adds the plain, muted description of a sheet or drawer.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.SheetDescription(text: String, id: String? = null): TextElement = SheetDescription(Component.text(text), id)

/**
 * Adds a drawer: its triggers and one [DrawerContent], a modal content that comes in from an edge
 * of the window, with a handle.
 *
 * @param onChange the handler run whenever the player opens or closes the drawer, or `null` for
 *        none
 * @param id the id of the drawer, or `null` for a generated one
 * @param children the builder of the triggers and the content
 * @return the reference to whether the drawer is open
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Drawer(onChange: ChangeHandler? = null, id: String? = null, children: ComponentScope.() -> Unit): InputRef<Boolean> {
    val elementId = nextId(id)
    add(DrawerElement(elementId, this.children(children), false, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds the content of a drawer, attached to an edge of the window.
 *
 * @param direction the edge of the window the drawer comes in from
 * @param id the id of the content, or `null` for a generated one
 * @param children the builder of the header, content and footer
 * @return the content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.DrawerContent(
    direction: OverlaySide = OverlaySide.BOTTOM,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): DrawerContentElement {
    val elementId = nextId(id)
    return add(DrawerContentElement(elementId, this.children(children), direction))
}

/**
 * Adds a region that confines the dialogs, sheets and drawers opened inside it, such as the frame
 * of a phone.
 *
 * @param width how wide the region is laid out
 * @param height how tall the region is laid out
 * @param id the id of the region, or `null` for a generated one
 * @param children the builder of the content
 * @return the region
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.OverlayContainer(
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): OverlayContainerElement {
    val elementId = nextId(id)
    return add(OverlayContainerElement(elementId, this.children(children), width, height))
}
