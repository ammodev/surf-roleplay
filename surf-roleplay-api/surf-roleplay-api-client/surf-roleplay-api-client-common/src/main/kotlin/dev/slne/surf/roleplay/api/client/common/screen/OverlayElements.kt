package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component

/**
 * The side of its trigger an overlay opens on.
 */
enum class OverlaySide {
    /**
     * Below the trigger.
     */
    BOTTOM,

    /**
     * Above the trigger.
     */
    TOP,

    /**
     * To the right of the trigger.
     */
    RIGHT,

    /**
     * To the left of the trigger.
     */
    LEFT,
}

/**
 * A popover: its triggers, and a content that opens next to them when a trigger is clicked.
 *
 * @property id the id of this element
 * @property children the triggers and one popover content
 * @property open whether the popover is open
 * @property side the side of the triggers the content opens on
 * @property align how the content is aligned along that side
 * @property onChange the handler run whenever the player opens or closes the overlay, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PopoverElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val side: OverlaySide = OverlaySide.BOTTOM,
    val align: Alignment = Alignment.CENTER,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a popover, on a bordered surface.
 *
 * @property id the id of this element
 * @property children the content, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PopoverContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The header of a popover content: its title and description, stacked.
 *
 * @property id the id of this element
 * @property children the title and description
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class PopoverHeaderElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * Adds the header of a popover content: its title and description, stacked.
 *
 * @param id the id of the header
 * @param children the builder of the title and description
 */
fun ElementsBuilder.popoverHeader(id: String, children: ElementsBuilder.() -> Unit) {
    elements += PopoverHeaderElement(id, build(children))
}

/**
 * A hover card: its triggers, and a content that opens next to them while the mouse rests on them.
 *
 * @property id the id of this element
 * @property children the triggers and one hover card content
 * @property open whether the hover card is open
 * @property side the side of the triggers the content opens on
 * @property align how the content is aligned along that side
 * @property openDelay how long the mouse must rest on a trigger before the content opens, in
 *           milliseconds
 * @property closeDelay how long after the mouse left the triggers and the content it closes, in
 *           milliseconds
 * @property onChange the handler run whenever the player opens or closes the overlay, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class HoverCardElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val side: OverlaySide = OverlaySide.BOTTOM,
    val align: Alignment = Alignment.CENTER,
    val openDelay: Int = 700,
    val closeDelay: Int = 300,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a hover card, on a bordered surface.
 *
 * @property id the id of this element
 * @property children the content, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class HoverCardContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A tooltip: its triggers, and a short text shown next to them while a trigger is hovered or
 * focused.
 *
 * @property id the id of this element
 * @property children the triggers
 * @property text the text as component JSON
 * @property side the side of the triggers the text is shown on
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TooltipElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val text: Component = Component.empty(),
    val side: OverlaySide = OverlaySide.TOP,
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
 * Adds a popover. Its children are its triggers, which open the popover when clicked instead of
 * firing their own actions, and one [popoverContent].
 *
 * @param id the id of the popover
 * @param side the side of the triggers the content opens on
 * @param align how the content is aligned along that side
 * @param open whether the popover starts open
 * @param onChange the handler run whenever the player opens or closes the popover, or `null` for
 *        none
 * @param children the builder of the triggers and the content
 */
fun ElementsBuilder.popover(
    id: String,
    side: OverlaySide = OverlaySide.BOTTOM,
    align: Alignment = Alignment.CENTER,
    open: Boolean = false,
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    elements += PopoverElement(id, build(children), open, side, align, onChange)
}

/**
 * Adds the content of a popover.
 *
 * @param id the id of the content
 * @param width how wide the content is laid out
 * @param children the builder of the content
 */
fun ElementsBuilder.popoverContent(id: String, width: ElementSize = ElementSize.fixed(POPOVER_WIDTH), children: ElementsBuilder.() -> Unit) {
    elements += PopoverContentElement(id, build(children), width)
}

/**
 * Adds the title of a popover.
 *
 * @param id the id of the title
 * @param text the title
 */
fun ElementsBuilder.popoverTitle(id: String, text: Component) {
    elements += TextElement(id, text, TextKind.POPOVER_TITLE)
}

/**
 * Adds the description of a popover.
 *
 * @param id the id of the description
 * @param text the description
 */
fun ElementsBuilder.popoverDescription(id: String, text: Component) {
    elements += TextElement(id, text, TextKind.POPOVER_DESCRIPTION)
}

/**
 * Adds a hover card. Its children are its triggers and one [hoverCardContent], which opens while
 * the mouse rests on a trigger.
 *
 * @param id the id of the hover card
 * @param side the side of the triggers the content opens on
 * @param align how the content is aligned along that side
 * @param openDelay how long the mouse must rest on a trigger before the content opens, in
 *        milliseconds
 * @param closeDelay how long after the mouse left the triggers and the content it closes, in
 *        milliseconds
 * @param onChange the handler run whenever the hover card opens or closes, or `null` for none
 * @param children the builder of the triggers and the content
 */
fun ElementsBuilder.hoverCard(
    id: String,
    side: OverlaySide = OverlaySide.BOTTOM,
    align: Alignment = Alignment.CENTER,
    openDelay: Int = 700,
    closeDelay: Int = 300,
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    require(openDelay >= 0 && closeDelay >= 0) { "Hover card delays must not be negative" }
    elements += HoverCardElement(id, build(children), false, side, align, openDelay, closeDelay, onChange)
}

/**
 * Adds the content of a hover card.
 *
 * @param id the id of the content
 * @param width how wide the content is laid out
 * @param children the builder of the content
 */
fun ElementsBuilder.hoverCardContent(id: String, width: ElementSize = ElementSize.fixed(HOVER_CARD_WIDTH), children: ElementsBuilder.() -> Unit) {
    elements += HoverCardContentElement(id, build(children), width)
}

/**
 * Adds a tooltip around triggers.
 *
 * @param id the id of the tooltip
 * @param text the text shown while a trigger is hovered or focused
 * @param side the side of the triggers the text is shown on
 * @param children the builder of the triggers
 */
fun ElementsBuilder.tooltip(id: String, text: Component, side: OverlaySide = OverlaySide.TOP, children: ElementsBuilder.() -> Unit) {
    elements += TooltipElement(id, build(children), text, side)
}

/**
 * The default width of popover contents, in GUI pixels.
 */
const val POPOVER_WIDTH: Int = 144

/**
 * The default width of hover card contents, in GUI pixels.
 */
const val HOVER_CARD_WIDTH: Int = 128

/**
 * A dropdown menu: its triggers, and a menu content that opens next to them when a trigger is
 * clicked.
 *
 * @property id the id of this element
 * @property children the triggers and one menu content
 * @property open whether the menu is open
 * @property side the side of the triggers the menu opens on
 * @property align how the menu is aligned along that side
 * @property onChange the handler run whenever the player opens or closes the menu, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DropdownMenuElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val side: OverlaySide = OverlaySide.BOTTOM,
    val align: Alignment = Alignment.CENTER,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a menu: a stack of items, labels, separators, groups and sub-menus on a bordered
 * surface.
 *
 * @property id the id of this element
 * @property children the items, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenuContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An item of a menu that fires a widget action and closes the menu when chosen.
 *
 * @property id the id of this element
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property shortcut a keyboard shortcut shown at the end of the item as component JSON, or
 *           `null` for none
 * @property destructive whether the item is drawn in the destructive colour
 * @property inset whether the text is indented as if it had an icon
 * @property enabled whether the item can be used
 * @property onClick the handler run when the player chooses the item, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenuItemElement(
    override val id: String,
    val text: Component = Component.empty(),
    val icon: String? = null,
    val shortcut: Component? = null,
    val destructive: Boolean = false,
    val inset: Boolean = false,
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * An item of a menu with a check mark; choosing it flips the mark and fires a widget action
 * carrying the new state.
 *
 * @property id the id of this element
 * @property text the text as component JSON
 * @property checked whether the item is checked
 * @property enabled whether the item can be used
 * @property onToggle the handler run with the new state when the player chooses the item, or
 *           `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenuCheckboxItemElement(
    override val id: String,
    val text: Component = Component.empty(),
    val checked: Boolean = false,
    val enabled: Boolean = true,
    val onToggle: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A group of radio items of which one is chosen; choosing an item fires the group's widget action
 * carrying its value.
 *
 * @property id the id of this element
 * @property children the radio items, in order
 * @property value the value of the chosen item, or empty for none
 * @property onSelect the handler run with the new value when the player chooses an item, or
 *           `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenuRadioGroupElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val value: String = "",
    val onSelect: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An item of a radio group, marked with a dot while it is chosen.
 *
 * @property id the id of this element
 * @property text the text as component JSON
 * @property value the value the item stands for
 * @property enabled whether the item can be used
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenuRadioItemElement(
    override val id: String,
    val text: Component = Component.empty(),
    val value: String = "",
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A heading inside a menu.
 *
 * @property id the id of this element
 * @property text the text as component JSON
 * @property inset whether the text is indented as if it had an icon
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenuLabelElement(
    override val id: String,
    val text: Component = Component.empty(),
    val inset: Boolean = false,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A line between parts of a menu.
 *
 * @property id the id of this element
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenuSeparatorElement(
    override val id: String,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A group of related menu items.
 *
 * @property id the id of this element
 * @property children the items, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenuGroupElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A sub-menu: its sub trigger, and a menu content that opens beside the trigger.
 *
 * @property id the id of this element
 * @property children the sub trigger and one menu content
 * @property open whether the menu is open
 * @property onChange the handler run whenever the player opens or closes the menu, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenuSubElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The item of a menu that opens a sub-menu, marked with an arrow.
 *
 * @property id the id of this element
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property inset whether the text is indented as if it had an icon
 * @property enabled whether the item can be used
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenuSubTriggerElement(
    override val id: String,
    val text: Component = Component.empty(),
    val icon: String? = null,
    val inset: Boolean = false,
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A context menu: an area, and a menu content that opens at the mouse on a right click in the area.
 *
 * @property id the id of this element
 * @property children the area and one menu content
 * @property open whether the menu is open
 * @property onChange the handler run whenever the player opens or closes the menu, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ContextMenuElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A horizontal bar of menus, as at the top of an application.
 *
 * @property id the id of this element
 * @property children the menus, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenubarElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A menu of a menubar: its trigger, and a menu content that opens below it.
 *
 * @property id the id of this element
 * @property children the menubar trigger and one menu content
 * @property open whether the menu is open
 * @property onChange the handler run whenever the player opens or closes the menu, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenubarMenuElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The trigger of a menubar menu, a text that is highlighted while its menu is open.
 *
 * @property id the id of this element
 * @property text the text as component JSON
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class MenubarTriggerElement(
    override val id: String,
    val text: Component = Component.empty(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * Adds a dropdown menu. Its children are its triggers and one [menuContent].
 *
 * @param id the id of the menu
 * @param side the side of the triggers the menu opens on
 * @param align how the menu is aligned along that side
 * @param onChange the handler run whenever the player opens or closes the menu, or `null` for
 *        none
 * @param children the builder of the triggers and the content
 */
fun ElementsBuilder.dropdownMenu(
    id: String,
    side: OverlaySide = OverlaySide.BOTTOM,
    align: Alignment = Alignment.CENTER,
    onChange: ChangeHandler? = null,
    children: ElementsBuilder.() -> Unit,
) {
    elements += DropdownMenuElement(id, build(children), false, side, align, onChange)
}

/**
 * Adds the content of a menu.
 *
 * @param id the id of the content
 * @param children the builder of the entries
 */
fun ElementsBuilder.menuContent(id: String, children: ElementsBuilder.() -> Unit) {
    elements += MenuContentElement(id, build(children))
}

/**
 * Adds a menu item.
 *
 * @param id the id of the item
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param shortcut a shortcut shown at the end, or `null` for none
 * @param destructive whether the item is drawn in the destructive colour
 * @param inset whether the text is indented as if it had an icon
 * @param enabled whether the item can be chosen
 * @param onClick the handler run when the player chooses the item, or `null` for none
 */
fun ElementsBuilder.menuItem(
    id: String,
    text: Component,
    icon: String? = null,
    shortcut: Component? = null,
    destructive: Boolean = false,
    inset: Boolean = false,
    enabled: Boolean = true,
    onClick: ButtonHandler? = null,
) {
    elements += MenuItemElement(id, text, icon, shortcut, destructive, inset, enabled, onClick)
}

/**
 * Adds a menu item with a check mark. Its handler reads the new state with
 * [ScreenValues.checked].
 *
 * @param id the id of the item
 * @param text the text
 * @param checked whether the item starts checked
 * @param enabled whether the item can be chosen
 * @param onToggle the handler run with the new state when the player chooses the item, or `null`
 *        for none
 */
fun ElementsBuilder.menuCheckboxItem(id: String, text: Component, checked: Boolean = false, enabled: Boolean = true, onToggle: ButtonHandler? = null) {
    elements += MenuCheckboxItemElement(id, text, checked, enabled, onToggle)
}

/**
 * Adds a radio group to a menu. Its handler reads the chosen value with [ScreenValues.text].
 *
 * @param id the id of the group
 * @param value the value of the item chosen at first, or empty for none
 * @param onSelect the handler run with the new value when the player chooses an item, or `null`
 *        for none
 * @param children the builder of the radio items
 */
fun ElementsBuilder.menuRadioGroup(id: String, value: String = "", onSelect: ButtonHandler? = null, children: ElementsBuilder.() -> Unit) {
    elements += MenuRadioGroupElement(id, build(children), value, onSelect)
}

/**
 * Adds an item to a menu radio group.
 *
 * @param id the id of the item
 * @param text the text
 * @param value the value the item stands for
 * @param enabled whether the item can be chosen
 */
fun ElementsBuilder.menuRadioItem(id: String, text: Component, value: String, enabled: Boolean = true) {
    elements += MenuRadioItemElement(id, text, value, enabled)
}

/**
 * Adds a heading to a menu.
 *
 * @param id the id of the label
 * @param text the text
 * @param inset whether the text is indented as if it had an icon
 */
fun ElementsBuilder.menuLabel(id: String, text: Component, inset: Boolean = false) {
    elements += MenuLabelElement(id, text, inset)
}

/**
 * Adds a line between parts of a menu.
 *
 * @param id the id of the separator
 */
fun ElementsBuilder.menuSeparator(id: String) {
    elements += MenuSeparatorElement(id)
}

/**
 * Adds a group of related menu entries.
 *
 * @param id the id of the group
 * @param children the builder of the entries
 */
fun ElementsBuilder.menuGroup(id: String, children: ElementsBuilder.() -> Unit) {
    elements += MenuGroupElement(id, build(children))
}

/**
 * Adds a sub-menu. Its children are one [menuSubTrigger] and one [menuContent].
 *
 * @param id the id of the sub-menu
 * @param onChange the handler run whenever the player opens or closes the sub-menu, or `null`
 *        for none
 * @param children the builder of the trigger and the content
 */
fun ElementsBuilder.menuSub(id: String, onChange: ChangeHandler? = null, children: ElementsBuilder.() -> Unit) {
    elements += MenuSubElement(id, build(children), false, onChange)
}

/**
 * Adds the trigger of a sub-menu.
 *
 * @param id the id of the trigger
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param inset whether the text is indented as if it had an icon
 * @param enabled whether the trigger can be used
 */
fun ElementsBuilder.menuSubTrigger(id: String, text: Component, icon: String? = null, inset: Boolean = false, enabled: Boolean = true) {
    elements += MenuSubTriggerElement(id, text, icon, inset, enabled)
}

/**
 * Adds a context menu. Its children are the area a right click opens the menu in, and one
 * [menuContent].
 *
 * @param id the id of the context menu
 * @param onChange the handler run whenever the player opens or closes the menu, or `null` for
 *        none
 * @param children the builder of the area and the content
 */
fun ElementsBuilder.contextMenu(id: String, onChange: ChangeHandler? = null, children: ElementsBuilder.() -> Unit) {
    elements += ContextMenuElement(id, build(children), false, onChange)
}

/**
 * Adds a menubar.
 *
 * @param id the id of the menubar
 * @param children the builder of the menus
 */
fun ElementsBuilder.menubar(id: String, children: ElementsBuilder.() -> Unit) {
    elements += MenubarElement(id, build(children))
}

/**
 * Adds a menu to a menubar. Its children are one [menubarTrigger] and one [menuContent].
 *
 * @param id the id of the menu
 * @param onChange the handler run whenever the player opens or closes the menu, or `null` for
 *        none
 * @param children the builder of the trigger and the content
 */
fun ElementsBuilder.menubarMenu(id: String, onChange: ChangeHandler? = null, children: ElementsBuilder.() -> Unit) {
    elements += MenubarMenuElement(id, build(children), false, onChange)
}

/**
 * Adds the trigger of a menubar menu.
 *
 * @param id the id of the trigger
 * @param text the text
 */
fun ElementsBuilder.menubarTrigger(id: String, text: Component) {
    elements += MenubarTriggerElement(id, text)
}

/**
 * A command menu: a search input above a list of items that the input filters.
 *
 * @property id the id of this element
 * @property children the input and the list
 * @property onSearch whether the mod reports the typed query instead of filtering the items
 *           itself
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CommandElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val onSearch: SearchHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The search input of a command menu, with a search icon.
 *
 * @property id the id of this element
 * @property placeholder the hint shown while the input is empty, as component JSON
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CommandInputElement(
    override val id: String,
    val placeholder: Component = Component.empty(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * The scrolling list of a command menu.
 *
 * @property id the id of this element
 * @property children the empty text, groups, items and separators
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CommandListElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The text of a command menu shown while no item matches the query.
 *
 * @property id the id of this element
 * @property text the text as component JSON
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CommandEmptyElement(
    override val id: String,
    val text: Component = Component.empty(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A group of command items under a heading, hidden while none of its items match the query.
 *
 * @property id the id of this element
 * @property children the items, in order
 * @property heading the heading as component JSON, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CommandGroupElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val heading: Component? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An item of a command menu that fires a widget action when chosen and is shown while it matches
 * the query.
 *
 * @property id the id of this element
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property shortcut a keyboard shortcut shown at the end of the item as component JSON, or
 *           `null` for none
 * @property keywords further words the query matches the item by
 * @property enabled whether the item can be chosen
 * @property onClick the handler run when the player chooses the item, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CommandItemElement(
    override val id: String,
    val text: Component = Component.empty(),
    val icon: String? = null,
    val shortcut: Component? = null,
    val keywords: List<String> = emptyList(),
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A line between parts of a command menu, hidden while a query is typed.
 *
 * @property id the id of this element
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CommandSeparatorElement(
    override val id: String,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * Adds a command menu. Its input filters its items on the client, unless it has a search
 * handler, which then receives every query and answers by patching the list.
 *
 * @param id the id of the command
 * @param onSearch the handler run for every typed query, or `null` to filter on the client
 * @param children the builder of the input and the list
 */
fun ElementsBuilder.command(id: String, onSearch: SearchHandler? = null, children: ElementsBuilder.() -> Unit) {
    elements += CommandElement(id, build(children), onSearch, width = ElementSize.grow())
}

/**
 * Adds the search input of a command menu.
 *
 * @param id the id of the input
 * @param placeholder the hint shown while the input is empty
 */
fun ElementsBuilder.commandInput(id: String, placeholder: Component = Component.empty()) {
    elements += CommandInputElement(id, placeholder, width = ElementSize.grow())
}

/**
 * Adds the list of a command menu.
 *
 * @param id the id of the list
 * @param children the builder of the empty text, groups, items and separators
 */
fun ElementsBuilder.commandList(id: String, children: ElementsBuilder.() -> Unit) {
    elements += CommandListElement(id, build(children))
}

/**
 * Adds the text shown while no item matches.
 *
 * @param id the id of the text
 * @param text the text
 */
fun ElementsBuilder.commandEmpty(id: String, text: Component) {
    elements += CommandEmptyElement(id, text)
}

/**
 * Adds a group of command items.
 *
 * @param id the id of the group
 * @param heading the heading, or `null` for none
 * @param children the builder of the items
 */
fun ElementsBuilder.commandGroup(id: String, heading: Component? = null, children: ElementsBuilder.() -> Unit) {
    elements += CommandGroupElement(id, build(children), heading)
}

/**
 * Adds a command item.
 *
 * @param id the id of the item
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param shortcut a shortcut shown at the end, or `null` for none
 * @param keywords further words the query matches the item by
 * @param enabled whether the item can be chosen
 * @param onClick the handler run when the player chooses the item, or `null` for none
 */
fun ElementsBuilder.commandItem(
    id: String,
    text: Component,
    icon: String? = null,
    shortcut: Component? = null,
    keywords: List<String> = emptyList(),
    enabled: Boolean = true,
    onClick: ButtonHandler? = null,
) {
    elements += CommandItemElement(id, text, icon, shortcut, keywords, enabled, onClick)
}

/**
 * Adds a line between parts of a command menu.
 *
 * @param id the id of the separator
 */
fun ElementsBuilder.commandSeparator(id: String) {
    elements += CommandSeparatorElement(id)
}
