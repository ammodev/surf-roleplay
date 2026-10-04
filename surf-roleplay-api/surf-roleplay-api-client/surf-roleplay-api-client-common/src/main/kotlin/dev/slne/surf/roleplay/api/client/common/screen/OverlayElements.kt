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
 * The size of an alert dialog.
 */
enum class AlertDialogSize {
    /**
     * The regular width, with texts at the start.
     */
    DEFAULT,

    /**
     * A narrow width, with centered texts and the footer buttons side by side.
     */
    SM,
}

/**
 * A dialog: its triggers, and a modal content centered over everything when a trigger is clicked.
 *
 * @property id the id of this element
 * @property children the triggers and one dialog content
 * @property open whether the overlay is open
 * @property onChange the handler run whenever the player opens or closes the overlay, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DialogElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a dialog, on a bordered surface with an optional close button.
 *
 * @property id the id of this element
 * @property children the header, content and footer
 * @property showCloseButton whether a close button is drawn at the top right
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DialogContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val showCloseButton: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The header of a dialog: its title and description, stacked.
 *
 * @property id the id of this element
 * @property children the title and description
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DialogHeaderElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The footer of a dialog: its buttons in a row at the end.
 *
 * @property id the id of this element
 * @property children the buttons
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DialogFooterElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A part of an overlay whose actions close the overlay after they fire, such as a cancel button.
 *
 * @property id the id of this element
 * @property children the buttons
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DialogCloseElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An alert dialog: its triggers, and a modal content that a click outside does not close.
 *
 * @property id the id of this element
 * @property children the triggers and one alert dialog content
 * @property open whether the overlay is open
 * @property onChange the handler run whenever the player opens or closes the overlay, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AlertDialogElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of an alert dialog, on a bordered surface.
 *
 * @property id the id of this element
 * @property children the media, header and footer
 * @property size the size of the dialog
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AlertDialogContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val size: AlertDialogSize = AlertDialogSize.DEFAULT,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * An icon on a muted square above the title of an alert dialog.
 *
 * @property id the id of this element
 * @property icon the Lucide name of the icon
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class AlertDialogMediaElement(
    override val id: String,
    val icon: String = "",
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A sheet: its triggers, and a modal content attached to an edge of the window when a trigger is
 * clicked.
 *
 * @property id the id of this element
 * @property children the triggers and one sheet content
 * @property open whether the overlay is open
 * @property onChange the handler run whenever the player opens or closes the overlay, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SheetElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a sheet, attached to an edge of the window.
 *
 * @property id the id of this element
 * @property children the header, content and footer
 * @property side the edge of the window the sheet is attached to
 * @property showCloseButton whether a close button is drawn at the top right
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SheetContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val side: OverlaySide = OverlaySide.RIGHT,
    val showCloseButton: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The header of a sheet or drawer: its title and description, stacked.
 *
 * @property id the id of this element
 * @property children the title and description
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SheetHeaderElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The footer of a sheet or drawer: its buttons, stacked at the end of the content.
 *
 * @property id the id of this element
 * @property children the buttons
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SheetFooterElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A drawer: its triggers, and a modal content that comes in from an edge of the window, with a
 * handle.
 *
 * @property id the id of this element
 * @property children the triggers and one drawer content
 * @property open whether the overlay is open
 * @property onChange the handler run whenever the player opens or closes the overlay, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DrawerElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val open: Boolean = false,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The content of a drawer, attached to an edge of the window.
 *
 * @property id the id of this element
 * @property children the header, content and footer
 * @property direction the edge of the window the drawer comes in from
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DrawerContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val direction: OverlaySide = OverlaySide.BOTTOM,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * The default width of dialogs and alert dialogs, in GUI pixels.
 */
const val DIALOG_WIDTH: Int = 256

/**
 * The width of small alert dialogs, in GUI pixels.
 */
const val ALERT_DIALOG_SMALL_WIDTH: Int = 160

/**
 * A region that confines the modal overlays opened inside it, such as the frame of a phone.
 *
 * @property id the id of this element
 * @property children the content of the region
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class OverlayContainerElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement
